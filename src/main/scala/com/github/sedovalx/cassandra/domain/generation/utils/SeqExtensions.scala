package com.github.sedovalx.cassandra.domain.generation.utils

import com.squareup.javapoet.{ClassName, ParameterSpec}
import com.github.sedovalx.cassandra.domain.generation.metadata.KeyMetadata

import scala.language.implicitConversions
import scala.collection.JavaConversions.asJavaIterable

/**
  * Created by Alexander 
  * on 31.12.2015.
  */
object SeqExtensions {
    class SequenceEx[T](list: Seq[T]) {
      /**
        * @example
        * <code>
        * (a, b, c).spread((d, e)) --> ((d, e), (d, e, a), (d, e, a, b), (d, e, a, b, c))
        * </code>
        */
        def spread(base: Seq[T]): Seq[Seq[T]] = {
            list.foldLeft(Seq(base)) { (total, item) =>
                total :+ (total.lastOption match {
                    case Some(last) => last :+ item
                    case None => Seq(item)
                })
            }
        }
    }

    class SeqOfKeyMetadata(keys: Seq[KeyMetadata]) {
        def asParameterSpecs: java.lang.Iterable[ParameterSpec] = {
            val result = keys.map { it => ParameterSpec.builder(ClassName.get(it.tpe), it.propertyName).build() }
            result
        }
    }

    implicit def extendedSeq[T](list: Seq[T]):SequenceEx[T] = new SequenceEx[T](list)

    implicit def asParameterSpecs(keys: Seq[KeyMetadata]): SeqOfKeyMetadata = new SeqOfKeyMetadata(keys)
}
