package de.wacodis.dwd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Hours;

/**
 * Helper class for dealing with different time resolutions of the various DWD WFS layers
 */
public class DwdTemporalResolutionHelper {

    // Enumerations of temporal resolution
    // {average Temp., precipitation, air pressure, air humidity, cloud coverage}
    private static Set hourly = new HashSet<>(
            Arrays.asList("TT_TU_MN009", "R1_MN008", "P0_MN008", "RF_TU_MN009", "N_MN008"));
    // {average Temp., max temp, min temp, precipitation, wind top, air pressure,
    // snow height, fresh snow height, sunshine duration, air humidity, cloud
    // coverage}
    private static Set daily = new HashSet<>(Arrays.asList("TMK_MN004", "TXK_MN004", "TNK_MN004", "RS_MN006",
            "FX_MN003", "PM_MN004", "SH_TAG_MN006", "NSH_TAG_MN006", "SDK_MN004", "UPM_MN004", "NM_MN004"));
    // {average Temp., max temp, min temp, precipitation, air pressure, snow height,
    // fresh snow height, sunshine duration, air humidity, cloud coverage}
    private static Set monthly = new HashSet<>(Arrays.asList("MO_TT_MN004", "MO_TX_MN004", "MO_TN_MN004", "MO_RR_MN006",
            "MO_P0_MN004", "MO_SH_S_MN006", "MO_NSH_MN006", "MO_SD_S_MN004", "MO_RF_MN004", "MO_N_MN004"));
    // {average Temp., max temp, min temp, precipitation, air pressure, snow height,
    // fresh snow height, sunshine duration, air humidity, cloud coverage}
    private static Set annual = new HashSet<>(Arrays.asList("JA_TT_MN004", "JA_TX_MN004", "JA_TN_MN004", "JA_RR_MN006",
            "JA_P0_MN004", "JA_SH_S_MN006", "JA_NSH_MN006", "JA_SD_S_MN004", "JA_RF_MN004", "JA_N_MN004"));

    // constants for interval calculation
    public static final int HOURLY_RESOLUTION = 0;
    public static final int DAILY_RESOLUTION = 1;
    public static final int MONTHLY_RESOLUTION = 2;
    public static final int ANNUAL_RESOLUTION = 3;

    public static final int ONE_WEEK_INTERVAL = 24 * 7;
    public static final int ONE_MONTH_INTERVAL = 24 * 30;
    public static final int TEN_YEARS_INTERVAL = 24 * 365 * 10;

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

    public static double calculateInterval(int hourSum, int resolution) {
        double hourSumD = (double) hourSum;
        if (resolution == DwdTemporalResolutionHelper.HOURLY_RESOLUTION) {
            return (hourSumD / DwdTemporalResolutionHelper.ONE_WEEK_INTERVAL); // splitting duration in three days blocks
        }
        if (resolution == DwdTemporalResolutionHelper.DAILY_RESOLUTION) {
            return (hourSumD / DwdTemporalResolutionHelper.ONE_MONTH_INTERVAL); // splitting duration in month blocks
        }
        if (resolution == DwdTemporalResolutionHelper.MONTHLY_RESOLUTION) {
            return (hourSumD / DwdTemporalResolutionHelper.TEN_YEARS_INTERVAL); // splitting duration in 10 years blocks
        } else {
            return 1;
        }
    }

    /**
     * Gets the request intervals for requesting a certain WFS request layer
     *
     * @param startDate requested start date
     * @param endDate   requested end date
     * @param layerName name of the DWD WFS layer
     * @return a list of start date/end date tuples
     */
    public static List<DateTime[]> getRequestIntervals(DateTime startDate, DateTime endDate, String layerName) {
        List<DateTime[]> interval = new ArrayList<DateTime[]>();

        // if the resolution is hourly, the request will be splitted into intervalls
        if (isHourly(layerName)) {
            interval = calculateRequestIntervalsForResolution(startDate, endDate,
                    HOURLY_RESOLUTION);
        }

        // if the resolution is daily, the request will be splitted into intervalls
        if (isDaily(layerName)) {
            interval = calculateRequestIntervalsForResolution(startDate, endDate,
                    DAILY_RESOLUTION);
        }

        // if the resolution is monthly, the request will be splitted into intervalls
        if (isMonthly(layerName)) {
            interval = calculateRequestIntervalsForResolution(startDate, endDate,
                    MONTHLY_RESOLUTION);

        }
        // if the resolution is annual, the request must not be splitted
        if (isAnnual(layerName)) {
            interval = calculateRequestIntervalsForResolution(startDate, endDate,
                    ANNUAL_RESOLUTION);
        }
        return interval;
    }

    /**
     * Calculates request intervals depending on the time resolution of a certain WFS layer
     *
     * @param startDate  requested start date
     * @param endDate    requested end date
     * @param resolution temporal resolution of a certain WFS layer (using constants for interval
     *                   calculation)
     * @return a list of start date/end date tuples
     */
    public static List<DateTime[]> calculateRequestIntervalsForResolution(DateTime startDate, DateTime endDate, int resolution) {

        List<DateTime[]> outputList = new ArrayList<DateTime[]>();

        Hours hourSumHours = Hours.hoursBetween(startDate, endDate);
        int hourSum = hourSumHours.getHours();

        double interval = DwdTemporalResolutionHelper.calculateInterval(hourSum, resolution);
        int intervalInMinutes = (int) (hourSum / interval) * 60;

        // Start interval
        DateTime[] eachIntervalDates = {startDate, startDate.plusMinutes(intervalInMinutes)}; // will be probably overwritten
        outputList.add(eachIntervalDates); // put first value into list

        int endCondition = (int) interval;
        // calculating the start- and enddate for every interval
        for (int i = 1; i <= interval; i++) {
            double modulo = interval % endCondition;
            // every interval
            if (i <= endCondition || modulo == 0) {
                eachIntervalDates = new DateTime[2];
                eachIntervalDates[0] = outputList.get(i - 1)[1];    // startdate is the enddate of the previous interval
                eachIntervalDates[1] = eachIntervalDates[0].plusMinutes(intervalInMinutes); // enddate
                outputList.add(eachIntervalDates);
            }

        }
        return outputList;
    }


}
