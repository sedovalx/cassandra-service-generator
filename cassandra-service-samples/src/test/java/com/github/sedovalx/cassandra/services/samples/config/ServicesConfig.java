package com.github.sedovalx.cassandra.services.samples.config;

import com.datastax.driver.mapping.MappingManager;
import com.github.sedovalx.cassandra.services.samples.utils.CassandraCluster;
import com.github.sedovlax.cassandra.services.samples.services.ClientReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Author alsedov on 18.01.2016
 */
@Configuration
public class ServicesConfig {
    @Autowired
    private CassandraCluster cassandraCluster;

    @Bean
    public ClientReportService clientReportService(){
        return new ClientReportService(() -> new MappingManager(cassandraCluster.session()));
    }
}
