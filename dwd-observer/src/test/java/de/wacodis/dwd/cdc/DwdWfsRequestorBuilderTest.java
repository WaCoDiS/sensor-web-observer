package de.wacodis.dwd.cdc;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class DwdHtmlReaderTest {

	static DwdHtmlReader reader;

	@BeforeAll
	static void setup() {

		String propUrl = "https://cdc.dwd.de/geoserver/CDC/wfs";
		String version = "2.0.0";
		String typeName = "CDC:VGSL_FX_MN003";
		List<String> bbox = new ArrayList<String>();
		bbox.add("51.200");
		bbox.add("6.700");
		bbox.add("51.500");
		bbox.add("7.300");
		DateTime startDate = DateTime.parse("2019-03-02T06:00:00Z", DwdHtmlReader.formatter);
		DateTime endDate = DateTime.parse("2019-06-02T06:00:00Z", DwdHtmlReader.formatter);

		String outputFormat = "json";

		reader = new DwdHtmlReader(propUrl, version, typeName, bbox, startDate, endDate, outputFormat);
	}

	@Test
	void test() throws ClientProtocolException, IOException {
		InputStream result = reader.createWfsRequestPost();
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> jsonMap = mapper.readValue(result, Map.class);
		ArrayList<LinkedHashMap<String, String>> resultList = (ArrayList<LinkedHashMap<String, String>>) jsonMap.get("features");
		LinkedHashMap<String, String> firstFeature =  resultList.get(0);
		String id = firstFeature.get("id");
		// LinkedHashMap<String, String> geomType = new LinkedHashMap<String, String>();
		// geomType.put("geometry", firstFeature.get("geometry"));
		Assertions.assertEquals("VGSL_FX_MN003.194212664429445290741059517461184742984", id);
		//Assertions.assertEquals(expected, actual);
	}

}
