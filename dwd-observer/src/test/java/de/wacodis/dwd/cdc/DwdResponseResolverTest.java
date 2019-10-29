package de.wacodis.dwd.cdc;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

class DwdResponseResolverTest {

	private static DwdWfsRequestParams params;
	private static DwdResponseResolver resolver;
	private InputStream capResponse;
	private InputStream getFeatureResponse;

	@BeforeAll
	static void setUp() {
		// params
		params = new DwdWfsRequestParams();
		params.setVersion("2.0.0");
		params.setTypeName("FX_MN003");

		List<Float> bounds = new ArrayList<Float>();
		bounds.add(0, 51.0000f);
		bounds.add(1, 6.6000f);
		bounds.add(2, 51.5000f);
		bounds.add(3, 7.3000f);
		params.setBbox(bounds);

		DateTime startDate = DateTime.parse("2019-04-24T01:00:00Z", DwdWfsRequestorBuilder.FORMATTER);
		DateTime endDate = DateTime.parse("2019-04-25T10:00:00Z", DwdWfsRequestorBuilder.FORMATTER);

		params.setStartDate(startDate);
		params.setEndDate(endDate);
		
		resolver = new DwdResponseResolver(params);
		
		
	}

	@Test
	void testRequestTypeName() throws ParserConfigurationException, SAXException, IOException {
		// actual
		InputStream getCapabilitiesStream = this.getClass().getResourceAsStream("/getCapabilities-test.xml");
		String[] typenameArray = resolver.requestTypeName(getCapabilitiesStream);
		String name = typenameArray[0];
		String typeName = typenameArray[1];
		// expected
		String expectedName = resolver.typename;
		String expectedTypeName = "Tägliche Stationsmessungen der maximalen Windspitze in ca. 10 m Höhe in m/s";
	
		Assertions.assertEquals(expectedName, name);
		Assertions.assertEquals(expectedTypeName, typeName);
	}

	@Test
	void testGenerateSpatioTemporalExtent() throws IOException, SAXException, ParserConfigurationException {
		// actual
		InputStream getFeatureResponse = this.getClass().getResourceAsStream("/getFeatureResult-test.xml");
		SpatioTemporalExtent timeAndBBox = resolver.generateSpatioTemporalExtent(getFeatureResponse);
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

}
