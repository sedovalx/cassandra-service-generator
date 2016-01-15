package com.github.sedovalx.cassandra.services.base

import java.util._
import java.util.concurrent.CompletableFuture

import com.datastax.driver.core.{ResultSet, ResultSetFuture}
import com.datastax.driver.mapping.Result
import com.github.sedovalx.cassandra.services.utils.Java8Interop
import com.github.sedovalx.cassandra.services.utils.Java8Interop._
import com.google.common.util.concurrent.ListenableFuture
import net.javacrumbs.futureconverter.java8guava.FutureConverter

/**
  * Author alsedov on 29.12.2015
  */
abstract class AbstractAccessorJava8Adapter[Entity] {
    protected def toCompletableFutureEntity(future: ListenableFuture[Entity]): CompletableFuture[Optional[Entity]] = {
        FutureConverter.toCompletableFuture(future).thenApply(toJavaFunction(toOptional))
    }

    protected def toCompletableFutureResult(future: ListenableFuture[Result[Entity]]): CompletableFuture[Result[Entity]] = {
        FutureConverter.toCompletableFuture(future)
    }

    protected def toCompletableFutureResultSet(future: ResultSetFuture): CompletableFuture[ResultSet] = {
        FutureConverter.toCompletableFuture(future)
    }

    protected def toVoidFutureResultSet(future: ResultSetFuture): CompletableFuture[Void] = {
        toCompletableFutureResultSet(future).thenApply { toJavaFunction(_ => null) }
    }

    protected def toVoidFuture(future: ListenableFuture[Result[Entity]]): CompletableFuture[Void] = {
        FutureConverter.toCompletableFuture(future).thenApply { toJavaFunction(_ => null) }
    }

    protected def toOptional(entity: Entity): Optional[Entity] = {
        Java8Interop.toOptional(entity)
    }
}
