/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.observer.quartz;

import de.wacodis.observer.model.WacodisJobDefinition;
import de.wacodis.observer.model.WacodisJobDefinitionExecution;
import de.wacodis.observer.model.WacodisJobDefinitionTemporalCoverage;
import java.util.UUID;
import org.joda.time.Duration;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Arne
 */
public class WacodisJobScheduleIntervalCalculatorTest {

    public WacodisJobScheduleIntervalCalculatorTest() {
    }

    @Test
    @DisplayName("test CalculateInterval")
    public void testCalculateInterval() {
        System.out.println("calculateInterval");
        WacodisJobDefinition job = new WacodisJobDefinition();
        job.setId(UUID.randomUUID());
        WacodisJobDefinitionExecution execution = new WacodisJobDefinitionExecution();
        execution.setPattern("0 0 1 * *"); //At 00:00 on day-of-month 1.
        job.setExecution(execution);
        Duration unadjustedInterval = new Duration(3600 * 1000); //1 hour in millis

        WacodisJobScheduleIntervalCalculator instance = new WacodisJobScheduleIntervalCalculator(unadjustedInterval);
        Duration adjustedInterval = instance.calculateInterval(job);

        assertTrue(adjustedInterval.isLongerThan(unadjustedInterval));
    }

    @Test
    @DisplayName("test CalculateInterval with different dailyCoefficients")
    public void testCalculateInterval_DailyCoefficient() {
        System.out.println("calculateInterval with different daily coefficients");
        WacodisJobDefinition job = new WacodisJobDefinition();
        job.setId(UUID.randomUUID());
        WacodisJobDefinitionExecution execution = new WacodisJobDefinitionExecution();
        execution.setPattern("0 0 1 * *"); //At 00:00 on day-of-month 1.
        job.setExecution(execution);
        Duration unadjustedInterval = new Duration(3600 * 1000); //1 hour in millis

        WacodisJobScheduleIntervalCalculator instance = new WacodisJobScheduleIntervalCalculator(unadjustedInterval);
        instance.setDailyCoefficient(0.3);
        Duration adjustedInterval1 = instance.calculateInterval(job);
        instance.setDailyCoefficient(0.4);
        Duration adjustedInterval2 = instance.calculateInterval(job);
        instance.setDailyCoefficient(0.2);
        Duration adjustedInterval3 = instance.calculateInterval(job);

        assertAll(
                () -> assertTrue(adjustedInterval2.isLongerThan(adjustedInterval1)),
                () -> assertTrue(adjustedInterval3.isShorterThan(adjustedInterval1))
        );

    }

    @Test
    @DisplayName("test CalculateInterval maxInterval")
    public void testCalculateInterval_MaxInterval() {
        System.out.println("calculateInterval max Interval");
        WacodisJobDefinition job = new WacodisJobDefinition();
        job.setId(UUID.randomUUID());
        WacodisJobDefinitionExecution execution = new WacodisJobDefinitionExecution();
        execution.setPattern("0 0 1 * *"); //At 00:00 on day-of-month 1.
        job.setExecution(execution);
        Duration unadjustedInterval = new Duration(3600 * 1000); //1 hour in millis

        WacodisJobScheduleIntervalCalculator instance = new WacodisJobScheduleIntervalCalculator(unadjustedInterval);
        instance.setMaxObservationInterval(new Duration(1)); //set short max interval

        Duration adjustedInterval = instance.calculateInterval(job);

        assertEquals(instance.getMaxObservationInterval(), adjustedInterval);
    }

    @Test
    @DisplayName("test CalculateInterval minInterval")
    public void testCalculateInterval_minInterval() {
        System.out.println("calculateInterval min Interval");
        WacodisJobDefinition job = new WacodisJobDefinition();
        job.setId(UUID.randomUUID());
        WacodisJobDefinitionExecution execution = new WacodisJobDefinitionExecution();
        execution.setPattern("0 0 1 * *"); //At 00:00 on day-of-month 1.
        job.setExecution(execution);
        Duration unadjustedInterval = new Duration(3600 * 1000); //1 hour in millis

        WacodisJobScheduleIntervalCalculator instance = new WacodisJobScheduleIntervalCalculator(unadjustedInterval);
        instance.setDailyCoefficient(0.0); //set minimal daily coefficient

        Duration adjustedInterval = instance.calculateInterval(job);
        //adjusted interval must not be shorter than unadjusted interval
        assertEquals(instance.getUnadustedInterval(), adjustedInterval);
    }

    @Test
    @DisplayName("test CalculateInterval without cron pattern")
    public void testCalculateInterval_TempCov() {
        System.out.println("calculateInterval without cron pattern");
        WacodisJobDefinition job = new WacodisJobDefinition();
        job.setId(UUID.randomUUID());
        WacodisJobDefinitionTemporalCoverage tempCov = new WacodisJobDefinitionTemporalCoverage();
        tempCov.setPreviousExecution(false);
        tempCov.setDuration("P1M"); //monthly
        job.setTemporalCoverage(tempCov);

        Duration unadjustedInterval = new Duration(3600 * 1000); //1 hour in millis
        WacodisJobScheduleIntervalCalculator instance = new WacodisJobScheduleIntervalCalculator(unadjustedInterval);
        Duration adjustedInterval = instance.calculateInterval(job);

        assertTrue(adjustedInterval.isLongerThan(unadjustedInterval));
    }

    @Test
    @DisplayName("test CalculateInterval fallback")
    public void testCalculateInterval_Fallback() {
        System.out.println("calculateInterval without cron pattern and temporal coverage");
        WacodisJobDefinition job = new WacodisJobDefinition();
        job.setId(UUID.randomUUID());

        Duration unadjustedInterval = new Duration(3600 * 1000); //1 hour in millis
        WacodisJobScheduleIntervalCalculator instance = new WacodisJobScheduleIntervalCalculator(unadjustedInterval);
        Duration adjustedInterval = instance.calculateInterval(job);

        //fall back to unadjustedInterval if neither cron pattern nor temporal coverage is provided
        assertEquals(unadjustedInterval, adjustedInterval);
    }

}
