package de.wacodis.observer.quartz;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
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
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Period;

@Component
public class JobScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(JobScheduler.class);

    private static final long DEFAULTEXECUTIONINTERVAL_MILLIS = 60 * 60;

    @Autowired
    private QuartzServer wacodisQuartz;

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
            data.put("executionInterval", DEFAULTEXECUTIONINTERVAL_MILLIS);
        }

        LOG.info("Build new Trigger with execution interval: {} seconds", data.get("executionInterval"));

        return TriggerBuilder.newTrigger()
                .withIdentity(job.getId().toString(), job.getName())
                .startNow()
                .withSchedule(SimpleScheduleBuilder
                        .simpleSchedule().repeatForever()
                        .withIntervalInSeconds(data.getInt("executionInterval")))
                .build();
    }

    private void calculateDuration(WacodisJobDefinition job) {
        Duration duration;

        CronDefinition unixCronDef = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser unixCronParser = new CronParser(unixCronDef);

        String cronPattern = job.getExecution().getPattern().trim();

        LOG.debug("calculating schedule for wacodis job {}, cron pattern: {}", job.getId(), cronPattern);

        if (cronPattern != null && !cronPattern.isEmpty()) {
            DateTime startDuration;
            DateTime endDuration;

            ZonedDateTime now = ZonedDateTime.now();
            //get time frame from execution interval
            Cron jobCron = unixCronParser.parse(cronPattern);
            LOG.debug("schedule for wacodis job {} is: {}", job.getId(), CronDescriptor.instance(Locale.US).describe(jobCron));

            ExecutionTime execution = ExecutionTime.forCron(jobCron);
            Optional<ZonedDateTime> prevExec = execution.lastExecution(now);
            Optional<ZonedDateTime> nextExec = execution.nextExecution(now);

            if (prevExec.isPresent()) {
                startDuration = convertZonedDateTimeToDateTime(prevExec.get());
            } else {
                LOG.info("Cannot calculate previous execution for wacodis job {}, assume current instant of time: ", job.getId(), now.toString());
                startDuration = DateTime.now();
            }

            if (nextExec.isPresent()) {
                endDuration = convertZonedDateTimeToDateTime(nextExec.get());
                duration = new org.joda.time.Duration(startDuration, endDuration);
            } else {
                LOG.info("Cannot calculate previous execution for wacodis job {}, try to derive schedule from temporal coverage", job.getId());
                duration = calcualteDurationFromTemporalCoverage(job);
            }

        } else {
            LOG.info("No cron pattern provided for wacodis job {}, derive schedule from temporal coverage", job.getId());
            //get time frame from temporal coverage 
            duration = calcualteDurationFromTemporalCoverage(job);
        }

    }

    private Duration calcualteDurationFromTemporalCoverage(WacodisJobDefinition job) {
        if (job.getTemporalCoverage() != null && job.getTemporalCoverage().getDuration() != null && !job.getTemporalCoverage().getDuration().isEmpty()) { //check if period is defined in job definition
            Period period = Period.parse(job.getTemporalCoverage().getDuration());
            return calculateDurationFromPeriod(period);
        } else {
            LOG.warn("cannot derive duration for wacodis job {} because no valid temporal coverage is defined, fall back to default interval", job.getId());
            return new Duration(DEFAULTEXECUTIONINTERVAL_MILLIS); //TODO use config interval
        }
    }

    private Duration calculateDurationFromPeriod(Period period, DateTime instant) {
        DateTime end = instant.plus(period);
        return new Duration(instant, end);
    }

    private Duration calculateDurationFromPeriod(Period period) {
        return calculateDurationFromPeriod(period, DateTime.now());
    }

    private DateTime convertZonedDateTimeToDateTime(ZonedDateTime zonedDateTime) {
        return new DateTime(
                zonedDateTime.toInstant().toEpochMilli(),
                DateTimeZone.forTimeZone(TimeZone.getTimeZone(zonedDateTime.getZone())));
    }

}
