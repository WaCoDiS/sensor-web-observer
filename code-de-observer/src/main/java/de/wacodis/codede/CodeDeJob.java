package de.wacodis.codede;

import de.wacodis.codede.sentinel.*;
import de.wacodis.codede.sentinel.exception.HttpConnectionException;
import de.wacodis.observer.decode.DecodingException;
import de.wacodis.observer.model.CopernicusDataEnvelope;
import de.wacodis.observer.publisher.PublisherChannel;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:tim.kurowski@hs-bochum.de">Tim Kurowski</a>
 * @author <a href="mailto:christian.koert@hs-bochum.de">Christian Koert</a>
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class CodeDeJob implements Job {

    // Class variables for Request
    public static final String PARENT_IDENTIFIER_KEY = "parentIdentifier";
    public static final String START_DATE_KEY = "startDate";
    public static final String END_DATE_KEY = "endDate";

    public static final String BBOX_KEY = "bbox";
    public static final String CLOUD_COVER_KEY = "cloudCover";

    //Class variables for Job
    public static final String TEMPORAL_COVERAGE_KEY = "temporalCoverage";
    public static final String PRODUCT_IDENTIFIER = "productIdentifier";
    public static final String LATEST_REQUEST_END_DATE = "endDate";
    public static final String MAXIMUM_RECORDS_KEY = "maximumRecords";
    public static final String RECORD_SCHEMA_KEY = "recordSchema";
    public static final String START_PAGE_KEY = "startPage";

    public static final String EXECUTION_INTERVAL_KEY = "executionInterval";


    private static final Logger LOG = LoggerFactory.getLogger(CodeDeJob.class);

    @Autowired
    private PublisherChannel pub;

    @Autowired
    private CodeDeOpenSearchRequestor requestor;

    /**
     * executes the job
     *
     * @param context all necessary parameters for a request
     */
    @Override
    public void execute(JobExecutionContext context) {
        LOG.debug("Start CodeDeJob's execute()");
        // 1) Get all required request parameters stored in the JobDataMap
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        // 1) Get all required request parameters stored in the JobDataMap
        String satelliteProduct = dataMap.getString(PRODUCT_IDENTIFIER);
        String durationISO = dataMap.getString(TEMPORAL_COVERAGE_KEY);
        String maxCloudCover = dataMap.getString(CLOUD_COVER_KEY);
        ArrayList cloudCover = new ArrayList() {
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
        ArrayList<Float> area = new ArrayList<>();
        area.add(0, bottomLeftY);
        area.add(1, bottomLeftX);
        area.add(2, upperRightY);
        area.add(3, upperRightX);

        Period period = Period.parse(durationISO, ISOPeriodFormat.standard());
        DateTime endDate = DateTime.now();
        DateTime startDate;
        // If there was a Job execution before, consider the latest request
        // end date as start date for the current request.
        // Else, calculate the start date for an initial request by taking a
        // certain period into account
        if (dataMap.get(LATEST_REQUEST_END_DATE) != null)
            startDate = (DateTime) dataMap.get(LATEST_REQUEST_END_DATE);
        else startDate = endDate.withPeriodAdded(period, -1);
        dataMap.put(LATEST_REQUEST_END_DATE, endDate);

        this.publishDataEnvelopes(satelliteProduct, startDate, endDate, area, cloudCover);

    }

    /**
     * Publish the {@link CopernicusDataEnvelope} for a set of request parameters
     *
     * @param parentIdentifier id of the requested satallitesensor
     * @param startDate        start date of the request timeframe
     * @param endDate          end date of the request timeframe
     * @param bbox             bbox (minLon, minLat, maxLon, maxLat)
     * @param cloudCover       requested cloud cover
     */
    private void publishDataEnvelopes(String parentIdentifier, DateTime startDate, DateTime endDate, List<Float> bbox, List<Float> cloudCover) {
        CodeDeRequestParamsEncoder encoder = new CodeDeRequestParamsEncoder();
        CodeDeRequestParams params = encoder.encode(parentIdentifier, startDate, endDate, bbox, cloudCover);
        List<CopernicusDataEnvelope> dataEnvelopes = this.createDataEnvelopes(params);
        for (CopernicusDataEnvelope copDE : dataEnvelopes) {
            pub.sendDataEnvelope().send(MessageBuilder.withPayload(copDE).build());
            LOG.info("Published new CodeDeDataEnvelope for Copernicus dataset: {}", copDE.getDatasetId());
            LOG.debug("Published CodeDeDataEnvelope: {}", copDE);
        }

    }

    /**
     * Request CodeDe metadata from an Opensearch API and create a {@link CopernicusDataEnvelope}
     *
     * @param params parameters from CodeDeRequestParams
     * @return all requested dataEnvelopes
     */
    private List<CopernicusDataEnvelope> createDataEnvelopes(CodeDeRequestParams params) {
        ArrayList<CopernicusDataEnvelope> dataEnvelopes = new ArrayList<>();
        // Request CodeDe Opensearch API with request paramaters
        List<CodeDeProductsMetadata> metadata = new ArrayList<>();
        try {
            metadata = requestor.request(params);
        } catch (DecodingException ex) {
            LOG.error("Error while decoding CODE-DE response: {}", ex.getMessage());
            LOG.debug("Decoding error cause: ", ex);
        } catch (HttpConnectionException ex) {
            LOG.error("Error while requesting CODE-DE: {}", ex.getMessage());
            LOG.debug("Requesting error cause: ", ex);
        }
        // Decode CodeDeProductsMetadata to CopernicusDataEnvelope
        for (CodeDeProductsMetadata object : metadata) {
            CopernicusDataEnvelope dataEnvelope = CodeDeProductsMetadataDecoder.decode(object);
            dataEnvelopes.add(dataEnvelope);
        }
        return dataEnvelopes;
    }
}
