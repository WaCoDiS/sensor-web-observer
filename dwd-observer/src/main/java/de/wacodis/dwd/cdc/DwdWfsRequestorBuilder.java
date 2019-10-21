package de.wacodis.dwd.cdc;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import net.opengis.fes.x20.AndDocument;
import net.opengis.fes.x20.BBOXDocument;
import net.opengis.fes.x20.BBOXType;
import net.opengis.fes.x20.BinaryLogicOpType;
import net.opengis.fes.x20.FilterDocument;
import net.opengis.fes.x20.FilterType;
import net.opengis.fes.x20.LiteralDocument;
import net.opengis.fes.x20.LiteralType;
import net.opengis.fes.x20.PropertyIsBetweenDocument;
import net.opengis.fes.x20.PropertyIsBetweenType;
import net.opengis.fes.x20.ValueReferenceDocument;
import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.EnvelopeDocument;
import net.opengis.gml.x32.EnvelopeType;
import net.opengis.wfs.x20.GetCapabilitiesDocument;
import net.opengis.wfs.x20.GetCapabilitiesType;
import net.opengis.wfs.x20.GetFeatureDocument;
import net.opengis.wfs.x20.GetFeatureType;
import net.opengis.wfs.x20.QueryDocument;
import net.opengis.wfs.x20.QueryType;

public class DwdWfsRequestorBuilder {

	// class attributes
	public static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
	public static final String typeNamePrefix = "CDC:VGSL_";
	public static final String geometryAttribute = "CDC:GEOM";
	public static final String timestampAttribute = "CDC:ZEITSTEMPEL";
	public static final String epsgUrn = "urn:ogc:def:crs:EPSG::4326";
	public static final String serviceAttribute = "WFS";

	// attributes
	String version;// &version=2.0.0
	String typeName;// &typeName=CDC%3AVGSL_FX_MN003
	List<String> bbox = new ArrayList<String>();// &bbox=51.200,6.700,51.500,7.300
	DateTime startDate;
	DateTime endDate;
	String outputFormat;// &outputformat=application%2Fjson
	ArrayList<String[]> namespaces = new ArrayList<String[]>();
	ArrayList<String[]> attributes = new ArrayList<String[]>();

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

	public GetFeatureDocument createXmlPostMessage() {

		createXmlAttributesList();

		// <GetFeature>
		GetFeatureDocument getFeatureDoc = GetFeatureDocument.Factory.newInstance();
		GetFeatureType getFeature = getFeatureDoc.addNewGetFeature();

		// <Query>
		QueryDocument queryDoc = QueryDocument.Factory.newInstance();
		QueryType query = queryDoc.addNewQuery();
		ArrayList<String> typeList = new ArrayList<String>();
		typeList.add(typeNamePrefix + typeName);

		// <Filter>
		FilterDocument filterDocument = FilterDocument.Factory.newInstance();
		FilterType filter = filterDocument.addNewFilter();

		// <And>
		AndDocument andDocument = AndDocument.Factory.newInstance();
		BinaryLogicOpType andType = andDocument.addNewAnd();

		// <BBOX>
		BBOXType bboxType = createBboxElement();
		// </BBOX>
		addXSAnyElement(andType, bboxType);

		// <PropertyIsBetween>
		PropertyIsBetweenType propBetweenType = createPropertyIsBetweenElement();
		// </PropertyIsBetween>
		addXSAnyElement(andType, propBetweenType);

		// </And>
		filter.set(andDocument);
		// </Filter>
		query.set(filterDocument);
		// </Query>
		query.setTypeNames(typeList);
		// </GetFeature>
		getFeature.set(queryDoc);

		// Attributes for <GetFeature>
		// namespaces
		addAttributesToElement(getFeature);
		return getFeatureDoc;
	}

	private void addAttributesToElement(GetFeatureType getFeature) {
		XmlCursor cursor = getFeature.newCursor();
		cursor.toNextToken();
		for (int i = 0; i < namespaces.size(); i++) {
			cursor.insertNamespace(namespaces.get(i)[0], namespaces.get(i)[1]);
		}
		for (int i = 0; i < attributes.size(); i++) {
			cursor.insertAttributeWithValue(attributes.get(i)[1], attributes.get(i)[0], attributes.get(i)[2]);
		}
		cursor.dispose();
	}

	private PropertyIsBetweenType createPropertyIsBetweenElement() {

		PropertyIsBetweenDocument propBetweenDocument = PropertyIsBetweenDocument.Factory.newInstance();
		PropertyIsBetweenType propBetweenType = propBetweenDocument.addNewPropertyIsBetween();

		// <ValueReference>
		ValueReferenceDocument valueRefDocument2 = ValueReferenceDocument.Factory.newInstance();
		valueRefDocument2.setValueReference(timestampAttribute);
		// </ValueReference>

		// <Literal>
		LiteralDocument litDoc = LiteralDocument.Factory.newInstance();
		LiteralType litType1 = litDoc.addNewLiteral();
		XmlString xmlString = XmlString.Factory.newValue(formatter.print(startDate));
		litType1.set(xmlString);
		// </Literal>

		// <Literal>
		LiteralDocument litDoc2 = LiteralDocument.Factory.newInstance();
		LiteralType litType2 = litDoc2.addNewLiteral();
		XmlString xmlString2 = XmlString.Factory.newValue(formatter.print(endDate));
		litType2.set(xmlString2);
		// </Literal>

		addXSAnyElement(propBetweenType, litType2);
		addXSAnyElement(propBetweenType, litType1);

		addXSAnyElement(propBetweenType, valueRefDocument2.getExpression());
		return propBetweenType;
	}

	private BBOXType createBboxElement() {

		BBOXDocument bboxDocument = BBOXDocument.Factory.newInstance();
		BBOXType bboxType = bboxDocument.addNewBBOX();

		// <ValueReference>
		ValueReferenceDocument valueRefDocument = ValueReferenceDocument.Factory.newInstance();
		valueRefDocument.setValueReference(geometryAttribute);
		// </ValueReference>

		// <Envelope>
		EnvelopeDocument envDoc = EnvelopeDocument.Factory.newInstance();
		EnvelopeType envType = envDoc.addNewEnvelope();
		envType.setSrsName(epsgUrn);
		DirectPositionType lowerCorner = envType.addNewLowerCorner();
		lowerCorner.setStringValue(bbox.get(0) + " " + bbox.get(1));
		DirectPositionType upperCorner = envType.addNewUpperCorner();
		upperCorner.setStringValue(bbox.get(2) + " " + bbox.get(3));
		// </Envelope>
		addXSAnyElement(bboxType, envType);
		addXSAnyElement(bboxType, valueRefDocument.getExpression());
		return bboxType;
	}

	protected void addXSAnyElement(XmlObject target, XmlObject newChild) {
		XmlCursor childCur = newChild.newCursor();

		XmlCursor targetCur = target.newCursor();
		targetCur.toNextToken();

		childCur.moveXml(targetCur);

		childCur.dispose();
		targetCur.dispose();
	}

	private void createXmlAttributesList() {
		// namespaces
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
		// attributes
		attributes.add(new String[] { "xsi", "schemaLocation",
				"http://www.opengis.net/wfs/2.0 https://cdc.dwd.de:443/geoserver/schemas/wfs/2.0/wfs.xsd http://inspire.ec.europa.eu/schemas/inspire_dls/1.0 http://inspire.ec.europa.eu/schemas/inspire_dls/1.0/inspire_dls.xsd" });
		attributes.add(new String[] { "", "service", serviceAttribute });
		attributes.add(new String[] { "", "version", this.version });
		attributes.add(new String[] { "", "outputFormat", this.outputFormat });
	}

	public GetCapabilitiesDocument createCapabilitiesPost() {
		GetCapabilitiesDocument getCapDoc = GetCapabilitiesDocument.Factory.newInstance();
		GetCapabilitiesType getCapType = getCapDoc.addNewGetCapabilities();
		getCapType.setService(serviceAttribute);

		return getCapDoc;
	}
}
