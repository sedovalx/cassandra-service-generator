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

/**
 * Author alsedov on 29.12.2015
 */
@CassandraService
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Table(keyspace = "csod", name = "approved_respondents")
public class ApprovedRespondentsCass {
    // идентификатор версии техкарты
    @PartitionKey(0)
    @Column(name = "routing_id")
    private String routingId;

    // ID критерия
    @PartitionKey(1)
    @Column(name = "tpl_code")
    @QueryParams(fetchSize = 100)
    private String tplCode;

    // ТОГС
    @PartitionKey(2)
    @Column(name = "togs")
    private Integer togs;

    // ОКПО респондента
    @ClusteringColumn(0)
    @Column(name = "okpo")
    private Long okpo;

    // ID респондента в ОСР
    @Column(name = "osr_id")
    private String osrId;

    // Дата утверждения
    @Column(name = "fix_date")
    private Date fixDate;
}
