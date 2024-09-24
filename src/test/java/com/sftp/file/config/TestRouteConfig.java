package com.sftp.file.config;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestRouteConfig extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // Define routes here for testing
    }

//    @Bean
//    public ProducerTemplate producerTemplate(CamelContext camelContext) {
//        return camelContext.createProducerTemplate();
//    }

    @Bean
    public CamelContext camelContext() {
        return new DefaultCamelContext();
    }

    @Bean
    public RouteBuilder routeBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // Define routes
            }
        };
    }
}
