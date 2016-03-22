package com.github.sedovalx.cassandra.services.base;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.mapping.Result;
import com.google.common.util.concurrent.ListenableFuture;
import net.javacrumbs.futureconverter.java8guava.FutureConverter;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Alexander
 * on 22.03.2016.
 */
public abstract class AbstractAccessorJava8Adapter<Entity> {
    protected CompletableFuture<Optional<Entity>> toCompletableFutureEntity(ListenableFuture<Entity> future) {
        return FutureConverter.toCompletableFuture(future).thenApply(Optional::ofNullable);
    }

    protected CompletableFuture<Result<Entity>> toCompletableFutureResult(ListenableFuture<Result<Entity>> future) {
        return FutureConverter.toCompletableFuture(future);
    }

    protected CompletableFuture<ResultSet> toCompletableFutureResultSet(ResultSetFuture future) {
        return FutureConverter.toCompletableFuture(future);
    }

    protected CompletableFuture<Void> toVoidFutureResultSet(ResultSetFuture future) {
        return FutureConverter.toCompletableFuture(future).thenApply(rows -> null);
    }

    protected CompletableFuture<Void> toVoidFuture(ListenableFuture<Result<Entity>> future) {
        return FutureConverter.toCompletableFuture(future).thenApply(entities -> null);
    }

    protected Optional<Entity> toOptional(Entity entity) {
        return Optional.ofNullable(entity);
    }
}
