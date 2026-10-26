package com.tutor.ct_tutor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Все запросы вида /images/** будут мапиться в локальную папку C:/ct-tutor/images/
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:C:/ct-tutor/ct-tutor/images/");
    }
}