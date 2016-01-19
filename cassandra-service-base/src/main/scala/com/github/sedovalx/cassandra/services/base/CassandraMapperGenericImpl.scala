package com.github.sedovalx.cassandra.services.base

import java.lang.reflect.ParameterizedType
import java.util.Optional
import java.util.concurrent.CompletableFuture

import com.datastax.driver.core.Statement
import com.datastax.driver.mapping.{Mapper, MappingManager}
import net.javacrumbs.futureconverter.java8guava.FutureConverter

import com.github.sedovalx.cassandra.services.utils.Java8Interop.{toJavaFunction, toOptional}

import scala.annotation.varargs

/**
  * Created by Alexander
  * on 26.12.2015.
  */
abstract class CassandraMapperGenericImpl[Entity](mappingManager: MappingManager) extends CassandraMapperGeneric[Entity] {

    protected val mapper: Mapper[Entity] = mappingManager.mapper(getDomainClass)

    override def saveQuery(entity: Entity): Statement = mapper.saveQuery(entity)

    @varargs
    override def save(entity: Entity, options: Mapper.Option*): Unit = mapper.save(entity, options:_*)

    @varargs
    override def saveAsync(entity: Entity, options: Mapper.Option*): CompletableFuture[Void] = {
        FutureConverter.toCompletableFuture(mapper.saveAsync(entity, options:_*))
    }

    override def deleteQuery(entity: Entity): Statement = mapper.deleteQuery(entity)

    @varargs
    override def delete(entity: Entity, options: Mapper.Option*) = mapper.delete(entity, options:_*)

    @varargs
    override def deleteAsync(entity: Entity, options: Mapper.Option*): CompletableFuture[Void] = {
        FutureConverter.toCompletableFuture(mapper.deleteAsync(entity, options:_*))
    }

    private def getDomainClass: Class[Entity] = {
        this.getClass.getGenericSuperclass.asInstanceOf[ParameterizedType].getActualTypeArguments.head.asInstanceOf[Class[Entity]]
    }

    protected def deleteAsyncWithMapper(args: Array[AnyRef]): CompletableFuture[Void] = {
        FutureConverter.toCompletableFuture(mapper.deleteAsync(args:_*))
    }

    @varargs
    protected def combineArgumentsToVarargs(varargs: Array[AnyRef], args: AnyRef*): Array[AnyRef] = {
        (args ++ varargs).toArray
    }

    @varargs
    protected def getOptionalEntity(objects: AnyRef*): Optional[Entity] = Optional.ofNullable(mapper.get(objects:_*))

    @varargs
    protected def getOptionalFuture(objects: AnyRef*): CompletableFuture[Optional[Entity]] = {
        FutureConverter.toCompletableFuture(mapper.getAsync(objects:_*)).thenApply(toJavaFunction(toOptional))
    }
}
