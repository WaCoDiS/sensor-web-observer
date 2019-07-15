/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.dwd;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DwdJob implements Job {

    public static String LAYER_NAME_KEY = "layerName";

    private static final Logger LOG = LoggerFactory.getLogger(DwdJob.class);

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        JobDataMap dataMap = jec.getJobDetail().getJobDataMap();

        String layerName= dataMap.getString(LAYER_NAME_KEY);
        
        //TODO 
        //1) Get all required request parameters stored in the JobDataMap
        //2) Create a DwdWfsRequestParams onbject from the restored request parameters
        //   - startDate and endDate should be choosed depending on the request interval
        //     and the last request endDate 
        //3) Request WFS with request paramaters
        //4) Decode DwdProductsMetada to DwdDataEnvelope
        //5) Publish DwdDataEnvelope message

    }

}
