package com.github.sedovalx.cassandra.domain.generation.base

import com.datastax.driver.core.Statement
import com.datastax.driver.mapping.Mapper
import java.util.concurrent.CompletableFuture

/**
  * Created by Alexander
  * on 26.12.2015.
  */
trait CassandraMapperGeneric[T] {
    def saveQuery(entity: T): Statement
    def save(entity: T, options: Mapper.Option*): Unit
    def saveAsync(entity: T, options: Mapper.Option*): CompletableFuture[Void]

    def deleteQuery(entity: T): Statement
    def delete(entity: T, options: Mapper.Option*): Unit
    def deleteAsync(entity: T, options: Mapper.Option*): CompletableFuture[Void]
}
