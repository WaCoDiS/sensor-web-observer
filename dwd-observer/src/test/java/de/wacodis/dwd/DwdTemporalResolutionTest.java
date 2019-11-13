package de.wacodis.dwd;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DwdTemporalResolutionTest {

	
	@Test
	void testCalculateStartAndEndDate() {
		int resolution = DwdTemporalResolutionHelper.HOURLY_RESOLUTION;
		
		String durationISO = "P2Y5M4W26DT8H57M";
		Period period = Period.parse(durationISO, ISOPeriodFormat.standard());
		DateTime endDate = DateTime.now();
		DateTime startDate = endDate.withPeriodAdded(period, -1);
		DateTime endDate2 = startDate.plusMinutes((int) (24*7*60));
		List<DateTime[]> interval = DwdTemporalResolutionHelper.calculateRequestIntervalsForResolution(startDate, endDate, resolution);
		
		Assertions.assertEquals(startDate.toLocalDate(), interval.get(0)[0].toLocalDate());
		Assertions.assertEquals(endDate2.toLocalDate(), interval.get(0)[1].toLocalDate());
	}

}
