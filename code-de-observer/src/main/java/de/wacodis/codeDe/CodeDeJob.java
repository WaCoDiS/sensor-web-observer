package de.wacodis.codeDe;

import de.wacodis.codeDe.sentinel.*;
import de.wacodis.observer.model.CopernicusDataEnvelope;
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
import org.springframework.messaging.support.MessageBuilder;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author <a href="mailto:tim.kurowski@hs-bochum.de">Tim Kurowski</a>
 * @author <a href="mailto:christian.koert@hs-bochum.de">Christian Koert</a>
 */

public class CodeDeJob implements Job {

    // Class variables for Request
    public static final String PARENT_IDENTIFIER_KEY = "parentIdentifier";
    public static final String START_DATE_KEY = "startDate";
    public static final String END_DATE_KEY = "endDate";

    public static final String BBOX_KEY = "bbox";
    public static final String CLOUD_COVER_KEY = "cloudCover";

    //Class variables for Job
    public static final String TEMPORAL_COVERAGE_KEY = "temporalCoverage";
    public static final String SATELLITEPRODUCT = "S2_MSI_L2A";
    public static final String LATEST_REQUEST_END_DATE = "endDate";
    public static final String MAXIMUM_RECORDS_KEY = "maximumRecords";
    public static final String RECORD_SCHEMA_KEY = "recordSchema";
    public static final String START_PAGE_KEY = "startPage";



    private static final Logger LOG = LoggerFactory.getLogger(CodeDeJob.class);

    JobDataMap jobDataMap = new JobDataMap();

    @Autowired
    private PublisherChannel pub;

    @Autowired
    private CodeDeOpenSearchRequestor requestor;

    /**
     * executes the job
     * @param context all necessary parameters for a request
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOG.debug("Start CodeDeJob's execute()");
        // 1) Get all required request parameters stored in the JobDataMap
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        // 1) Get all required request parameters stored in the JobDataMap
        String satelliteProduct = dataMap.getString(SATELLITEPRODUCT);
        String durationISO = dataMap.getString(TEMPORAL_COVERAGE_KEY);
        String maxCloudCover = dataMap.getString(CLOUD_COVER_KEY);
        List<Float> cloudCover = new ArrayList() {
            {
                add(0);
                add(maxCloudCover);
            }
        };
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

        try {
            this.publishDataEnvelopes(satelliteProduct, startDate, endDate, area, cloudCover);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Publish the {@link CopernicusDataEnvelope} for a set of request parameters
     *
     * @param parentIdentifier    id of the requested satallitesensor
     * @param startDate  start date of the request timeframe
     * @param endDate end date of the request timeframe
     * @param bbox       bbox (minLon, minLat, maxLon, maxLat)
     * @param cloudCover requested cloud cover
     * @param endDate
     */
    private void publishDataEnvelopes(String parentIdentifier, DateTime startDate, DateTime endDate, List<Float> bbox, List<Float>cloudCover) throws Exception {
        CodeDeRequestParamsEncoder encoder = new CodeDeRequestParamsEncoder();
        CodeDeRequestParams params = encoder.encode(parentIdentifier, startDate, endDate, bbox, cloudCover);
        List<CopernicusDataEnvelope> dataEnvelopes = this.createDataEnvelopes(params);
        for (CopernicusDataEnvelope copDE:dataEnvelopes) {
            pub.sendDataEnvelope().send(MessageBuilder.withPayload(copDE).build());
        }

    }

    /**
     * Request CodeDe metadata from an Opensearch API and create a {@link CopernicusDataEnvelope}
     *
     * @param params parameters from CodeDeRequestParams
     * @return all requested dataEnvelopes
     */
    private List<CopernicusDataEnvelope> createDataEnvelopes(CodeDeRequestParams params) throws Exception {
        List<CopernicusDataEnvelope> dataEnvelopes = null;
        try {
            // Request CodeDe Opensearch API with request paramaters
            List<CodeDeProductsMetadata> metadata = requestor.request(params);

            // Decode CodeDeProductsMetadata to CopernicusDataEnvelope
            for (CodeDeProductsMetadata object:metadata) {
                CopernicusDataEnvelope dataEnvelope = CodeDeProductsMetadataDecoder.decode(object);
                dataEnvelopes.add(dataEnvelope);
                LOG.debug("new dataEnvelope:\n{" + dataEnvelope.toString() + "}");
            }

        } catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException e) {
            LOG.error("Error while performing CodeDe Opensearch request for CodeDe product metadata.");
        }
        return dataEnvelopes;
    }
}
