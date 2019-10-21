/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.dwd.cdc;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class is responsible for requesting DWD FeatureServices for stationary
 * weather data.
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DwdWfsRequestor {

	final static Logger LOG = LoggerFactory.getLogger(DwdWfsRequestor.class);

	/**
	 * Performs a query with the given parameters
	 *
	 * @param url    DWD CDC FeatureService URL
	 * @param params Paramaters for the FeatureService URL
	 * @return metadata for the found stationary weather data
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static DwdProductsMetadata request(String url, DwdWfsRequestParams params)
			throws IOException, ParserConfigurationException, SAXException {
		LOG.info("Start Buildung Connection Parameters for WFS Service");

		DwdWfsRequestorBuilder wfsRequest = new DwdWfsRequestorBuilder(params);
		LOG.info("Start getFeature request");
		String getPostBody = wfsRequest.createXmlPostMessage().xmlText();
		InputStream httpContent = sendWfsRequest(url, getPostBody);
		String text = IOUtils.toString(httpContent, StandardCharsets.UTF_8.name());
		FeatureJSON featureJson = new FeatureJSON();
		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = featureJson.readFeatureCollection(text);

		// Connect to WFS
		LOG.info("Start getCapabilities request");
		String capPostBody = wfsRequest.createCapabilitiesPost().xmlText();
		InputStream capResponse = sendWfsRequest(url, capPostBody);

		// create DwdProductsMetaData
		DwdProductsMetadata metadata = new DwdProductsMetadata();

		// typename and clearname
		String[] featureClearName = requestTypeName(params, capResponse);
		metadata.setLayername(featureClearName[0]);
		metadata.setParameter(featureClearName[1]);

		LOG.info("Calculating the actual timeFrame and BoundingBox");
		// set parameters
		SpatioTemporalExtent timeAndBbox = generateSpatioTemporalExtent(collection);

		LOG.info("Building DwdProductsMetaData Object");
		// bbox
		ArrayList<Float> extent = timeAndBbox.getbBox();
		metadata.setExtent(extent.get(0), extent.get(1), extent.get(2), extent.get(3));

		// timeframe
		ArrayList<DateTime> timeFrame = timeAndBbox.getTimeFrame();
		metadata.setStartDate(timeFrame.get(0));
		metadata.setEndDate(timeFrame.get(1));

		// serviceurl
		metadata.setServiceUrl(url);
		LOG.info("End of request()-Method - Return DwdProductsMetaData Object");
		return metadata;
	}

	/**
	 * Delivers a String Array consisting of <name>- and <title> values
	 * 
	 * @param params      Paramaters for the FeatureService URL
	 * @param capResponse getCapabilities document
	 * @return featureTypeName <name> and <title> of the denoted feature
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static String[] requestTypeName(DwdWfsRequestParams params, InputStream capResponse)
			throws ParserConfigurationException, SAXException, IOException {
		// create Document to search for the correct Elements
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbf.newDocumentBuilder();
		Document doc = docBuilder.parse(capResponse);
		// initialize return attribute
		String[] featureTypeName = new String[2];

		// search all FeatureType elements
		NodeList nodes = doc.getElementsByTagName("FeatureType");
		for (int i = 0; i < nodes.getLength(); i++) {
			// check content of childnodes <name> and <title> of every <FeatureType>
			Element featureType = (Element) nodes.item(i);
			NodeList childNodes = featureType.getChildNodes();
			Element name = (Element) childNodes.item(0);

			String typename = DwdWfsRequestorBuilder.typeNamePrefix + params.getTypeName();
			// search for the correct typeName
			if (name.getTextContent().equals(typename)) {
				Element title = (Element) childNodes.item(1);

				// fill return attribute
				featureTypeName[0] = name.getTextContent(); // <name>
				featureTypeName[1] = title.getTextContent(); // <title>
			}

		}
		return featureTypeName;
	}

	/**
	 * Delivers the post response depending on the outputformat (e.g. xml, json)
	 * 
	 * @param url         serviceURL
	 * @param postRequest post message (xml)
	 * @return httpContent post response
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	protected static InputStream sendWfsRequest(String url, String postRequest)
			throws UnsupportedEncodingException, IOException, ClientProtocolException {
		// contact http-client
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader("content-type", "application/xml");
		// create PostMessage
		StringEntity entity = new StringEntity(postRequest);
		httpPost.setEntity(entity);
		HttpResponse response = httpclient.execute(httpPost);

		HttpEntity responseEntity = response.getEntity(); // fill http-Object (status, parameters, content)
		InputStream httpContent = responseEntity.getContent(); // ask for content
		return httpContent;
	}

	/**
	 * Determines the spatial and temporal extentn of the denoted feature
	 * 
	 * @param collection FeatureCollection from GeoJSON
	 * @return timeAndBbox spatial and temporal extent
	 * @throws IOException
	 */
	private static SpatioTemporalExtent generateSpatioTemporalExtent(
			FeatureCollection<SimpleFeatureType, SimpleFeature> collection) throws IOException {

		// initialize necessary attributes
		SpatioTemporalExtent timeAndBbox = new SpatioTemporalExtent();
		LOG.info("Connecting WFS Service");
		FeatureIterator<SimpleFeature> iterator = collection.features();

		// TimeFrame Parameter
		DateTime startDate = new DateTime();
		DateTime endDate = new DateTime();
		ArrayList<DateTime> timeFrame = new ArrayList<DateTime>();

		// BBOX Parameter
		ArrayList<Float> extent = new ArrayList<Float>();
		extent.add(0, (float) collection.getBounds().getMinX());
		extent.add(1, (float) collection.getBounds().getMinY());
		extent.add(2, (float) collection.getBounds().getMaxX());
		extent.add(3, (float) collection.getBounds().getMaxY());

		try {
			for (int i = 1; hasNextNew(iterator); i++) {
				SimpleFeature feature = (SimpleFeature) iterator.next();

				// Request time attribute
				DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
				// DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
				// String wert = feature.getAttribute("ZEITSTEMPEL").toString();
				DateTime temp = DateTime.parse(feature.getAttribute("ZEITSTEMPEL").toString(), df);

				// Set start Values
				if (i == 1) {

					// Time Frame - First values
					startDate = temp;
					endDate = temp;
					timeFrame.add(0, startDate);
					timeFrame.add(1, endDate);
				}

				// Set StartDate or EndDate
				if (temp.isBefore(startDate)) {
					startDate = temp;
					timeFrame.remove(0);
					timeFrame.add(0, startDate);
				}

				if (temp.isAfter(endDate)) {
					endDate = temp;
					timeFrame.remove(1);
					timeFrame.add(1, endDate);
				}
			}
		} finally {
			iterator.close();
		}

		timeAndBbox.setbBox(extent);
		timeAndBbox.setTimeFrame(timeFrame);

		return timeAndBbox;
	}

	/**
	 * Checks, if the iterator has a next element
	 * 
	 * @param iterator features of the collection
	 * @return hasNext return boolean
	 */
	private static boolean hasNextNew(FeatureIterator<SimpleFeature> iterator) {
		boolean hasNext = false;
		try {
			hasNext = iterator.hasNext();
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug("error while deserializing features", e);
		}

		return hasNext;
	}

}
