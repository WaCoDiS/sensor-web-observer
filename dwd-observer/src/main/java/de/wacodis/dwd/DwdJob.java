/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.dwd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.base.AbstractPeriod;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISOPeriodFormat;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.support.MessageBuilder;

import com.esotericsoftware.minlog.Log;

import de.wacodis.dwd.cdc.DwdProductsMetadata;
import de.wacodis.dwd.cdc.DwdProductsMetadataDecoder;
import de.wacodis.dwd.cdc.DwdRequestParamsEncoder;
import de.wacodis.dwd.cdc.DwdWfsRequestParams;
import de.wacodis.dwd.cdc.DwdWfsRequestor;
import de.wacodis.observer.model.AbstractDataEnvelopeAreaOfInterest;
import de.wacodis.observer.model.DwdDataEnvelope;
import de.wacodis.observer.model.WacodisJobDefinitionTemporalCoverage;
import de.wacodis.observer.publisher.PublisherChannel;

/**
 *
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
	public static final String PREVIOUS_DAYS_KEY = "previousDays";

	private static final Logger LOG = LoggerFactory.getLogger(DwdJob.class);

	@Override
	public void execute(JobExecutionContext jec) throws JobExecutionException {
		LOG.info("Start DwdJob's execute()");
		JobDataMap dataMap = jec.getJobDetail().getJobDataMap();

		// 1) Get all required request parameters stored in the JobDataMap
		String version = dataMap.getString(VERSION_KEY);
		String layerName = dataMap.getString(LAYER_NAME_KEY);
		String serviceUrl = dataMap.getString(VERSION_KEY);
		String durationISO = dataMap.getString(TEMPORAL_COVERAGE_KEY); // previous days unnecessary?
		String[] executionAreaJSON = dataMap.getString(EXECUTION_AREA_KEY).split(",");

		// parse executionAreaJSON into Float list
		String bottomLeftYStr = executionAreaJSON[0].split(" ")[0];
		String bottomLeftXStr = executionAreaJSON[0].split(" ")[1];
		String upperRightYStr = executionAreaJSON[1].split(" ")[2];
		String upperRightXStr = executionAreaJSON[1].split(" ")[3];

		float bottomLeftY = Float.parseFloat(bottomLeftYStr);
		float bottomLeftX = Float.parseFloat(bottomLeftXStr);
		float upperRightY = Float.parseFloat(upperRightYStr);
		float upperRightX = Float.parseFloat(upperRightXStr);
		ArrayList<Float> area = new ArrayList<Float>();
		area.add(0, bottomLeftY);
		area.add(1, bottomLeftX);
		area.add(2, upperRightY);
		area.add(3, upperRightX);

		Object previousDaysCandidate = dataMap.get(PREVIOUS_DAYS_KEY);

		if (previousDaysCandidate != null && previousDaysCandidate instanceof Integer
				&& ((int) previousDaysCandidate) > 0) {
			int previousDays = (int) previousDaysCandidate;

			Period period = Period.parse(durationISO, ISOPeriodFormat.standard());
			// timeframe
			DateTime startDate = DateTime.now();
			startDate.withPeriodAdded(period, -1);

			Set<DwdDataEnvelope> FinalEnvelopeSet = createFinalEnvelopeSet(version, layerName, serviceUrl, area,
					startDate);
		}

	}

	private Set<DwdDataEnvelope> createFinalEnvelopeSet(String version, String layerName, String serviceUrl,
			ArrayList<Float> area, DateTime startDate) {
		Set<DwdDataEnvelope> envelopeSet = new HashSet<DwdDataEnvelope>();
		ArrayList<DateTime[]> interval = new ArrayList<DateTime[]>();

		// if the resolution is hourly, the request will be splitted into intervalls
		if (DwdTemporalResolution.isHourly(layerName)) {
			interval = DwdTemporalResolution.calculateStartAndEndDate(startDate,
					DwdTemporalResolution.HOURLY_RESOLUTION);
		}

		// if the resolution is daily, the request will be splitted into intervalls
		if (DwdTemporalResolution.isDaily(layerName)) {
			interval = DwdTemporalResolution.calculateStartAndEndDate(startDate,
					DwdTemporalResolution.HOURLY_RESOLUTION);
		}

		// if the resolution is monthly, the request will be splitted into intervalls
		if (DwdTemporalResolution.isMonthly(layerName)) {
			interval = DwdTemporalResolution.calculateStartAndEndDate(startDate,
					DwdTemporalResolution.MONTHLY_RESOLUTION);

		}
		// if the resolution is annual, the request must not be splitted
		if (DwdTemporalResolution.isAnnual(layerName)) {
			interval = DwdTemporalResolution.calculateStartAndEndDate(startDate,
					DwdTemporalResolution.ANNUAL_RESOLUTION);
		}
		if (interval != null) {
			for (int i = 0; i < interval.size(); i++) {
				DwdDataEnvelope dataEnvelope = createDwdDataEnvelope(version, layerName, serviceUrl, area,
						interval.get(i)[0], interval.get(i)[1]);
				envelopeSet.add(dataEnvelope);
				i++;
			}
		}
		return envelopeSet;
	}

	private DwdDataEnvelope createDwdDataEnvelope(String version, String layerName, String serviceUrl, List<Float> area,
			DateTime startDate, DateTime endDate) {
		// 2) Create a DwdWfsRequestParams onbject from the restored request parameters
		DwdWfsRequestParams params = DwdRequestParamsEncoder.encode(version, layerName, area, startDate, endDate);

		// - startDate and endDate should be chosen depending on the request interval
		// and the last request endDate
		// 3) Request WFS with request paramaters
		DwdProductsMetadata metadata = null;
		try {
			metadata = DwdWfsRequestor.request(serviceUrl, params);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 4) Decode DwdProductsMetada to DwdDataEnvelope
		DwdDataEnvelope dataEnvelope = DwdProductsMetadataDecoder.decode(metadata);
		LOG.info("new dataEnvelope:\n{}", dataEnvelope.toString());
		// 5) Publish DwdDataEnvelope message
		PublisherChannel pub = null;
		pub.sendDataEnvelope().send(MessageBuilder.withPayload(dataEnvelope).build());
		LOG.info("DataEnvelope published");

		return dataEnvelope;
	}

}
