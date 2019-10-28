package de.wacodis.dwd.cdc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.xmlbeans.XmlException;
import org.jdom2.JDOMException;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.opengis.wfs.x20.GetFeatureDocument;

class DwdWfsRequestorBuilderTest {

	private static DwdWfsRequestParams params;
	private static DwdWfsRequestorBuilder reader;
	ArrayList<String> valuesFile = new ArrayList<String>();
	ArrayList<String> valuesCalculated = new ArrayList<String>();

	@BeforeAll
	static void setup() {

		params = new DwdWfsRequestParams();
		params.setVersion("2.0.0");
		params.setTypeName("FX_MN003");
		params.setOutputFormat("json");
		// params.setTypeName("CDC:VGSL_TT_TU_MN009");

		List<Float> bounds = new ArrayList<Float>();
		bounds.add(0, 51.0000f);
		bounds.add(1, 6.6000f);
		bounds.add(2, 51.5000f);
		bounds.add(3, 7.3000f);
		params.setBbox(bounds);

		DateTime startDate = DateTime.parse("2019-04-24T01:00:00Z", DwdWfsRequestorBuilder.formatter);
		DateTime endDate = DateTime.parse("2019-04-25T10:00:00Z", DwdWfsRequestorBuilder.formatter);

		params.setStartDate(startDate);
		params.setEndDate(endDate);
		params.setOutputFormat("json");

		reader = new DwdWfsRequestorBuilder(params);

	}

	@DisplayName("Test builder Method")
	@Test
	void testBuilder() throws JDOMException, IOException, XmlException, ParserConfigurationException, SAXException {

		GetFeatureDocument result = reader.createGetFeaturePost();

		InputStream postMessage = this.getClass().getResourceAsStream("/postmessage-test.xml");
		GetFeatureDocument gfdoc = GetFeatureDocument.Factory.parse(postMessage);

		iterateNodes(gfdoc.getGetFeature().getDomNode(), valuesFile);
		iterateNodes(result.getGetFeature().getDomNode(), valuesCalculated);

		Assertions.assertTrue(valuesFile.size() == valuesCalculated.size());
		for (int i = 0; i < valuesFile.size(); i++) {
			Assertions.assertEquals(valuesFile.get(i), valuesCalculated.get(i));
		}
	}

	private ArrayList<String> iterateNodes(Node node, ArrayList<String> values) {
		if (node.hasChildNodes()) {
			NodeList nodeList = node.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node innerNode = nodeList.item(i);
				iterateNodes(innerNode, values);

			}
		} else {
			values.add(node.getNodeValue());
		}

		return values;
	}

}
