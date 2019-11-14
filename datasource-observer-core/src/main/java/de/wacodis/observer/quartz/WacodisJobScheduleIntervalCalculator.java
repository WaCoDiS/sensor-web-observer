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
 * Calculates adjusted observation intervals based on a wacodis job's schedule.
 * If provided the job's execution pattern (cron) is used to derive
 * the adjusted interval, otherwise the job's temporal coverage is used.
 *
 * @author Arne
 */
public class WacodisJobScheduleIntervalCalculator implements ObservationIntervalCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(WacodisJobScheduleIntervalCalculator.class);

    private Duration unadustedInterval;
    private Duration maxObservationInterval = new Duration(60 * 60 * 24 * 1000); //24 hours
    private double dailyCoefficient = 0.2;

    /**
     * @param unadjustedInterval base interval to be adjusted, also adjusted
     * interval will never fall below the provided value
     */
    public WacodisJobScheduleIntervalCalculator(Duration unadjustedInterval) {
        this.unadustedInterval = unadjustedInterval;
    }

    /**
     * @param unadjustedInterval base interval to be adjusted, also adjusted
     * interval will never fall below the provided value
     * @param maxObservationInterval adjusted interval will never exceed the
     * provided value
     */
    public WacodisJobScheduleIntervalCalculator(Duration unadjustedInterval, Duration maxObservationInterval) {
        this.unadustedInterval = unadjustedInterval;
        this.maxObservationInterval = maxObservationInterval;
    }

    /* 
     * @return base interval to be adjusted, also adjusted interval will never fall below this value
     */
    public Duration getUnadustedInterval() {
        return unadustedInterval;
    }

    /**
     * @param unadustedInterval base interval to be adjusted, also adjusted
     * interval will never fall below the provided value
     */
    public void setUnadustedInterval(Duration unadustedInterval) {
        this.unadustedInterval = unadustedInterval;
    }

    /**
     * @return adjusted interval will never exceed this value
     */
    public Duration getMaxObservationInterval() {
        return maxObservationInterval;
    }

    /**
     * default: 24 hours
     *
     * @param maxObservationInterval adjusted interval will never exceed the
     * provided value
     */
    public void setMaxObservationInterval(Duration maxObservationInterval) {
        this.maxObservationInterval = maxObservationInterval;
    }

    /**
     * Determines the influence of the number of days between two planned
     * executions of a wacodis job. Higher values will leed do longer adjusted
     * observation intervals. 
     * default: 0.2
     *
     * @return current value
     */
    public double getDailyCoefficient() {
        return dailyCoefficient;
    }

    /**
     * Determines the influence of the number of days between two planned
     * executions of a wacodis job. Higher values will leed do longer adjusted
     * observation intervals.
     *
     * @param dailyCoefficient new value
     */
    public void setDailyCoefficient(double dailyCoefficient) {
        this.dailyCoefficient = dailyCoefficient;
    }

    /**
     * Calculates adjusted observation interval.
     * Formula: d * this.dailyCoefficient * this.unadjustedInterval (where d is the number of days between two planned executions of the provided wacodis job) 
     * The number of days between two planned executions of the provided wacodis job is derived from provided wacodis job's execution pattern (cron pattern).
     * If no pattern is provided the number of days is derived from provided wacodis job's temporal covergae (duration). 
     * If neither execution pattern nor temporal coverage is provided the unadjusted interval(this.unadjustedInterval) is returned.
     * The adjusted interval will never fall below the unadjusted interval (this.unadjustedInterval).
     * The adjusted interval will never exceed the maximum interval (this.maxObservationInterval).
     *
     * @param job wacodis job for which an adjusted observation interval is to
     * be calculated
     * @return adjusted observation interval
     */
    @Override
    public Duration calculateInterval(WacodisJobDefinition job) {
        LOG.debug("calculating observation schedule for wacodis job {}", job.getId());

        Duration observationInterval;

        if (!isCronPatternProvided(job) && !isTemporalCoverageDurationProvided(job)) {
            LOG.warn("cannot calculate observation schedule for wacodis job {} because neither execution pattern nor valid temporal coverage is defined, fall back to default interval", job.getId());
            observationInterval = getDefaultInterval();
        } else {
            Optional<Duration> jobExecInterval = calculateJobExecutionInterval(job);
            if (jobExecInterval.isPresent()) {
                observationInterval = calculateObservationInterval(jobExecInterval.get());
            } else {
                LOG.warn("failed to calculate observation schedule for wacodis job {}, fall back to default interval", job.getId());
                observationInterval = getDefaultInterval();
            }
        }

        LOG.debug("calculated observation schedule for wacodis job {} is {} seconds", job.getId(), observationInterval.getMillis() / 1000);

        return observationInterval;
    }

    /**
     * Formula: d * this.dailyCoefficient * this.unadjustedInterval (where d is the number of days between two planned executions of the provided wacodis job)
     */
    private Duration calculateObservationInterval(Duration jobExecutionInterval) {
        double daysInExecInterval = ((double) jobExecutionInterval.getMillis()) / (24 * 60 * 60 * 1000); //86400 seconds in 24 hours

        double intervalMultiplicator = daysInExecInterval * Math.abs(this.dailyCoefficient);
        long adjustedInterval_Millis = (long) (intervalMultiplicator * unadustedInterval.getMillis());
        Duration adjustedObservationInterval = new Duration(adjustedInterval_Millis);

        if (adjustedObservationInterval.isLongerThan(maxObservationInterval)) { //constructor demands milliseconds
            return maxObservationInterval;
        } else if (adjustedObservationInterval.isShorterThan(unadustedInterval)) {
            return unadustedInterval;
        } else {
            return adjustedObservationInterval;
        }
    }

    /**
     * calculate duration of time frame between to planned executions of a
     * wacodis job
     */
    private Optional<Duration> calculateJobExecutionInterval(WacodisJobDefinition job) {
        Optional<Duration> duration;
        ZonedDateTime now = ZonedDateTime.now();

        if (isCronPatternProvided(job)) {
            //derive time frame from cron
            LOG.debug("derive interval from execution pattern (cron) for wacodis job {}", job.getId());
            duration = calculateDurationFromExecutionPattern(job, now);

            if (!duration.isPresent()) {
                LOG.debug("failed to derive interval from execution pattern for wacodis job {}, try to derive interval from temporal coverage", job.getId());
                duration = calculateDurationFromTemporalCoverage(job, convertZonedDateTimeToDateTime(now));
            }
        } else {
            //derive time frame from temporal coverage
            LOG.debug("derive interval from temporal coverage for wacodis job {}, because no execution pattern (cron) is provided", job.getId());
            duration = calculateDurationFromTemporalCoverage(job, convertZonedDateTimeToDateTime(now));
        }

        return duration;
    }

    private Optional<Duration> calculateDurationFromExecutionPattern(WacodisJobDefinition job, ZonedDateTime instant) {

        if (isCronPatternProvided(job)) {
            DateTime startDuration;
            DateTime endDuration;

            String cronPattern = job.getExecution().getPattern().trim();
            Cron jobCron = parseUnixCronPattern(cronPattern);
            LOG.debug("schedule for wacodis job {} is: {}", job.getId(), CronDescriptor.instance(Locale.US).describe(jobCron));

            ExecutionTime execution = ExecutionTime.forCron(jobCron);
            Optional<ZonedDateTime> prevExec = execution.lastExecution(instant);
            Optional<ZonedDateTime> nextExec = execution.nextExecution(instant);

            if (prevExec.isPresent()) {
                startDuration = convertZonedDateTimeToDateTime(prevExec.get());
            } else {
                LOG.debug("Cannot calculate previous execution for wacodis job {}, assume current instant of time: ", job.getId(), instant.toString());
                startDuration = DateTime.now();
            }

            if (nextExec.isPresent()) {
                endDuration = convertZonedDateTimeToDateTime(nextExec.get());
                return Optional.of(new Duration(startDuration, endDuration));
            } else {
                LOG.debug("Cannot calculate previous execution from cron pattern {} for wacodis job {}", cronPattern, job.getId());
                return Optional.empty();
            }
        } else {
            LOG.debug("cannot derive interval for wacodis job {} because no valid execution pattern (cron) is defined", job.getId());
            return Optional.empty();
        }
    }

    private Optional<Duration> calculateDurationFromTemporalCoverage(WacodisJobDefinition job, DateTime instant) {
        if (isTemporalCoverageDurationProvided(job)) {
            Period period = Period.parse(job.getTemporalCoverage().getDuration());
            return Optional.of(calculateDurationFromPeriod(period));
        } else {
            LOG.debug("cannot derive interval for wacodis job {} because no valid temporal coverage is defined", job.getId());
            return Optional.empty();
        }
    }

    /**
     * get Duration from a period using a instant of time as reference for
     * calculation
     *
     * @param period
     * @param instant
     * @return
     */
    private Duration calculateDurationFromPeriod(Period period, DateTime instant) {
        DateTime start = instant;
        DateTime end = instant.plus(period);

        return new Duration(start, end);
    }

    /**
     * get Duration from a period using the current instant of time (now) as
     * reference for calculation
     *
     * @param period
     * @return
     */
    private Duration calculateDurationFromPeriod(Period period) {
        return calculateDurationFromPeriod(period, DateTime.now());
    }

    private DateTime convertZonedDateTimeToDateTime(ZonedDateTime zonedDateTime) {
        return new DateTime(
                zonedDateTime.toInstant().toEpochMilli(),
                DateTimeZone.forTimeZone(TimeZone.getTimeZone(zonedDateTime.getZone())));
    }

    private boolean isCronPatternProvided(WacodisJobDefinition job) {
        if (job.getExecution() == null) {
            return false;
        } else {
            String cronPattern = job.getExecution().getPattern();
            return (cronPattern != null && !cronPattern.trim().isEmpty());
        }
    }

    private boolean isTemporalCoverageDurationProvided(WacodisJobDefinition job) {
        if (job.getTemporalCoverage() == null) {
            return false;
        } else {
            String period = job.getTemporalCoverage().getDuration();
            return (period != null && !period.trim().isEmpty());
        }
    }

    private CronParser getUnixCronParser() {
        CronDefinition unixCronDef = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser unixCronParser = new CronParser(unixCronDef);

        return unixCronParser;
    }

    private Cron parseUnixCronPattern(String cron) {
        CronParser parser = getUnixCronParser();
        return parser.parse(cron);
    }

    private Duration getDefaultInterval() {
        return (this.unadustedInterval.isShorterThan(this.maxObservationInterval)) ? this.unadustedInterval : this.maxObservationInterval;
    }
}
