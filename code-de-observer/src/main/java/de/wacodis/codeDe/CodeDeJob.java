package de.wacodis.codeDe;

import de.wacodis.observer.publisher.PublisherChannel;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CodeDeJob implements Job {

    public static final String PARENT_IDENTIFIER_KEY = "parentIdentifier";
    public static final String START_DATE_KEY = "startDate";
    public static final String END_DATE_KEY = "endDate";
    public static final String BBOX_KEY = "bbox";
    public static final String CLOUD_COVER_KEY = "cloudCover";

    private static final Logger LOG = LoggerFactory.getLogger(CodeDeJob.class);

    JobDataMap jobDataMap = new JobDataMap();

    @Autowired
    private PublisherChannel pub;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        // 1) Get all required request parameters stored in the JobDataMap



    }
}
