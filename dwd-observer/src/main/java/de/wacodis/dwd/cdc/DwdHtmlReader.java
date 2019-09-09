package de.wacodis.dwd.cdc;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.geotools.geometry.Envelope2D;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DwdHtmlReader {
	// attributes
	private static String xmlns = "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
			+ "xmlns=\"http://www.opengis.net/wfs/2.0\" " + "xmlns:wfs=\"http://www.opengis.net/wfs/2.0\" "
			+ "xmlns:ows=\"http://www.opengis.net/ows/1.1\" " + "xmlns:gml=\"http://www.opengis.net/gml/3.2\" "
			+ "xmlns:fes=\"http://www.opengis.net/fes/2.0\" " + "xmlns:xlink=\"http://www.w3.org/1999/xlink\" "
			+ "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" "
			+ "xmlns:inspire_dls=\"http://inspire.ec.europa.eu/schemas/inspire_dls/1.0\" "
			+ "xmlns:inspire_common=\"http://inspire.ec.europa.eu/schemas/common/1.0\" "
			+ "xmlns:CDC=\"https://cdc.dwd.de\" "
			+ "xsi:schemaLocation=\"http://www.opengis.net/wfs/2.0 https://cdc.dwd.de:443/geoserver/schemas/wfs/2.0/wfs.xsd http://inspire.ec.europa.eu/schemas/inspire_dls/1.0 http://inspire.ec.europa.eu/schemas/inspire_dls/1.0/inspire_dls.xsd\" "
			+ "updateS-equence=\"727\" " + "xmlns:ogc=\"http://www.opengis.net/ogc\">";

	// example GETurl:
	String propUrl;// https://cdc.dwd.de/geoserver/CDC/wfs?service=WFS&request=GetFeature
	String version;// &version=2.0.0
	String typeName;// &typeName=CDC%3AVGSL_FX_MN003
	List<String> bbox;// &bbox=51.200,6.700,51.500,7.300
	DateTime startDate;
	DateTime endDate;
	DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
	String outputFormat;// &outputformat=application%2Fjson

	// constructor
	public DwdHtmlReader(String propUrl) {
		this.propUrl = propUrl;
	}

	public InputStream createWfsRequestPost() throws ClientProtocolException, IOException {

		// contact http-client
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost();
		
		// create PostMessage
		String postRequest = createXmlPostMessage();
		StringEntity entity = new StringEntity(postRequest);
		httpPost.setEntity(entity);
		CloseableHttpResponse response = httpclient.execute(httpPost);
		
		HttpEntity responseEntity = response.getEntity(); // fill http-Object (status, parameters, content)
		InputStream httpcontent = responseEntity.getContent(); // ask for content
		return httpcontent;
	}

	private String createXmlPostMessage() {
		StringBuffer wfsRequest = new StringBuffer();
		// GetFeature-tag
		wfsRequest.append("<wfs:GetFeature service=\"WFS\" version=\"" + version + "\" outputFormat=\"" + outputFormat);
		wfsRequest.append(xmlns);
		// Query-tag
		wfsRequest.append("<wfs:Query typeNames=\"CDC:VGSL_TT_TU_MN009\">");
		wfsRequest.append("<fes:Filter><fes:And>"); // Filter
		wfsRequest.append("<fes:BBOX>" + "                <fes:ValueReference>CDC:GEOM</fes:ValueReference>"
				+ "                <gml:Envelope srsName=\"urn:ogc:def:crs:EPSG::4326\">"
				+ "                    <gml:lowerCorner>" + bbox.get(1) + " " + bbox.get(0) + "</gml:lowerCorner>"
				+ "                    <gml:upperCorner>" + bbox.get(3) + " " + bbox.get(2) + "</gml:upperCorner>"
				+ "                </gml:Envelope>" + "            </fes:BBOX>"); // BBOX
		wfsRequest.append(
				" <fes:PropertyIsBetween>" + "            	<fes:ValueReference>CDC:ZEITSTEMPEL</fes:ValueReference>"
						+ "            	<fes:Literal>" + formatter.print(startDate) + "</fes:Literal>" + // 2019-06-02T06:00:00Z
						"            	<fes:Literal>" + formatter.print(endDate) + "</fes:Literal>" + // 2019-06-02T10:00:00Z
						"            </fes:PropertyIsBetween>"); // timeFrame
		wfsRequest.append(" </fes:And></fes:Filter></wfs:Query>"); // end query
		
		return wfsRequest.toString();
	}

}
