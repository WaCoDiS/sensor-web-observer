/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.dwd;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Period;
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
	public static String VERSION_KEY = "version";
	public static String LAYER_NAME_KEY = "layerName";
	public static String SERVICE_URL_KEY = "serviceUrl";
	public static String EXECUTION_INTERVAL_KEY = "executionInterval";
	public static String EXECUTION_AREA_KEY = "executionArea";
	public static final String PREVIOUS_DAYS_KEY = "previousDays";
	
	private static final Logger LOG = LoggerFactory.getLogger(DwdJob.class);
	@Override
	public void execute(JobExecutionContext jec) throws JobExecutionException {
		LOG.info("Start DwdJob's execute()");
		JobDataMap dataMap = jec.getJobDetail().getJobDataMap();

		// 1) Get all required request parameters stored in the JobDataMap
		String version = dataMap.getString("version");
		String layerName = dataMap.getString(LAYER_NAME_KEY);
		String serviceUrl = dataMap.getString("serviceUrl");

		WacodisJobDefinitionTemporalCoverage executionTemporalCoverage = (WacodisJobDefinitionTemporalCoverage) dataMap
				.get("executionTemporalCoverage");

		AbstractDataEnvelopeAreaOfInterest executionArea = (AbstractDataEnvelopeAreaOfInterest) dataMap
				.get("executionArea");
		List<Float> area = executionArea.getExtent();

		// timeframe
		DateTime startDate = null;
		DateTime endDate = null;

		Object previousDaysCandidate = dataMap.get(PREVIOUS_DAYS_KEY);

		if (previousDaysCandidate != null && previousDaysCandidate instanceof Integer // ????
				&& ((int) previousDaysCandidate) > 0) {
			int previousDays = (int) previousDaysCandidate;

			// startDate = DateTime.now().minusDays(previousDays);

			// Doppelt gemoppelt da nicht nur Tage gezählt werden dürfen
			String durationISO = executionTemporalCoverage.getDuration();
			ISOPeriodFormat iso = null;
			Period period = Period.parse(durationISO, iso.standard());
			double hoursSum = period.getHours() + period.getDays() * 24 + period.getWeeks() * 24 * 7
					+ period.getMonths() * 30.436857 * 24 + period.getYears() * 365.2425 * 24;

			Set<DwdDataEnvelope> envelopeSet = new HashSet<DwdDataEnvelope>();
			if (hourly.contains(layerName)) {
				// duration shorter than one week
				if (hoursSum <= (24 * 7)) {
					startDate = DateTime.now().minusHours((int) hoursSum);
					DwdDataEnvelope dataEnvelope = createDwdDataEnvelope(version, layerName, serviceUrl, area,
							startDate, DateTime.now());
					envelopeSet.add(dataEnvelope);
				}
				// duration longer than one week
				else {
					int intervall = (int) (hoursSum / (24 * 7)); // splitting duration in week blocks
					for (int i = 0; i < intervall; i++) {
						startDate = DateTime.now().minusHours((int) hoursSum);
						endDate = startDate.plusHours((int) (hoursSum / intervall));
						DwdDataEnvelope dataEnvelope = createDwdDataEnvelope(version, layerName, serviceUrl, area,
								startDate, endDate);
						envelopeSet.add(dataEnvelope);
					}
				}
			}

			if (daily.contains(layerName)) {
				// duration shorter than one month
				if (hoursSum <= (24 * 7 * 30)) {
					startDate = DateTime.now().minusHours((int) hoursSum);
					DwdDataEnvelope dataEnvelope = createDwdDataEnvelope(version, layerName, serviceUrl, area,
							startDate, DateTime.now());
					envelopeSet.add(dataEnvelope);
				}
				// duration longer than one month
				else {
					int intervall = (int) (hoursSum / (24 * 7 * 30)); // splitting duration in month blocks
					for (int i = 0; i < intervall; i++) {
						startDate = DateTime.now().minusHours((int) hoursSum);
						endDate = startDate.plusHours((int) (hoursSum / intervall));
						DwdDataEnvelope dataEnvelope = createDwdDataEnvelope(version, layerName, serviceUrl, area,
								startDate, endDate);
						envelopeSet.add(dataEnvelope);
					}
				}

			}

			if (monthly.contains(layerName)) {
				DwdDataEnvelope dataEnvelope = createDwdDataEnvelope(version, layerName, serviceUrl, area, startDate,
						DateTime.now());
			}

			if (annual.contains(layerName)) {
				DwdDataEnvelope dataEnvelope = createDwdDataEnvelope(version, layerName, serviceUrl, area, startDate,
						DateTime.now());
			}

		} else {

			// kann weg?

			// lets default to one week
			startDate = DateTime.now().minusDays(7);
		}

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
