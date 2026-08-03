package org.javierGomez18.infrastructure.sharedfilters.config;

import org.javierGomez18.infrastructure.sharedfilters.filters.HeaderValidationFilter;
import org.javierGomez18.infrastructure.sharedfilters.filters.MdcFilter;
import org.javierGomez18.infrastructure.sharedfilters.filters.SanitizationFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

@AutoConfiguration
@EnableConfigurationProperties(ClientWhitelistProperties.class)
public class SharedFiltersAutoConfiguration {

    @Bean
    @Order(1)
    public MdcFilter mdcFilter() {
        return new MdcFilter();
    }

    @Bean
    @Order(2)
    public HeaderValidationFilter headerValidationFilter(ClientWhitelistProperties whitelist) {
        return new HeaderValidationFilter(whitelist);
    }

    @Bean
    @Order(3)
    public SanitizationFilter sanitizationFilter() {
        return new SanitizationFilter();
    }
}
