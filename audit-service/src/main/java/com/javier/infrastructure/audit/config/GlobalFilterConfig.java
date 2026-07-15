package com.javier.infrastructure.audit.config;

import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

//@Configuration
@RequiredArgsConstructor
public class GlobalFilterConfig {

    private final List<Filter> filters;

//    @Bean
//    public List<FilterRegistrationBean<Filter>> registerFilters() {
//        AtomicInteger order = new AtomicInteger(1);
//
//        return filters.stream()
//                .map(filter -> {
//                    FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>();
//                    bean.setFilter(filter);
//                    bean.setOrder(order.getAndIncrement());
//                    return bean;
//                })
//                .toList();
//    }
}
