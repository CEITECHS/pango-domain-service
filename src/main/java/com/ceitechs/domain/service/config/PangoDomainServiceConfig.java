package com.ceitechs.domain.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
@Configuration
@Import({PangoDomainServiceMongoConfiguration.class})
public class PangoDomainServiceConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
