package com.github.sedovalx.cassandra.service.generation.utils

import scala.language.implicitConversions

/**
  * Author alsedov on 14.01.2016
  */
object AnyExtensions {
    class TypeCast(x : Any) {
        def is[T : Manifest] = manifest.runtimeClass.isInstance(x)
        def as[T : Manifest] : Option[T] = if (manifest.runtimeClass.isInstance(x)) Some(x.asInstanceOf[T]) else None
    }

    implicit def asTypeCast(x:Any): TypeCast = new TypeCast(x)
}
