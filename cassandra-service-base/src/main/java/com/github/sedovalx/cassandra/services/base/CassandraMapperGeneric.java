package com.github.sedovalx.cassandra.services.base;

import com.datastax.driver.core.Statement;
import com.datastax.driver.mapping.Mapper;

import java.util.concurrent.CompletableFuture;

/**
 * Created by Alexander
 * on 22.03.2016.
 */
public interface CassandraMapperGeneric<Entity> {
    Statement saveQuery(Entity entity);
    void save(Entity entity, Mapper.Option... options);
    CompletableFuture<Void> saveAsync(Entity entity, Mapper.Option... options);

    Statement deleteQuery(Entity entity);
    void delete(Entity entity, Mapper.Option... options);
    CompletableFuture<Void> deleteAsync(Entity entity, Mapper.Option... options);
}
