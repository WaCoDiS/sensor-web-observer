/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.observer.quartz;

import de.wacodis.observer.model.WacodisJobDefinition;

/**
 *
 * @author Arne
 */
public interface ObservationIntervalCalculator {
    
    org.joda.time.Duration calculateInterval(WacodisJobDefinition job);
    
}
