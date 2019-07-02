package de.wacodis.dwd.cdc;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.wacodis.api.model.AbstractDataEnvelopeAreaOfInterest;
import de.wacodis.api.model.WacodisJobDefinition;

public class DwdWfsRequestParamsEncoderTest {

	private static String version;
	private static String typeName;
	private static WacodisJobDefinition jobDefinition = new WacodisJobDefinition();
	private static DateTime startDate;
	private static DateTime endDate;

	@BeforeAll
	static void setup() throws ParseException {
		version = "2.0.0";
		typeName = "CDC:VGSL_FX_MN003";
		AbstractDataEnvelopeAreaOfInterest area = new AbstractDataEnvelopeAreaOfInterest();
		List<Float> extent = new ArrayList<Float>();
		extent.add(51.402f);
		extent.add(6.966f);
		extent.add(51.405f);
		extent.add(6.969f);
		area.setExtent(extent);
		jobDefinition.setAreaOfInterest(area);

		DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss:SS'Z'");
		startDate = DateTime.parse("2019-04-24T01:00:00:00Z", df);
		endDate = DateTime.parse("2019-04-25T10:00:00:00Z", df);

	}

	@DisplayName("Test DWD Params Encoder Method")
	@Test
	void testEncodeParams() throws Exception {
		DwdWfsRequestParams params = DwdRequestParamsEncoder.encode(version, typeName, jobDefinition, startDate, endDate);

		Assertions.assertEquals(version, params.getVersion());
		Assertions.assertEquals(typeName, params.getTypeName());
		Assertions.assertEquals(startDate, params.getStartDate());
		Assertions.assertEquals(endDate, params.getEndDate());
		Assertions.assertEquals((double) jobDefinition.getAreaOfInterest().getExtent().get(0), params.getBbox().getMinY());
		Assertions.assertEquals((double) jobDefinition.getAreaOfInterest().getExtent().get(1), params.getBbox().getMinX());
		Assertions.assertEquals((double) jobDefinition.getAreaOfInterest().getExtent().get(2), params.getBbox().getMaxY());
		Assertions.assertEquals((double) jobDefinition.getAreaOfInterest().getExtent().get(3), params.getBbox().getMaxX());
	}


}