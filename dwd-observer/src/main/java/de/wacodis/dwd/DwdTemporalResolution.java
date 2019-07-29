package de.wacodis.dwd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Period;

import de.wacodis.observer.model.DwdDataEnvelope;

public class DwdTemporalResolution {

	// Enumerations of temporal resolution
	// {average Temp., precipitation, air pressure, air humidity, cloud coverage}
	public static Set hourly = new HashSet<>(
			Arrays.asList("TT_TU_MN009", "R1_MN008", "P0_MN008", "RF_TU_MN009", "N_MN008"));
	// {average Temp., max temp, min temp, precipitation, wind top, air pressure,
	// snow height, fresh snow height, sunshine duration, air humidity, cloud
	// coverage}
	public static Set daily = new HashSet<>(Arrays.asList("TMK_MN004", "TXK_MN004", "TNK_MN004", "RS_MN006", "FX_MN003",
			"PM_MN004", "SH_TAG_MN006", "NSH_TAG_MN006", "SDK_MN004", "UPM_MN004", "NM_MN004"));
	// {average Temp., max temp, min temp, precipitation, air pressure, snow height,
	// fresh snow height, sunshine duration, air humidity, cloud coverage}
	public static Set monthly = new HashSet<>(Arrays.asList("MO_TT_MN004", "MO_TX_MN004", "MO_TN_MN004", "MO_RR_MN006",
			"MO_P0_MN004", "MO_SH_S_MN006", "MO_NSH_MN006", "MO_SD_S_MN004", "MO_RF_MN004", "MO_N_MN004"));
	// {average Temp., max temp, min temp, precipitation, air pressure, snow height,
	// fresh snow height, sunshine duration, air humidity, cloud coverage}
	public static Set annual = new HashSet<>(Arrays.asList("JA_TT_MN004", "JA_TX_MN004", "JA_TN_MN004", "JA_RR_MN006",
			"JA_P0_MN004", "JA_SH_S_MN006", "JA_NSH_MN006", "JA_SD_S_MN004", "JA_RF_MN004", "JA_N_MN004"));

	// constants for interval calculation
	public static final int HOURLY_RESOLUTION = 0;
	public static final int DAILY_RESOLUTION = 1;
	public static final int MONTHLY_RESOLUTION = 2;

	public static boolean isHourly(String layerName) {
		return hourly.contains(layerName);
	}

	public static boolean isDaily(String layerName) {
		return daily.contains(layerName);
	}

	public static boolean isMonthly(String layerName) {
		return monthly.contains(layerName);
	}

	public static boolean isAnnual(String layerName) {
		return annual.contains(layerName);
	}

	public static int calculateInterval(double hourSum, int resolution) {
		if (resolution == DwdTemporalResolution.HOURLY_RESOLUTION) {
			return (int) (hourSum / (24 * 7)); // splitting duration in week blocks
		}
		if (resolution == DwdTemporalResolution.DAILY_RESOLUTION) {
			return (int) (hourSum / (24 * 30)); // splitting duration in month blocks
		}
		if (resolution == DwdTemporalResolution.MONTHLY_RESOLUTION) {
			return (int) (hourSum/(24 * 365 * 10)); // splitting duration in 10 years blocks
		}
		else {
			return 1;
		}
	}

	public static ArrayList<DateTime> calculateStartAndEndDate(Period period, int resolution) {
		ArrayList<DateTime> outputList = new ArrayList<DateTime>();

		double hourSum = period.getHours() + period.getDays() * 24 + period.getWeeks() * 24 * 7
				+ period.getMonths() * 30.436857 * 24 + period.getYears() * 365.2425 * 24;
		int interval = DwdTemporalResolution.calculateInterval(hourSum, resolution);

		DateTime startDate = DateTime.now().minusHours((int) hourSum);
		DateTime endDate = DateTime.now();

		// duration longer than one week
		if (interval > 1) {

			for (int i = 0; i < interval; i++) {
				startDate = DateTime.now().minusHours((int) hourSum);
				endDate = startDate.plusHours((int) (hourSum / interval));
			}
		}
		outputList.add(startDate);
		outputList.add(endDate);
		return outputList;

	}
}
