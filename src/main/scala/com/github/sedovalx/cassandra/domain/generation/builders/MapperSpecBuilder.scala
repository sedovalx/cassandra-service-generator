package com.github.sedovalx.cassandra.domain.generation.builders

import javax.lang.model.element.Modifier

import com.datastax.driver.core.Statement
import com.datastax.driver.mapping.{MappingManager, Mapper}
import com.squareup.javapoet._
import com.github.sedovalx.cassandra.domain.generation.base.CassandraMapperGenericImpl
import com.github.sedovalx.cassandra.domain.generation.metadata.{TableMetadata, KeyMetadata}
import com.github.sedovalx.cassandra.domain.generation.utils.MethodSpecUtils

import scala.collection.JavaConversions.asJavaIterable
import com.github.sedovalx.cassandra.domain.generation.utils.SeqExtensions.asParameterSpecs

/**
  * Created by Alexander 
  * on 02.01.2016.
  */
class MapperSpecBuilder(metadata: TableMetadata, types: BuildTypes) {
    def buildSpec(): TypeSpec = {
        val methodSpecs = Seq(
            buildConstructor(),
            buildGetQuery(metadata.keys, types),
            buildGetMethod(metadata.keys, types),
            buildGetAsyncMethod(metadata.keys, types),
            buildDeleteQuery(metadata.keys, types),
            buildDeleteMethod(metadata.keys, types),
            buildDeleteAsyncMethod(metadata.keys, types)
        )
        TypeSpec.classBuilder(metadata.entityName + metadata.mapperSuffix)
            .superclass(ParameterizedTypeName.get(
                ClassName.get(classOf[CassandraMapperGenericImpl[_]]),
                ClassName.get(metadata.element)
            ))
            .addModifiers(Modifier.PUBLIC)
            .addMethods(methodSpecs)
            .build()
    }

    private def buildConstructor(): MethodSpec = {
        MethodSpec.constructorBuilder()
            .addParameter(classOf[MappingManager], "mappingManager")
            .addStatement("super(mappingManager)")
            .build()
    }

    private def buildGetQuery(keys: Seq[KeyMetadata], types: BuildTypes): MethodSpec = {
        val parameters = getParametersString(keys)
        MethodSpecUtils.public("getQuery")
            .addParameters(keys.asParameterSpecs)
            .returns(classOf[Statement])
            .addStatement(s"return this.mapper().getQuery($parameters)")
            .build()
    }

    private def buildGetMethod(keys: Seq[KeyMetadata], types: BuildTypes): MethodSpec = {
        MethodSpecUtils.public("get")
            .addParameters(keys.asParameterSpecs)
            .addParameter(buildMapperOptionParam())
            .varargs()
            .returns(types.optionalEntity)
            .addStatement(s"Object[] args = combineArgumentsToVarargs(options, ${getParametersString(keys)})")
            .addStatement("return getOptionalEntity(args)")
            .build()
    }

    private def buildGetAsyncMethod(keys: Seq[KeyMetadata], types: BuildTypes): MethodSpec = {
        MethodSpecUtils.public("getAsync")
            .addParameters(keys.asParameterSpecs)
            .addParameter(buildMapperOptionParam())
            .varargs()
            .returns(types.optionalEntityFuture)
            .addStatement(s"Object[] args = combineArgumentsToVarargs(options, ${getParametersString(keys)})")
            .addStatement("return getOptionalFuture(args)")
            .build()
    }

    private def buildDeleteQuery(keys: Seq[KeyMetadata], types: BuildTypes): MethodSpec = {
        val parameters = getParametersString(keys)
        MethodSpecUtils.public("deleteQuery")
            .addParameters(keys.asParameterSpecs)
            .returns(ClassName.get(classOf[Statement]))
            .addStatement(s"return this.mapper().deleteQuery($parameters)")
            .build()
    }

    private def buildDeleteMethod(keys: Seq[KeyMetadata], types: BuildTypes): MethodSpec = {
        MethodSpecUtils.public("delete")
            .addParameters(keys.asParameterSpecs)
            .addParameter(buildMapperOptionParam())
            .varargs()
            .addStatement(s"Object[] args = combineArgumentsToVarargs(options, ${getParametersString(keys)})")
            .addStatement("this.mapper().delete(args)")
            .build()
    }

    private def buildDeleteAsyncMethod(keys: Seq[KeyMetadata], types: BuildTypes): MethodSpec = {
        MethodSpecUtils.public("deleteAsync")
            .addParameters(keys.asParameterSpecs)
            .addParameter(buildMapperOptionParam())
            .varargs()
            .returns(types.voidFuture)
            .addStatement(s"Object[] args = combineArgumentsToVarargs(options, ${getParametersString(keys)})")
            .addStatement("return deleteAsyncWithMapper(args)")
            .build()
    }

    private def getParametersString(keys: Seq[KeyMetadata], additional: String*): String = {
        keys.map(_.propertyName).union(additional).mkString(", ")
    }

    private def buildMapperOptionParam(): ParameterSpec = {
        ParameterSpec.builder(classOf[Array[Mapper.Option]], "options").build()
    }
}
