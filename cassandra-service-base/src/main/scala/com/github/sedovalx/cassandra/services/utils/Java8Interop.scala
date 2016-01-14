package com.github.sedovalx.cassandra.services.utils

import java.util.Optional
import java.util.function.Function

import scala.language.implicitConversions

/**
  * Created by Alexander 
  * on 02.01.2016.
  */
object Java8Interop {
    implicit def toOptional[Entity](value: Entity): Optional[Entity] = {
        if (value == null) Optional.empty() else Optional.of(value)
    }

    implicit def toJavaFunction[A, B](f: (A) => B): Function[A, B] = new Function[A, B] {
        override def apply(t: A): B = f(t)
    }
}
