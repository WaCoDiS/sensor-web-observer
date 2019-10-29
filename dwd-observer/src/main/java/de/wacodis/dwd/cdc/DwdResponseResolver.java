package de.wacodis.dwd.cdc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DwdResponseResolver {

	final static Logger LOG = LoggerFactory.getLogger(DwdWfsRequestor.class);
	public static final String FEATURE_TYPE_TAG = "FeatureType";
	public static final String TITLE_TAG = "Title";
	public static final String NAME_TAG = "Name";
	//public static final String TIME_ATTRIBUTE = "CDC:ZEITSTEMPEL";
	//public static final String timeFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public static final String LOWER_CORNER_TAG = "gml:lowerCorner";
	public static final String UPPER_CORNER_TAG = "gml:upperCorner";
	
	
	DwdWfsRequestParams params;
	static String typename;
	
	public DwdResponseResolver(DwdWfsRequestParams params) {
		super();
		this.params = params;
		this.typename = DwdWfsRequestorBuilder.TYPE_NAME_PREFIX + params.getTypeName();
		
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
	public String[] requestTypeName(InputStream capResponse)
			throws ParserConfigurationException, SAXException, IOException {
		LOG.info("Resolve TypeName out of GetCapabilities Document");
		
		// create Document to search for the correct Elements
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbf.newDocumentBuilder();
		Document doc = docBuilder.parse(capResponse);
		// initialize return attribute
		String[] featureTypeName = new String[2];

		// search all FeatureType elements
		NodeList nodes = doc.getElementsByTagName(FEATURE_TYPE_TAG);
		for (int i = 0; i < nodes.getLength(); i++) {
			// check content of childnodes <name> and <title> of every <FeatureType>
			Element featureType = (Element) nodes.item(i);
			
			NodeList titleNodes = featureType.getElementsByTagName(TITLE_TAG);
			String title = titleNodes.item(0).getTextContent();

			NodeList NameNodes = featureType.getElementsByTagName(NAME_TAG);
			String name = NameNodes.item(0).getTextContent();
				
			// search for the correct typeName
			if (name.equals(typename)) {
				// fill return attribute
				featureTypeName[0] = name; // <name>
				featureTypeName[1] = title; // <title>
			}

		}
		
		return featureTypeName;
	}
	
	
	
	/**
	 * Determines the spatial and temporal extentn of the denoted feature
	 * 
	 * @param collection FeatureCollection from GeoJSON
	 * @return timeAndBbox spatial and temporal extent
	 * @throws IOException
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public SpatioTemporalExtent generateSpatioTemporalExtent(InputStream getFeatureResponse) throws IOException, SAXException, ParserConfigurationException {
		LOG.info("Resolving the actual timeframe and bounding box out of GetFeature Document");
		SpatioTemporalExtent timeAndBbox = new SpatioTemporalExtent();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbf.newDocumentBuilder();
		Document doc = docBuilder.parse(getFeatureResponse);

		// BBOX 
		NodeList lowerNodes = doc.getElementsByTagName(LOWER_CORNER_TAG);
		String lowerCornerBBox = lowerNodes.item(0).getTextContent();
		NodeList upperNodes = doc.getElementsByTagName(UPPER_CORNER_TAG);
		String upperCornerBBox = upperNodes.item(0).getTextContent();
		
		// BBOX Parameter 
		//Schema is [minLon, minLat, maxLon, maxLat]
		ArrayList<Float> extent = new ArrayList<Float>();
		extent.add(0, Float.parseFloat(lowerCornerBBox.split(" ")[1]));
		extent.add(1, Float.parseFloat(lowerCornerBBox.split(" ")[0]));
		extent.add(2, Float.parseFloat(upperCornerBBox.split(" ")[1]));
		extent.add(3, Float.parseFloat(upperCornerBBox.split(" ")[0]));
		
		// TimeFrame Parameter
		DateTime startDate = new DateTime();
		DateTime endDate = new DateTime();
		ArrayList<DateTime> timeFrame = new ArrayList<DateTime>();	
		
		NodeList featureNodes = doc.getElementsByTagName(typename);
		for (int i = 0; i < featureNodes.getLength(); i++) {
			Element feature = (Element) featureNodes.item(i);
			NodeList timeStampNodes = feature.getElementsByTagName(DwdWfsRequestorBuilder.TIMESTAMP_ATTRIBUTE);
			String timeStamp = timeStampNodes.item(0).getTextContent();
			
			DateTime temp = DateTime.parse(timeStamp, DwdWfsRequestorBuilder.FORMATTER);
			// Set start Values
			if (i == 0) {

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
	timeAndBbox.setbBox(extent);
	timeAndBbox.setTimeFrame(timeFrame);

	return timeAndBbox;
		
	}
	
}
