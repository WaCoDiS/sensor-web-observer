package de.wacodis.dwd.cdc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.gml.x32.EnvelopeDocument;
import net.opengis.gml.x32.EnvelopeType;
import net.opengis.gml.x32.impl.EnvelopeDocumentImpl;
import net.opengis.wfs.x20.EnvelopePropertyType;
import net.opengis.wfs.x20.FeatureCollectionDocument;
import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class DwdResponseResolverTest {

	private static DwdResponseResolver resolver;
	private String typeName = DwdWfsRequestorBuilder.TYPE_NAME_PREFIX + "FX_MN003";

	@BeforeAll
	static void setUp() {
		resolver = new DwdResponseResolver();
	}

	@Test
	void testRequestTypeName() throws ParserConfigurationException, SAXException, IOException {
		// actual
		InputStream getCapabilitiesStream = this.getClass().getResourceAsStream("/getCapabilities-test.xml");
		String[] typenameArray = resolver.requestTypeName(getCapabilitiesStream, typeName);
		String name = typenameArray[0];
		String title = typenameArray[1];
		
		// expected
		String expectedName = name;
		String expectedTitle = "Tägliche Stationsmessungen der maximalen Windspitze in ca. 10 m Höhe in m/s";

		Assertions.assertEquals(expectedTitle, title);
		Assertions.assertEquals(expectedName, name);
	}

//	@Test
	void testGenerateSpatioTemporalExtent() throws IOException, SAXException, ParserConfigurationException, XmlException {
		// actual
		InputStream getFeatureResponse = this.getClass().getResourceAsStream("/getFeatureResult-test.xml");

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbf.newDocumentBuilder();
		Document doc = docBuilder.parse(getFeatureResponse);

		FeatureCollectionDocument featureDoc = FeatureCollectionDocument.Factory.parse(getFeatureResponse);
		EnvelopePropertyType envelopePropertyType = featureDoc.getFeatureCollection().getBoundedBy();
		EnvelopeType env = (EnvelopeType)envelopePropertyType.changeType(EnvelopeDocument.type);

		EnvelopeDocument envDocument = EnvelopeDocument.Factory.parse(featureDoc.getFeatureCollection().getBoundedBy().xmlText());

		String boundedByText = featureDoc.getFeatureCollection().getBoundedBy().getDomNode().getTextContent();
		NodeList nodes = featureDoc.getFeatureCollection().getBoundedBy().getDomNode().getChildNodes();
		int nodeLength = nodes.getLength();

		SpatioTemporalExtent timeAndBBox = resolver.generateSpatioTemporalExtent(doc, typeName);
		ArrayList<Float> bBox = timeAndBBox.getbBox();
		ArrayList<DateTime> timeFrame = timeAndBBox.getTimeFrame();

		// expected
		ArrayList<Float> expectedBBox = new ArrayList<Float>();
		expectedBBox.add(6.7686f);
		expectedBBox.add(51.2531f);
		expectedBBox.add(7.2156f);
		expectedBBox.add(51.4041f);
		ArrayList<DateTime> expectedTimeFrame = new ArrayList<DateTime>();
		DateTime expectedStartDate = DateTime.parse("2019-04-25T00:00:00Z", DwdWfsRequestorBuilder.FORMATTER);
		DateTime expectedEndDate = DateTime.parse("2019-04-25T00:00:00Z", DwdWfsRequestorBuilder.FORMATTER);
		expectedTimeFrame.add(expectedStartDate);
		expectedTimeFrame.add(expectedEndDate);

		// comparison
		Assertions.assertEquals(expectedBBox, bBox);
		Assertions.assertEquals(expectedTimeFrame, timeFrame);
	}

	@Test
	void responseContainsFeatureCollectionForValidResponse() throws ParserConfigurationException, IOException, SAXException {
		InputStream getFeatureResponse = this.getClass().getResourceAsStream("/getFeatureResult-test.xml");

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbf.newDocumentBuilder();
		Document doc = docBuilder.parse(getFeatureResponse);

		Assertions.assertTrue(resolver.responseContainsFeatureCollection(doc));
	}

	@Test
	void responseContainsFeatureCollectionForEmptyResponse() throws ParserConfigurationException, IOException, SAXException {
		InputStream getFeatureResponse = this.getClass().getResourceAsStream("/getFeatureResultEmpty-test.xml");

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbf.newDocumentBuilder();
		Document doc = docBuilder.parse(getFeatureResponse);

		Assertions.assertFalse(resolver.responseContainsFeatureCollection(doc));
	}

}
