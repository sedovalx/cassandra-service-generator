package com.github.sedovalx.cassandra.service.generation.builders

import java.lang.Iterable
import java.util.Optional
import java.util.concurrent.CompletableFuture
import javax.lang.model.element.TypeElement

import com.datastax.driver.core.ResultSet
import com.datastax.driver.mapping.Result
import com.google.common.util.concurrent.ListenableFuture
import com.squareup.javapoet.{ParameterizedTypeName, ClassName}

/**
  * Created by Alexander 
  * on 31.12.2015.
  */
case class BuildTypes(typeElement: TypeElement) {
    /**
      * T
      */
    val nullableEntity = ClassName.get(typeElement)
    /**
      * Optional<T>
      */
    val optionalEntity = ParameterizedTypeName.get(
        ClassName.get(classOf[Optional[_]]),
        nullableEntity
    )
    /**
      * Result<T>
      */
    val entityResult = ParameterizedTypeName.get(
        ClassName.get(classOf[Result[_]]),
        nullableEntity
    )
    /**
      * Iterable<T>
      */
    val entityIterable = ParameterizedTypeName.get(
        ClassName.get(classOf[Iterable[_]]),
        nullableEntity
    )
    /**
      * ListenableFuture<T>
      */
    val entityGuavaFuture = ParameterizedTypeName.get(
        ClassName.get(classOf[ListenableFuture[_]]),
        nullableEntity
    )
    /**
      * CompletableFuture<Optional<T>>
      */
    val optionalEntityFuture = ParameterizedTypeName.get(
        ClassName.get(classOf[CompletableFuture[_]]),
        ParameterizedTypeName.get(
            ClassName.get(classOf[Optional[_]]),
            nullableEntity
        )
    )
    /**
      * ListenableFuture<Result<T>>
      */
    val entityResultGuavaFuture = ParameterizedTypeName.get(
        ClassName.get(classOf[ListenableFuture[_]]),
            ParameterizedTypeName.get(
                ClassName.get(classOf[Result[_]]),
                nullableEntity
        )
    )
    /**
      * CompletableFuture<Result<T>>
      */
    val entityResultFuture = ParameterizedTypeName.get(
        ClassName.get(classOf[CompletableFuture[_]]),
            ParameterizedTypeName.get(
                ClassName.get(classOf[Result[_]]),
                nullableEntity
        )
    )
    /**
      * CompletableFuture<Void>
      */
    val voidFuture = ParameterizedTypeName.get(
        ClassName.get(classOf[CompletableFuture[_]]),
        ClassName.get(classOf[Void])
    )

  /**
    * CompletableFuture<ResultSet>
    */
  val resultSetFuture = ParameterizedTypeName.get(
        ClassName.get(classOf[CompletableFuture[_]]),
        ClassName.get(classOf[ResultSet])
    )
}
