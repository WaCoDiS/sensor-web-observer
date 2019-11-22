/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.dwd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.xmlbeans.XmlException;
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

import de.wacodis.dwd.cdc.DwdProductsMetadata;
import de.wacodis.dwd.cdc.DwdProductsMetadataDecoder;
import de.wacodis.dwd.cdc.DwdRequestParamsEncoder;
import de.wacodis.dwd.cdc.DwdWfsRequestParams;
import de.wacodis.dwd.cdc.DwdWfsRequestor;
import de.wacodis.observer.model.DwdDataEnvelope;
import de.wacodis.observer.publisher.PublisherChannel;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DwdJob implements Job {

    // identifiers
    public static final String VERSION_KEY = "version";
    public static final String LAYER_NAME_KEY = "layerName";
    public static final String SERVICE_URL_KEY = "serviceUrl";
    public static final String TEMPORAL_COVERAGE_KEY = "temporalCoverage";
    public static final String EXECUTION_INTERVAL_KEY = "executionInterval";
    public static final String EXECUTION_AREA_KEY = "executionArea";
    public static final String LATEST_REQUEST_END_DATE = "endDate";

    private static final Logger LOG = LoggerFactory.getLogger(DwdJob.class);

    JobDataMap jobDataMap = new JobDataMap();

    @Autowired
    private PublisherChannel pub;

    @Autowired
    private DwdWfsRequestor requestor;

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        LOG.debug("Start DwdJob's execute()");
        JobDataMap dataMap = jec.getJobDetail().getJobDataMap();

        // 1) Get all required request parameters stored in the JobDataMap
        String version = dataMap.getString(VERSION_KEY);
        String layerName = dataMap.getString(LAYER_NAME_KEY);
        String serviceUrl = dataMap.getString(SERVICE_URL_KEY);
        String durationISO = dataMap.getString(TEMPORAL_COVERAGE_KEY); // previous days unnecessary?
        String[] executionAreaJSON = dataMap.getString(EXECUTION_AREA_KEY).split(",");

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
        if (jobDataMap.get(LATEST_REQUEST_END_DATE) != null) {
            startDate = (DateTime) jobDataMap.get(LATEST_REQUEST_END_DATE);
        } else {
            startDate = endDate.withPeriodAdded(period, -1);
        }
        jobDataMap.put(LATEST_REQUEST_END_DATE, endDate);

        this.createDwdDataEnvelope(version, layerName, serviceUrl, area, startDate, endDate);
    }

    /**
     * Create and publish the {@link DwdDataEnvelope} for a set of request parameters
     *
     * @param version    version number of WFS - usually 2.0.0
     * @param layerName  short designation of layer
     * @param serviceUrl general url of webservice
     * @param area       bbox (minLon, minLat, maxLon, maxLat)
     * @param startDate  start date of the request timeframe
     * @param endDate    end date of the request timeframe
     */
    private void createDwdDataEnvelope(String version, String layerName, String serviceUrl,
                                       ArrayList<Float> area, DateTime startDate, DateTime endDate) {
        Set<DwdDataEnvelope> envelopeSet = new HashSet<DwdDataEnvelope>();
        List<DateTime[]> interval = DwdTemporalResolutionHelper.getRequestIntervals(startDate, endDate, layerName);

        // Start requesting DWD data iteratively if the amount of data is too large
        if (interval != null) {
            for (int i = 0; i < interval.size(); i++) {
                DwdWfsRequestParams params = DwdRequestParamsEncoder.encode(version, layerName, area, interval.get(i)[0], interval.get(i)[1]);
                DwdDataEnvelope dataEnvelope = this.requestDwdMetadata(serviceUrl, params);
                if (dataEnvelope != null) {
                    // Publish DwdDataEnvelope message
                    pub.sendDataEnvelope().send(MessageBuilder.withPayload(dataEnvelope).build());
                    LOG.info("Successfully created and published new DwdDataEnvelope: {}", dataEnvelope);
                } else {
                    LOG.warn("Failed creation and publishing of new DwdDataEnvelope for request params: {}", params);
                }
            }
        }
    }

    /**
     * Request DWD metadata from an WFS and create a {@link DwdDataEnvelope}
     *
     * @param serviceUrl the DWD WFS service URL
     * @param params     parameters to use for DWD WFS request
     * @return
     */
    private DwdDataEnvelope requestDwdMetadata(String serviceUrl, DwdWfsRequestParams params) {
        DwdDataEnvelope dataEnvelope = null;
        try {
            // Request DWD WFS with request paramaters
            DwdProductsMetadata metadata = requestor.request(serviceUrl, params);
            if (metadata == null) {
                LOG.info("No dataEnvelope to publish from DWD WFS response");
            }

            // Decode DwdProductsMetadata to DwdDataEnvelope
            dataEnvelope = DwdProductsMetadataDecoder.decode(metadata);
            LOG.info("Publish new dataEnvelope:\n{}", dataEnvelope.toString());

        } catch (IOException | ParserConfigurationException | SAXException e) {
            LOG.error(e.getMessage());
            LOG.debug("Error while performing DWD WFS request for URL " + serviceUrl + " with parameters: " + params, e);
        }
        return dataEnvelope;
    }

}
