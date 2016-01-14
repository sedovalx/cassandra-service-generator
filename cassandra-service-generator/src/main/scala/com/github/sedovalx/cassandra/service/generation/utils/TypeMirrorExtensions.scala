package com.github.sedovalx.cassandra.service.generation.utils

import javax.lang.model.`type`.TypeMirror

import com.squareup.javapoet.{ParameterizedTypeName, ClassName, TypeName}

import scala.language.implicitConversions

/**
  * Author alsedov on 14.01.2016
  */
object TypeMirrorExtensions {

    private val parametersPattern = "([\\w\\.]+<(?:[\\w\\.]+,)+[\\w\\.]+>)|([\\w\\.-<->]+)".r

    private case class CanonicalName(packageName: Option[String], name: String) {
        def toTypeName: TypeName = {
            if (packageName.isDefined) {
                toClassName
            } else {
                name match {
                    case "boolean" => TypeName.BOOLEAN
                    case "byte" => TypeName.BYTE
                    case "short" => TypeName.SHORT
                    case "int" => TypeName.INT
                    case "long" => TypeName.LONG
                    case "char" => TypeName.CHAR
                    case "float" => TypeName.FLOAT
                    case "double" => TypeName.DOUBLE
                    case _ => TypeName.OBJECT
                }
            }
        }

        def toClassName: ClassName = ClassName.get(packageName.get, name)
    }

    private case class GenericTypeName(name: CanonicalName, parameters: Seq[GenericTypeName]) {
        def toTypeName: TypeName = {
            if (parameters.isEmpty) {
                name.toTypeName
            } else {
                ParameterizedTypeName.get(
                    name.toClassName,
                    parameters.map { _.toTypeName }:_*
                )
            }
        }

        def print(offset: Int = 0): Unit = {
            val offsetStr = " " * offset
            println(offsetStr + s"(${name.packageName.getOrElse("")}, ${name.name})")
            parameters.foreach(p => p.print(offset + 4))
        }
    }

    private def parseName(name: String): GenericTypeName = {
        val preparedName = name.replaceAll(" ", "")
        val firstGenericIndex = preparedName.indexOf('<')
        if (firstGenericIndex < 0) {
            GenericTypeName(
                splitPackageAndName(preparedName),
                Nil
            )
        } else {
            val rawName = splitPackageAndName(preparedName.take(firstGenericIndex))
            val typeParametersString = preparedName.takeRight(preparedName.length - firstGenericIndex - 1).dropRight(1)
            val parameters = parametersPattern.findAllMatchIn(typeParametersString).map(m => parseName(m.toString()))
            GenericTypeName(rawName, parameters.toList)
        }
    }

    private def splitPackageAndName(name: String): CanonicalName = {
        val lastDot = name.lastIndexOf('.')
        if (lastDot >= 0)
            CanonicalName(Some(name.take(lastDot)), name.takeRight(name.length - lastDot - 1))
        else
            CanonicalName(None, name)
    }

    class TypeMirrorExtensions(typeMirror: TypeMirror) {
        val canonicalName = typeMirror.toString

      /**
        * Tries to convert string representation of the TypeMirror to a TypeName
        * @return TypeName descendant or exception
        */
        def asTypeName(): TypeName = {
            parseName(canonicalName).toTypeName
        }
    }

    implicit def asTypeMirrorExtensions(typeMirror: TypeMirror): TypeMirrorExtensions = new TypeMirrorExtensions(typeMirror: TypeMirror)
}
