package com.github.sedovalx.cassandra.services.samples.utils;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

/**
 * Author alsedov on 18.01.2016
 */
public class CassandraCluster implements AutoCloseable {

    private final String address;
    private final int port;
    private final String keyspace;
    private Cluster cluster;
    private Session session;

    public CassandraCluster(String address, int port, String keyspace) {
        this.address = address;
        this.port = port;
        this.keyspace = keyspace;
    }

    private Session connect() {
        cluster = Cluster.builder().addContactPoint(address).withPort(port).build();
        return cluster.connect(keyspace);
    }

    @Override
    public void close() throws Exception {
        cluster.close();
    }

    public Session session(){
        if (session == null) {
            session = connect();
        }
        return session;
    }
}
