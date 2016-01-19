package com.github.sedovalx.cassandra.services.samples.config;

import com.datastax.driver.mapping.MappingManager;
import com.github.sedovalx.cassandra.services.samples.utils.CassandraCluster;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Author alsedov on 18.01.2016
 */
@Configuration
public class CassandraConfig {
    @Bean
    public CassandraCluster cassandraCluster() {
        return new CassandraCluster("127.0.0.1", 9142, "sample");
    }

    @Lazy
    @Bean
    public MappingManager mappingManager(){
        return new MappingManager(cassandraCluster().connect());
    }
}
