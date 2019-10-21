package de.wacodis.dwd.cdc;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DwdWfsRequestorIT {

	private static DwdWfsRequestParams params;
	private static DwdWfsRequestorBuilder reader;
	private static String serviceUrl = "https://cdc.dwd.de:443/geoserver/CDC/wfs?";

	@BeforeAll
	static void setup() throws ParseException {
		
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

	@DisplayName("Test request Method")
	@Test
	void testRequest() throws IOException, ParserConfigurationException, SAXException {
		DwdProductsMetadata result = new DwdProductsMetadata();
		result = DwdWfsRequestor.request(serviceUrl, params);

		// object of comparison
		DwdProductsMetadata metadata = new DwdProductsMetadata();

		metadata.setServiceUrl(serviceUrl);
		metadata.setLayername("CDC:VGSL_FX_MN003");
		metadata.setParameter("Tägliche Stationsmessungen der maximalen Windspitze in ca. 10 m Höhe in m/s");

		ArrayList<Float> extent = new ArrayList<Float>();

		extent.add(0, 6.7686f);
		extent.add(1, 51.2531f);
		extent.add(2, 7.2156f);
		extent.add(3, 51.4041f);
		metadata.setExtent(extent);

		DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss:SS'Z'");
		DateTime startDate = DateTime.parse("2010-04-24T02:00:00:00Z", df);
		DateTime endDate = DateTime.parse("2019-04-25T02:00:00:00Z", df);
		metadata.setStartDate(startDate);
		metadata.setEndDate(endDate);

		// Comparison
		// ServiceURL
		Assertions.assertEquals(metadata.getServiceUrl(), result.getServiceUrl());
		// LayerName
		Assertions.assertEquals(metadata.getLayername(), result.getLayername());
		// ClearName
		Assertions.assertEquals(metadata.getParameter(), result.getParameter());
		// bbox
		Assertions.assertEquals(metadata.getExtent(), result.getExtent());
		// time
		// is the startDate of the feature timeframe between the query startDate and
		// endDate
		int biggerThanStartDate = result.getStartDate().compareTo(metadata.getStartDate());
		int lowerThanEndDate = result.getStartDate().compareTo(metadata.getEndDate());
		Assertions.assertTrue(biggerThanStartDate >= 0 && lowerThanEndDate <= 0);
		// is the endDate of the feature timeframe between the query startDate and
		// endDate
		biggerThanStartDate = result.getEndDate().compareTo(metadata.getStartDate());
		lowerThanEndDate = result.getEndDate().compareTo(metadata.getEndDate());
		Assertions.assertTrue(biggerThanStartDate >= 0 && lowerThanEndDate <= 0);

	}

	@DisplayName("Test correct throwing of Exceptions")
	@Test
	void testExceptions() {
		// request
		Assertions.assertDoesNotThrow(() -> {
			DwdWfsRequestor.request(serviceUrl, params);
		});

	}

	@Test
	void test() throws ClientProtocolException, IOException {
		String message = reader.createXmlPostMessage().xmlText();
		InputStream result = DwdWfsRequestor.sendWfsRequest(serviceUrl, message);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> jsonMap = mapper.readValue(result, Map.class);
		ArrayList<LinkedHashMap<String, String>> resultList = (ArrayList<LinkedHashMap<String, String>>) jsonMap
				.get("features");
		LinkedHashMap<String, String> firstFeature = resultList.get(0);
		String id = firstFeature.get("id");
		// LinkedHashMap<String, String> geomType = new LinkedHashMap<String, String>();
		// geomType.put("geometry", firstFeature.get("geometry"));
		Assertions.assertDoesNotThrow(() -> {
			DwdWfsRequestor.sendWfsRequest(serviceUrl, message);
		});
		// Assertions.assertEquals(expected, actual);
	}

}