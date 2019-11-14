package de.wacodis.observer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * All values must be given in seconds.
 * @author Arne
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("datasource-observer.execution.interval")
public class ExecutionIntervalConfig {

    private int sensorWeb;
    private int sentinel;
    private int dwd;
    private int maxInterval;
    

    public int getSensorWeb() {
        return sensorWeb;
    }

    public void setSensorWeb(int sensorWeb) {
        this.sensorWeb = sensorWeb;
    }

    public int getSentinel() {
        return sentinel;
    }

    public void setSentinel(int sentinel) {
        this.sentinel = sentinel;
    }

    public int getDwd() {
        return dwd;
    }

    public void setDwd(int dwd) {
        this.dwd = dwd;
    }

    public int getMaxInterval() {
        return maxInterval;
    }

    public void setMaxInterval(int maxInterval) {
        this.maxInterval = maxInterval;
    }
}
