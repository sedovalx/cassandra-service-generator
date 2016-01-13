package com.github.sedovalx.cassandra.domain.generation.utils

import javax.lang.model.`type`.TypeMirror
import javax.lang.model.element.{AnnotationValue, AnnotationMirror, TypeElement}
import javax.lang.model.util.Types
import scala.collection.JavaConversions._

/**
  * Author alsedov on 12.01.2016
  */
object JavaxModelHelper {
    def getAnnotationMirror(typeElement: TypeElement, clazz: Class[_]): Option[AnnotationMirror] = {
        val className = clazz.getName
        typeElement.getAnnotationMirrors.find(it => it.getAnnotationType.toString.equals(className))
    }

    def getAnnotationValue(annotationMirror: AnnotationMirror, key: String): Option[AnnotationValue] = {
        annotationMirror.getElementValues.entrySet().find { it => it.getKey.getSimpleName.toString.equals(key) }.map { _.getValue }
    }

    def asTypeElement(typeMirror: TypeMirror, typeUtils: Types): TypeElement = typeUtils.asElement(typeMirror).asInstanceOf[TypeElement]

    def getAnnotationPropTypeMirror(typeUtils: Types, typeElement: TypeElement, clazz: Class[_], key: String): Option[TypeElement] = {
        val annotationMirror = getAnnotationMirror(typeElement, clazz)
        val annotationValue = annotationMirror.flatMap(it => getAnnotationValue(it, key))
        annotationValue.map(it => asTypeElement(it.getValue.asInstanceOf[TypeMirror], typeUtils))
    }
}
