/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.observer.quartz;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import de.wacodis.observer.model.WacodisJobDefinition;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Arne
 */
public class WacodisJobScheduleIntervalCalculator implements ObservationIntervalCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(WacodisJobScheduleIntervalCalculator.class);

    private Duration unajustedExecutionInterval;
    private Duration maxExecutionInterval = new Duration(60 * 60 * 24 * 1000); //24 hours
    private double dailyCoefficient = 0.2;

    public WacodisJobScheduleIntervalCalculator() {
    }

    public WacodisJobScheduleIntervalCalculator(Duration unajustedExecutionInterval) {
        this.unajustedExecutionInterval = unajustedExecutionInterval;
    }

    public WacodisJobScheduleIntervalCalculator(Duration unadjustedExecutionInterval, Duration maxExecutionInterval) {
        this.unajustedExecutionInterval = unadjustedExecutionInterval;
        this.maxExecutionInterval = maxExecutionInterval;
    }

    public Duration getUnajustedExecutionInterval() {
        return unajustedExecutionInterval;
    }

    public void setUnajustedExecutionInterval(Duration unajustedExecutionInterval) {
        this.unajustedExecutionInterval = unajustedExecutionInterval;
    }

    public Duration getMaxExecutionInterval() {
        return maxExecutionInterval;
    }

    public void setMaxExecutionInterval(Duration maxExecutionInterval) {
        this.maxExecutionInterval = maxExecutionInterval;
    }

    public double getDailyCoefficient() {
        return dailyCoefficient;
    }

    public void setDailyCoefficient(double dailyCoefficient) {
        this.dailyCoefficient = dailyCoefficient;
    }

    @Override
    public Duration calculateInterval(WacodisJobDefinition job) {
        LOG.debug("calculating observation schedule for wacodis job {}, cron pattern: {}", job.getId(), job.getExecution().getPattern());
        Duration jobExecInterval = calculateJobExecutionInterval(job);
        Duration observationInterval = calculateObservationInterval(jobExecInterval);

        LOG.debug("calculated observation schedule for wacodis job {} is {} seconds", job.getId(), observationInterval.getMillis() / 1000);

        return observationInterval;
    }

    private Duration calculateObservationInterval(Duration jobExecutionInterval) {
        double daysInExecInterval = ((double) jobExecutionInterval.getMillis()) / (24 * 60 * 60 * 1000); //86400 seconds in 24 hours

        double intervalMultiplicator = daysInExecInterval * Math.abs(this.dailyCoefficient);
        long adjustedInterval_Millis = (long) (intervalMultiplicator * unajustedExecutionInterval.getMillis());
        Duration adjustedObservationInterval = new Duration(adjustedInterval_Millis);

        if (adjustedObservationInterval.isLongerThan(maxExecutionInterval)) { //constructor demands milliseconds
            return maxExecutionInterval;
        } else if (adjustedObservationInterval.isShorterThan(unajustedExecutionInterval)) {
            return unajustedExecutionInterval;
        } else {
            return adjustedObservationInterval;
        }
    }

    private Duration calculateJobExecutionInterval(WacodisJobDefinition job) {
        Duration duration;

        CronDefinition unixCronDef = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser unixCronParser = new CronParser(unixCronDef);

        String cronPattern = job.getExecution().getPattern();

        if (cronPattern != null && !cronPattern.isEmpty()) {
            DateTime startDuration;
            DateTime endDuration;

            ZonedDateTime now = ZonedDateTime.now();
            cronPattern = cronPattern.trim();
            //get time frame from execution interval
            Cron jobCron = unixCronParser.parse(cronPattern);
            LOG.debug("schedule for wacodis job {} is: {}", job.getId(), CronDescriptor.instance(Locale.US).describe(jobCron));

            ExecutionTime execution = ExecutionTime.forCron(jobCron);
            Optional<ZonedDateTime> prevExec = execution.lastExecution(now);
            Optional<ZonedDateTime> nextExec = execution.nextExecution(now);

            if (prevExec.isPresent()) {
                startDuration = convertZonedDateTimeToDateTime(prevExec.get());
            } else {
                LOG.debug("Cannot calculate previous execution for wacodis job {}, assume current instant of time: ", job.getId(), now.toString());
                startDuration = DateTime.now();
            }

            if (nextExec.isPresent()) {
                endDuration = convertZonedDateTimeToDateTime(nextExec.get());
                duration = new org.joda.time.Duration(startDuration, endDuration);
            } else {
                LOG.debug("Cannot calculate previous execution for wacodis job {}, try to derive schedule from temporal coverage", job.getId());
                duration = calculateDurationFromTemporalCoverage(job);
            }
        } else {
            LOG.debug("No cron pattern provided for wacodis job {}, derive schedule from temporal coverage", job.getId());
            //get time frame from temporal coverage 
            duration = calculateDurationFromTemporalCoverage(job);
        }

        return duration;
    }

    private Duration calculateDurationFromTemporalCoverage(WacodisJobDefinition job) {
        if (job.getTemporalCoverage() != null && job.getTemporalCoverage().getDuration() != null && !job.getTemporalCoverage().getDuration().isEmpty()) { //check if period is defined in job definition
            Period period = Period.parse(job.getTemporalCoverage().getDuration());
            return calculateDurationFromPeriod(period);
        } else {
            LOG.warn("cannot derive duration for wacodis job {} because no valid temporal coverage is defined, fall back to default interval", job.getId());
            return this.unajustedExecutionInterval;
        }
    }

    private Duration calculateDurationFromPeriod(Period period, DateTime instant) {
        return period.toStandardDuration();
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
