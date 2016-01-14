package ru.croc.rosstat.csod.domain.cass;

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
 * Author alsedov
 * On 25.12.2015
 */

@CassandraService
@Getter
@Setter
@ToString(exclude = {"xml", "flk", "json", "microdata"})
@EqualsAndHashCode
@Table(keyspace = "csod", name = "source_data")
public class SourceDataCass {
    @PartitionKey
    @Column(name = "data_id")
    @QueryParams(consistency = "ALL")
    private UUID dataId;

    @ClusteringColumn
    @Column(name = "ver")
    @QueryParams(consistency = "ONE", tracing = true)
    private Date ver;

    @Column(name = "status")
    private Integer status;

    @Column(name = "json")
    private String json;

    @Column(name = "xml")
    private String xml;

    @Column(name = "flk")
    private String flk;

    @Column(name = "microdata")
    private String microdata;

    @Column(name = "flk_summary")
    private String flkSummary;

    @Column(name = "togs")
    private Integer togs;

    @Column(name = "tpl_code")
    private String templateCode;

    @Column(name = "p_year")
    private Integer periodYear;

    @Column(name = "p_code")
    private Integer periodCode;

    @Column(name = "okpo")
    private Long okpo;

    @Column(name = "tpl_ver")
    private String templateVersion;
}
