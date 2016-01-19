package com.github.sedovalx.cassandra.services.samples.config;

import com.datastax.driver.mapping.MappingManager;
import com.github.sedovlax.cassandra.services.samples.accessors.java8.ClientReportAccessorAdapter;
import com.github.sedovlax.cassandra.services.samples.mappers.ClientReportMapper;
import com.github.sedovlax.cassandra.services.samples.services.ClientReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Author alsedov on 18.01.2016
 */
@Configuration
public class ServicesConfig {
    @Lazy
    @Autowired
    private MappingManager mappingManager;

    @Lazy
    @Bean
    public ClientReportService clientReportService(){
        ClientReportAccessorAdapter accessor = new ClientReportAccessorAdapter(mappingManager);
        ClientReportMapper mapper = new ClientReportMapper(mappingManager);
        return new ClientReportService(accessor, mapper);
    }
}
