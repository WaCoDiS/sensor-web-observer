package de.wacodis.dwd.cdc;

import java.text.ParseException;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.wacodis.observer.model.DwdDataEnvelope;

public class DwdDataEnvelopeTest {
	
	// dummy object
	static DwdProductsMetadata metadata;
	@BeforeAll
	static void setup() throws ParseException {

		// fill dummy object
		metadata = new DwdProductsMetadata();
		metadata.setServiceUrl("https://cdc.dwd.de:443/geoserver/CDC/wfs?");
		metadata.setLayerName("CDC:VGSL_FX_MN003");
		metadata.setParameter("Tägliche Stationsmessungen der maximalen Windspitze in ca. 10 m Höhe in m/s");
		
		// bbox
		ArrayList<Float> extent = new ArrayList<Float>();
		extent.add(51.402f);
		extent.add(6.966f);
		extent.add(51.405f);
		extent.add(6.969f);
		metadata.setExtent(extent);

		// time frame
		DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss:SS'Z'");
		DateTime startDate = DateTime.parse("2019-04-24T01:00:00:00Z", df);
		DateTime endDate = DateTime.parse("2019-04-25T10:00:00:00Z", df);
		metadata.setStartDate(startDate);
		metadata.setEndDate(endDate);

	}

	@DisplayName("Test DwdDataEnvelope")
	@Test
	void testDataEnvelope() {
		// test if DwdDataEnvelope has the same content
		DwdDataEnvelope envelope = DwdProductsMetadataDecoder.decode(metadata);

		// comparison
		Assertions.assertEquals(envelope.getServiceUrl(), metadata.getServiceUrl());
		Assertions.assertEquals(envelope.getLayerName(), metadata.getLayerName());
		Assertions.assertEquals(envelope.getParameter(), metadata.getParameter());
		Assertions.assertEquals(envelope.getAreaOfInterest().getExtent(), metadata.getExtent());
		Assertions.assertEquals(envelope.getTimeFrame().getEndTime(), metadata.getEndDate());
		Assertions.assertEquals(envelope.getTimeFrame().getEndTime(), metadata.getEndDate());

	}
}