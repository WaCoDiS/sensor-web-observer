package de.wacodis.codeDe;

import de.wacodis.codeDe.sentinel.CodeDeOpenSearchRequestor;
import de.wacodis.observer.publisher.PublisherChannel;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

/**
 * @author <a href="mailto:tim.kurowski@hs-bochum.de">Tim Kurowski</a>
 * @author <a href="mailto:christian.koert@hs-bochum.de">Christian Koert</a>
 */

public class CodeDeJob implements Job {

    // Class varibles for Request
    public static final String PARENT_IDENTIFIER_KEY = "parentIdentifier";
    public static final String START_DATE_KEY = "startDate";
    public static final String END_DATE_KEY = "endDate";

    public static final String BBOX_KEY = "bbox";
    public static final String CLOUD_COVER_KEY = "cloudCover";

    //Class variables for Job
    public static final String TEMPORAL_COVERAGE_KEY = "temporalCoverage";
    public static final String SATELLITE = "satellite";
    public static final String LATEST_REQUEST_END_DATE = "endDate";



    private static final Logger LOG = LoggerFactory.getLogger(CodeDeJob.class);

    JobDataMap jobDataMap = new JobDataMap();

    @Autowired
    private PublisherChannel pub;

    @Autowired
    private CodeDeOpenSearchRequestor requestor;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOG.debug("Start CodeDeJob's execute()");
        // 1) Get all required request parameters stored in the JobDataMap
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        // 1) Get all required request parameters stored in the JobDataMap
        String satellite = dataMap.getString(SATELLITE);
        String durationISO = dataMap.getString(TEMPORAL_COVERAGE_KEY);
        String cloudCover = dataMap.getString(CLOUD_COVER_KEY);
        String[] executionAreaJSON = dataMap.getString(BBOX_KEY).split(",");

        // parse executionAreaJSON into Float list
        String bottomLeftYStr = executionAreaJSON[0].split(" ")[0];
        String bottomLeftXStr = executionAreaJSON[0].split(" ")[1];
        String upperRightYStr = executionAreaJSON[1].split(" ")[0];
        String upperRightXStr = executionAreaJSON[1].split(" ")[1];

        float bottomLeftY = Float.parseFloat(bottomLeftYStr);
        float bottomLeftX = Float.parseFloat(bottomLeftXStr);
        float upperRightY = Float.parseFloat(upperRightYStr);
        float upperRightX = Float.parseFloat(upperRightXStr);
        ArrayList<Float> area = new ArrayList<Float>();
        area.add(0, bottomLeftY);
        area.add(1, bottomLeftX);
        area.add(2, upperRightY);
        area.add(3, upperRightX);

        Period period = Period.parse(durationISO, ISOPeriodFormat.standard());

        DateTime endDate = DateTime.now();
        DateTime startDate = null;
        // If there was a Job execution before, consider the latest request
        // end date as start date for the current request.
        // Else, calculate the start date for an initial request by taking a
        // certain period into account
        if (dataMap.get(LATEST_REQUEST_END_DATE) != null) {
            startDate = (DateTime) dataMap.get(LATEST_REQUEST_END_DATE);
        } else {
            startDate = endDate.withPeriodAdded(period, -1);
        }
        dataMap.put(LATEST_REQUEST_END_DATE, endDate);

        //this.createDwdDataEnvelope(version, layerName, serviceUrl, area, startDate, endDate);

    }
}
