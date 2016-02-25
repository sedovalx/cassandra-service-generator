package com.github.sedovalx.cassandra.services.samples.config;

import com.github.sedovalx.cassandra.services.samples.utils.CassandraCluster;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Author alsedov on 18.01.2016
 */
@Configuration
public class CassandraConfig {
    @Bean
    public CassandraCluster cassandraCluster() {
        return new CassandraCluster("127.0.0.1", 9142, "sample");
    }
}
