package de.wacodis.observer.http;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Configuration
public class HttpClientConfig {

    @Bean
    public CloseableHttpClient httpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        return builder.build();
    }
}
