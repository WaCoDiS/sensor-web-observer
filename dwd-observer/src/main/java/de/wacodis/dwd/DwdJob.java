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

	//Enumerations of temporal resolution
	//{average Temp., precipitation, air pressure, air humidity, cloud coverage}
	//public enum hourly { TT_TU_MN009, R1_MN008, P0_MN008, RF_TU_MN009, N_MN008 }
	public Set hourly = new HashSet<> (Arrays.asList( "TT_TU_MN009", "R1_MN008", "P0_MN008", "RF_TU_MN009", "N_MN008" ));
	//{average Temp., max temp, min temp, precipitation, wind top, air pressure, snow height, fresh snow height, sunshine duration, air humidity, cloud coverage}
	//public enum daily {TMK_MN004, TXK_MN004, TNK_MN004, RS_MN006, FX_MN003, PM_MN004, SH_TAG_MN006, NSH_TAG_MN006, SDK_MN004, UPM_MN004, NM_MN004}
	public Set daily = new HashSet<> (Arrays.asList( "TMK_MN004", "TXK_MN004", "TNK_MN004", "RS_MN006", "FX_MN003", "PM_MN004", "SH_TAG_MN006", "NSH_TAG_MN006", "SDK_MN004", "UPM_MN004", "NM_MN004" ));
	//{average Temp., max temp, min temp, precipitation, air pressure, snow height, fresh snow height, sunshine duration, air humidity, cloud coverage}
	//public enum monthly { MO_TT_MN004, MO_TX_MN004, MO_TN_MN004, MO_RR_MN006, MO_P0_MN004, MO_SH_S_MN006, MO_NSH_MN006, MO_SD_S_MN004, MO_RF_MN004, MO_N_MN004}
	public Set monthly = new HashSet<> (Arrays.asList(  "MO_TT_MN004", "MO_TX_MN004", "MO_TN_MN004", "MO_RR_MN006", "MO_P0_MN004", "MO_SH_S_MN006", "MO_NSH_MN006", "MO_SD_S_MN004", "MO_RF_MN004", "MO_N_MN004" ));
	//{average Temp., max temp, min temp, precipitation, air pressure, snow height, fresh snow height, sunshine duration, air humidity, cloud coverage}
	//public enum annual { JA_TT_MN004, JA_TX_MN004, JA_TN_MN004, JA_RR_MN006, JA_P0_MN004, JA_SH_S_MN006, JA_NSH_MN006, JA_SD_S_MN004, JA_RF_MN004, JA_N_MN004}
	public Set annual = new HashSet<> (Arrays.asList( "JA_TT_MN004", "JA_TX_MN004", "JA_TN_MN004", "JA_RR_MN006", "JA_P0_MN004", "JA_SH_S_MN006", "JA_NSH_MN006", "JA_SD_S_MN004", "JA_RF_MN004", "JA_N_MN004"));
		
	public static String LAYER_NAME_KEY = "layerName";

	private static final Logger LOG = LoggerFactory.getLogger(DwdJob.class);

	public static final String PREVIOUS_DAYS_KEY = "previousDays";
	

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
		Object previousDaysCandidate = dataMap.get(PREVIOUS_DAYS_KEY);
		if (previousDaysCandidate != null && previousDaysCandidate instanceof Integer
				&& ((int) previousDaysCandidate) > 0) {
			int previousDays = (int) previousDaysCandidate;
			
			
			if(hourly.contains(layerName)) {
				
			}
			if(daily.contains(layerName)) {
				
			}
			if(monthly.contains(layerName)) {
				
			}
			if(annual.contains(layerName)) {
				
			}
			
			startDate = DateTime.now().minusDays(previousDays);
			
			
		} else {
			// lets default to one week
			startDate = DateTime.now().minusDays(7);
		}
		
		
		
		
		
		
		
		
		
		
		
		
		

		// 2) Create a DwdWfsRequestParams onbject from the restored request parameters
		DwdWfsRequestParams params = DwdRequestParamsEncoder.encode(version, layerName, area, startDate, DateTime.now());

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
	}

}
