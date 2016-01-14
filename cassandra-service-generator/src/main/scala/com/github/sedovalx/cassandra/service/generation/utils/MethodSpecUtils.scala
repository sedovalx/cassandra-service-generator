package com.github.sedovalx.cassandra.service.generation.utils

import javax.lang.model.element.Modifier

import com.squareup.javapoet.MethodSpec

/**
  * Created by Alexander 
  * on 02.01.2016.
  */
object MethodSpecUtils {
    def public(name: String): MethodSpec.Builder = MethodSpec.methodBuilder(name).addModifiers(Modifier.PUBLIC)
}
