package de.wacodis.dwd.cdc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.xmlbeans.XmlCursor;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opengis.filter.PropertyIsBetween;

import net.opengis.fes.x20.BBOXDocument;
import net.opengis.fes.x20.BBOXType;
import net.opengis.fes.x20.BinaryLogicOpType;
import net.opengis.fes.x20.ComparisonOpsType;
import net.opengis.fes.x20.FilterDocument;
import net.opengis.fes.x20.FilterType;
import net.opengis.fes.x20.FunctionType;
import net.opengis.fes.x20.LogicOpsDocument;
import net.opengis.fes.x20.LogicOpsType;
import net.opengis.fes.x20.ValueReferenceDocument;
import net.opengis.wfs.x20.GetFeatureDocument;
import net.opengis.wfs.x20.GetFeatureType;
import net.opengis.wfs.x20.PropertyNameDocument;
import net.opengis.wfs.x20.PropertyNameDocument.PropertyName;
import net.opengis.wfs.x20.QueryDocument;
import net.opengis.wfs.x20.QueryType;

public class DwdWfsRequestorBuilder {

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
	public static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
	// example GETurl:
	// String propUrl;//
	// https://cdc.dwd.de/geoserver/CDC/wfs?service=WFS&request=GetFeature
	String version;// &version=2.0.0
	String typeName;// &typeName=CDC%3AVGSL_FX_MN003
	List<String> bbox = new ArrayList<String>();// &bbox=51.200,6.700,51.500,7.300
	DateTime startDate;
	DateTime endDate;
	String outputFormat;// &outputformat=application%2Fjson

	// constructor
	public DwdWfsRequestorBuilder(DwdWfsRequestParams params) {
		this.version = params.getVersion();
		this.typeName = params.getTypeName();
		for (int i = 0; i < params.getBbox().size(); i++) {
			bbox.add(i, Float.toString(params.getBbox().get(i)));
		}
		this.startDate = params.getStartDate();
		this.endDate = params.getEndDate();
		this.outputFormat = params.getOutputFormat();
	}

	public String createXmlPostMessage() {

		ArrayList<String[]> namespaces = new ArrayList<String[]>();
		namespaces.add(new String[] { "xsi", "http://www.w3.org/2001/XMLSchema-instance" });
		namespaces.add(new String[] { "", "http://www.opengis.net/wfs/2.0" });
		namespaces.add(new String[] { "wfs", "http://www.opengis.net/wfs/2.0" });
		namespaces.add(new String[] { "ows", "http://www.opengis.net/ows/1.1" });
		namespaces.add(new String[] { "gml", "http://www.opengis.net/gml/3.2" });
		namespaces.add(new String[] { "fes", "http://www.opengis.net/fes/2.0" });
		namespaces.add(new String[] { "xlink", "http://www.w3.org/1999/xlink" });
		namespaces.add(new String[] { "xs", "http://www.w3.org/2001/XMLSchema" });
		namespaces.add(new String[] { "inspire_dls", "http://inspire.ec.europa.eu/schemas/inspire_dls/1.0" });
		namespaces.add(new String[] { "inspire_common", "http://inspire.ec.europa.eu/schemas/common/1.0" });
		namespaces.add(new String[] { "CDC", "https://cdc.dwd.de" });
		namespaces.add(new String[] { "ogc", "http://www.opengis.net/ogc" });

		ArrayList<String[]> attributes = new ArrayList<String[]>();
		attributes.add(new String[] { "xsi", "schemaLocation",
				"http://www.opengis.net/wfs/2.0 https://cdc.dwd.de:443/geoserver/schemas/wfs/2.0/wfs.xsd http://inspire.ec.europa.eu/schemas/inspire_dls/1.0 http://inspire.ec.europa.eu/schemas/inspire_dls/1.0/inspire_dls.xsd" });
		// attributes.add(new String[] {"updateS-equence", "727"});

		GetFeatureDocument getFeatureDoc = GetFeatureDocument.Factory.newInstance();
		GetFeatureType getFeature = getFeatureDoc.addNewGetFeature();
		getFeature.setService("WFS");
		getFeature.setVersion(this.version);
		getFeature.setOutputFormat(this.outputFormat);

		// namespaces
		XmlCursor cursor = getFeature.newCursor();
		cursor.toNextToken();
		for (int i = 0; i < namespaces.size(); i++) {
			cursor.insertNamespace(namespaces.get(i)[0], namespaces.get(i)[1]);
		}
		cursor.insertAttributeWithValue(attributes.get(0)[1], attributes.get(0)[0], attributes.get(0)[2]);

		cursor.dispose();

		QueryDocument queryDoc = QueryDocument.Factory.newInstance();
		QueryType query = queryDoc.addNewQuery();
		ArrayList<String> typeList = new ArrayList<String>();
		typeList.add("layer_01");

		PropertyNameDocument propertyNameDoc = PropertyNameDocument.Factory.newInstance();
		PropertyName propertyName = propertyNameDoc.addNewPropertyName();
		propertyName.setStringValue("TestAttributeValue");

		FilterDocument filterDocument = FilterDocument.Factory.newInstance();
		FilterType filter = filterDocument.addNewFilter();
		filter.addNewLogicOps();

		BBOXDocument bboxDocument = BBOXDocument.Factory.newInstance();
		BBOXType bbox2 = bboxDocument.addNewBBOX();

		ValueReferenceDocument geomColumnDocument = ValueReferenceDocument.Factory.newInstance();
		geomColumnDocument.setValueReference("CDC:GEOM");
		bbox2.set(geomColumnDocument);

		filter.set(bboxDocument);

		query.set(propertyNameDoc);
		query.setTypeNames(typeList);
		query.set(filterDocument);
		getFeature.set(queryDoc);

		System.out.println(getFeatureDoc.xmlText());

		StringBuffer wfsRequest = new StringBuffer();
		// GetFeature-tag
		wfsRequest.append(
				"<wfs:GetFeature service=\"WFS\" version=\"" + version + "\" outputFormat=\"" + outputFormat + "\" ");
		wfsRequest.append(xmlns);
		// Query-tag
		wfsRequest.append("<wfs:Query typeNames=\"" + "CDC:VGSL_" + typeName + "\">");
		wfsRequest.append("<fes:Filter><fes:And>"); // Filter
		wfsRequest.append("<fes:BBOX>" + "                <fes:ValueReference>CDC:GEOM</fes:ValueReference>"
				+ "                <gml:Envelope srsName=\"urn:ogc:def:crs:EPSG::4326\">"
				+ "                    <gml:lowerCorner>" + bbox.get(0) + " " + bbox.get(1) + "</gml:lowerCorner>"
				+ "                    <gml:upperCorner>" + bbox.get(2) + " " + bbox.get(3) + "</gml:upperCorner>"
				+ "                </gml:Envelope>" + "            </fes:BBOX>"); // BBOX
		wfsRequest.append(
				" <fes:PropertyIsBetween>" + "            	<fes:ValueReference>CDC:ZEITSTEMPEL</fes:ValueReference>"
						+ "            	<fes:Literal>" + formatter.print(startDate) + "</fes:Literal>" + // 2019-06-02T06:00:00Z
						"            	<fes:Literal>" + formatter.print(endDate) + "</fes:Literal>" + // 2019-06-02T10:00:00Z
						"            </fes:PropertyIsBetween>"); // timeFrame
		wfsRequest.append(" </fes:And></fes:Filter></wfs:Query>"); // end query
		wfsRequest.append("</wfs:GetFeature>"); // end getFeature

		return wfsRequest.toString();
	}

}
