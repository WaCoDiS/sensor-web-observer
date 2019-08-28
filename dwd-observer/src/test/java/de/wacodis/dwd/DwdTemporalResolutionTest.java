package de.wacodis.dwd;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

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
		DateTime endDate = DateTime.now();
		DateTime startDate = endDate.withPeriodAdded(period, -1);
		DateTime endDate2 = startDate.plusMinutes((int) (24*7*60));
		List<DateTime[]> interval = DwdTemporalResolution.calculateStartAndEndDate(startDate, endDate, resolution);
		
		Assertions.assertEquals(startDate.toLocalDate(), interval.get(0)[0].toLocalDate());
		Assertions.assertEquals(endDate2.toLocalDate(), interval.get(0)[1].toLocalDate());
	}

}
