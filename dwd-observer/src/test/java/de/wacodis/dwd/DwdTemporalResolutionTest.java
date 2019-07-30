package de.wacodis.dwd;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISOPeriodFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.wacodis.dwd.cdc.DwdProductsMetadata;

class DwdTemporalResolutionTest {

	
	@Test
	void testCalculateStartAndEndDate() {
		int resolution = DwdTemporalResolution.HOURLY_RESOLUTION;
		
		String durationISO = "P2Y5M4W26DT8H57M";
		Period period = Period.parse(durationISO, ISOPeriodFormat.standard());
		
		ArrayList<DateTime> interval = DwdTemporalResolution.calculateStartAndEndDate(period, resolution);
		
		Assertions.assertEquals(DateTime.now().toLocalDate(), interval.get(interval.size()-1).toLocalDate());
		Assertions.assertEquals(DateTime.now().getHourOfDay(), interval.get(interval.size()-1).getHourOfDay());
	}

}
