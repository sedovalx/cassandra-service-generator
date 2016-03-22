package com.github.sedovalx.cassandra.services.base;

import com.datastax.driver.core.Statement;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import net.javacrumbs.futureconverter.java8guava.FutureConverter;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Alexander
 * on 22.03.2016.
 */
public abstract class CassandraMapperGenericImpl<Entity> implements CassandraMapperGeneric<Entity> {
    private final Mapper<Entity> mapper;

    protected Mapper<Entity> mapper() {
        return mapper;
    }

    public CassandraMapperGenericImpl(MappingManager mappingManager) {
        this.mapper = mappingManager.mapper(getDomainClass());
    }

    @Override
    public Statement saveQuery(Entity entity) {
        return mapper.saveQuery(entity);
    }

    @Override
    public void save(Entity entity, Mapper.Option... options) {
        mapper.save(entity, options);
    }

    @Override
    public CompletableFuture<Void> saveAsync(Entity entity, Mapper.Option... options) {
        return FutureConverter.toCompletableFuture(mapper.saveAsync(entity, options));
    }

    @Override
    public Statement deleteQuery(Entity entity) {
        return mapper.deleteQuery(entity);
    }

    @Override
    public void delete(Entity entity, Mapper.Option... options) {
        mapper.delete(entity, options);
    }

    @Override
    public CompletableFuture<Void> deleteAsync(Entity entity, Mapper.Option... options) {
        return FutureConverter.toCompletableFuture(mapper.deleteAsync(entity, options));
    }

    protected CompletableFuture<Void> deleteAsyncWithMapper(Object[] args) {
        return FutureConverter.toCompletableFuture(mapper.deleteAsync(args));
    }

    protected Object[] combineArgumentsToVarargs(Object[] varargs, Object... args) {
        Object[] array = new Object[varargs.length + args.length];
        System.arraycopy(varargs, 0, array, 0, varargs.length);
        System.arraycopy(args, varargs.length, array, varargs.length, varargs.length + args.length - varargs.length);
        return array;
    }

    protected Optional<Entity> getOptionalEntity(Object... args) {
        return Optional.ofNullable(mapper.get(args));
    }

    protected CompletableFuture<Optional<Entity>> getOptionalFuture(Object... args) {
        return FutureConverter.toCompletableFuture(mapper.getAsync(args)).thenApply(Optional::ofNullable);
    }

    private Class<Entity> getDomainClass(){
        return (Class<Entity>)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
