package de.wacodis.observer.quartz;

import de.wacodis.observer.config.ExecutionIntervalConfig;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import de.wacodis.observer.model.WacodisJobDefinition;
import de.wacodis.observer.core.JobFactory;
import org.joda.time.Duration;

@Component
public class JobScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(JobScheduler.class);

    private static final int DEFAULTEXECUTIONINTERVAL_SECONDS = 60 * 60;
    private static final int DEFAULTMAXEXECUTIONINTERVAL_SECONDS = 60 * 60 * 24; // 24 hours 

    @Autowired
    private QuartzServer wacodisQuartz;

    @Autowired
    private ExecutionIntervalConfig intervalConf;

    public JobScheduler() {
    }

    public void scheduleJob(WacodisJobDefinition job, JobFactory factory) {
        try {
            JobDataMap data = new JobDataMap();
            data.put("areaOfInterest", job.getAreaOfInterest());

            JobDetail jobDetail = factory.initializeJob(job, data);

            Trigger trigger = prepareTrigger(job, data);

            wacodisQuartz.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            LOG.warn(e.getMessage());
            LOG.debug(e.getMessage(), e);
        }
    }

    private Trigger prepareTrigger(WacodisJobDefinition job, JobDataMap data) {
        /**
         * set a default execution interval
         */
        if (!data.containsKey("executionInterval")) {
            data.put("executionInterval", DEFAULTEXECUTIONINTERVAL_SECONDS);
        }

        //use default max interval if not set in config
        int executionInterval = (this.intervalConf.getMaxInterval() > 0) ? calculateObservationInterval(job, data.getInt("executionInterval"), this.intervalConf.getMaxInterval()) : calculateObservationInterval(job, data.getInt("executionInterval"), DEFAULTMAXEXECUTIONINTERVAL_SECONDS);

        LOG.info("Build new Trigger for wacodis job {} with execution interval: {} seconds", job.getId(), executionInterval);

        return TriggerBuilder.newTrigger()
                .withIdentity(job.getId().toString(), job.getName())
                .startNow()
                .withSchedule(SimpleScheduleBuilder
                        .simpleSchedule().repeatForever()
                        .withIntervalInSeconds(executionInterval))
                .build();
    }

    /**
     * @return interval in seconds
     */
    private int calculateObservationInterval(WacodisJobDefinition job, int unadjustedInterval, int maxInterval) {
        ObservationIntervalCalculator intervalCalc = new WacodisJobScheduleIntervalCalculator(new Duration(unadjustedInterval * 1000), new Duration(maxInterval * 1000)); //Duration constructor demands milliseconds
        Duration interval = intervalCalc.calculateInterval(job);
        long intervalSeconds = interval.getStandardSeconds();

        if (intervalSeconds <= Integer.MAX_VALUE) { //check if conversion to integer is lossy
            return (int) intervalSeconds;
        } else {
            LOG.warn("calculated execution interval (seconds) for wacodis job {} exceeds Integer.Max_VALUE, use Integer.MAX_VALUE {} instead.", job.getId(), Integer.MAX_VALUE);
            return Integer.MAX_VALUE;
        }
    }
}
