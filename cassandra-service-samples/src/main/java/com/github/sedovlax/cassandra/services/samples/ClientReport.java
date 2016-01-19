package com.github.sedovlax.cassandra.services.samples;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.github.sedovalx.cassandra.service.generation.annotations.CassandraService;
import com.github.sedovalx.cassandra.service.generation.annotations.QueryParams;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Alexander
 * on 14.01.2016.
 */
@Getter
@Setter
@ToString(exclude = {"data"})
@EqualsAndHashCode
@QueryParams(consistency = "QUORUM", fetchSize = 1000)
@CassandraService(customAccessor = ClientReportUpdateAccessor.class)
@Table(keyspace = "sample", name = "client_report")
public class ClientReport {
    @PartitionKey(0)
    @Column(name = "region")
    private Integer region;

    @QueryParams(consistency = "ALL", tracing = true)
    @ClusteringColumn(0)
    @Column(name = "tpl_code")
    private String templateCode;

    @ClusteringColumn(1)
    @Column(name = "p_year")
    private Integer periodYear;

    @ClusteringColumn(2)
    @Column(name = "p_code")
    private Integer periodCode;

    @ClusteringColumn(3)
    @Column(name = "client_id")
    private Long clientId;

    @Column
    private String data;

    @Column
    private Date ver;

    @Column
    private boolean deleted;
}
