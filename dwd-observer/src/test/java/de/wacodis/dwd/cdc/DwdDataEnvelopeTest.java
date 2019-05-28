package de.wacodis.dwd.cdc;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.wacodis.api.model.DwdDataEnvelope;

public class DwdDataEnvelopeTest {
	static DwdProductsMetadata metadata;
	@BeforeAll
	static void setup() throws ParseException {

		metadata = new DwdProductsMetadata();
		metadata.setServiceUrl("https://cdc.dwd.de:443/geoserver/CDC/wfs?");
		metadata.setLayername("CDC:VGSL_FX_MN003");
		metadata.setParameter("Tägliche Stationsmessungen der maximalen Windspitze in ca. 10 m Höhe in m/s");
		List<Float> extent = new ArrayList<Float>();
		extent.add(51.402f);
		extent.add(6.966f);
		extent.add(51.405f);
		extent.add(6.969f);
		metadata.setExtent(extent);
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SS'Z'");
		Date startDate = df.parse("2019-04-24T01:00:00:00Z");
		Date endDate = df.parse("2019-04-25T10:00:00:00Z");
		metadata.setStartDate(startDate);
		metadata.setEndDate(endDate);
		
	}
	
	@DisplayName("Test DwdDataEnvelope")
	@Test
	void testDataEnvelope() {
		DwdDataEnvelope envelope = DwdProductsMetadataDecoder.decode(metadata);
		
		Assertions.assertEquals(envelope.getServiceUrl(), metadata.getServiceUrl());
		Assertions.assertEquals(envelope.getLayerName(), metadata.getLayername());
		Assertions.assertEquals(envelope.getParameter(), metadata.getParameter());
		//Assertions.assertEquals(envelope.getAreaOfInterest().getExtent(), metadata.getExtent());
		Assertions.assertEquals(envelope.getTimeFrame().getEndTime(), metadata.getStartDate());
		//Assertions.assertEquals(envelope.getTimeFrame().getEndTime(), metadata.getEndDate());
		
	}
}
