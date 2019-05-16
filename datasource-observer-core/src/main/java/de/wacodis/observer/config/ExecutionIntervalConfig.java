package de.wacodis.observer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("datasource.execution.interval")
public class ExecutionIntervalConfig {

    public int sensorWeb;
    public int sentinel;

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

}
