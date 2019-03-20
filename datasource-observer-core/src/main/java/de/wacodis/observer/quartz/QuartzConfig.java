/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wacodis.observer.quartz;

import java.util.Properties;
import javax.sql.DataSource;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 *
 * @author Matthes Rieke (m.rieke@52north.org)
 */
@Configuration
public class QuartzConfig {

    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext) {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);

        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean quartzScheduler(ApplicationContext applicationContext, QuartzProperties qp) {
        SchedulerFactoryBean quartzScheduler = new SchedulerFactoryBean();

        Properties props = new Properties();
        props.putAll(qp.getProperties());

        quartzScheduler.setQuartzProperties(props);

        quartzScheduler.setJobFactory(jobFactory(applicationContext));

        return quartzScheduler;
    }

    @Bean
    @ConfigurationProperties("spring.datasource.quartz-data-source")
    @QuartzDataSource
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

}
