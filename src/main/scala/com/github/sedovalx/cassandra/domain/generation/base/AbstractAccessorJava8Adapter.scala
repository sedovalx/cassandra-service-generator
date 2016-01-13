package com.github.sedovalx.cassandra.domain.generation.base

import com.datastax.driver.mapping.Result
import com.google.common.util.concurrent.ListenableFuture
import net.javacrumbs.futureconverter.java8guava.FutureConverter
import java.util._
import java.util.concurrent.CompletableFuture
import com.github.sedovalx.cassandra.domain.generation.utils.Java8Interop
import Java8Interop.toJavaFunction

/**
  * Author alsedov on 29.12.2015
  */
abstract class AbstractAccessorJava8Adapter[Entity] {
    protected def singleToCompletableFuture(future: ListenableFuture[Entity]): CompletableFuture[Optional[Entity]] = {
        FutureConverter.toCompletableFuture(future).thenApply(toJavaFunction(toOptional))
    }

    protected def listToCompletableFuture(future: ListenableFuture[Result[Entity]]): CompletableFuture[java.lang.Iterable[Entity]] = {
        FutureConverter.toCompletableFuture(future).thenApply(toJavaFunction((it: Result[Entity]) => it))
    }

    protected def toVoidFuture(future: ListenableFuture[Result[Entity]]): CompletableFuture[Void] = {
        FutureConverter.toCompletableFuture(future).thenApply { toJavaFunction(_ => null) }
    }

    protected def toOptional(entity: Entity): Optional[Entity] = {
        Java8Interop.toOptional(entity)
    }
}
