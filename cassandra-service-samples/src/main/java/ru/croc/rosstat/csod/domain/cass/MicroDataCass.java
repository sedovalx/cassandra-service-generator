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
import ru.croc.rosstat.csod.domain.cass.custom.accessors.MicroDataCassAccessor;


import java.util.Date;
import java.util.UUID;

/**
 * Created by Alexander
 * on 27.12.2015.
 */
@CassandraService(excludeKeys = {"dataId"}, customAccessor = MicroDataCassAccessor.class)
@Getter
@Setter
@ToString(exclude = {"data", "flkSummary", "versions"})
@EqualsAndHashCode
@Table(keyspace = "csod", name = "micro_data")
@QueryParams(consistency = "QUORUM", fetchSize = 1000)
public class MicroDataCass {
    // Номер ТОГС
    @PartitionKey(0)
    @Column(name = "togs")
    private Integer togs;

    // Код шаблона формы
    @PartitionKey(1)
    @Column(name = "tpl_code")
    private String templateCode;

    // Год отчетного периода
    @PartitionKey(2)
    @Column(name = "p_year")
    private Integer periodYear;

    // Код отчетного периода
    @PartitionKey(3)
    @Column(name = "p_code")
    private Integer periodCode;

    // ОКПО респондента, приславшего отчет
    @ClusteringColumn(0)
    @Column(name = "okpo")
    private Long okpo;

    // Уникальный идентификатор первичных данных (всех версий), ссылка на source_data.data_id
    @ClusteringColumn(1)
    @Column(name = "data_id")
    private UUID dataId;

    // Означает, что последняя версия отчета отправлена на доработку
    @Column(name = "is_canceled")
    private Boolean isCanceled;

    // Json с параметрами
    @Column(name = "data")
    private String data;

    // Метка версии отчета
    @Column(name = "ver")
    private Date ver;

    // Json с версиями MicroData
    @Column(name = "flk_summary")
    private String flkSummary;

    // Результаты ФЛК в формате Выжимка из результатов ФЛК
    @Column(name = "versions")
    private String versions;
}
