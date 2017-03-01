package com.github.sedovalx.cassandra.service.generation.builders

import javax.lang.model.`type`.{TypeMirror, PrimitiveType}
import javax.lang.model.element.{TypeElement, ElementKind, ExecutableElement, Modifier}
import javax.lang.model.util.Types

import com.datastax.driver.mapping.annotations.{Accessor, Query, QueryParameters}
import com.squareup.javapoet._
import com.github.sedovalx.cassandra.service.generation.cql.CqlBuilder
import com.github.sedovalx.cassandra.service.generation.metadata.{KeyMetadata, TableMetadata}
import com.github.sedovalx.cassandra.service.generation.utils.{JavaxModelHelper, MethodSpecUtils}
import com.github.sedovalx.cassandra.service.generation.utils.SeqExtensions.{asParameterSpecs, extendedSeq}
import com.github.sedovalx.cassandra.service.generation.utils.TypeMirrorExtensions.asTypeMirrorExtensions

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.language.implicitConversions

class AccessorSpecBuilder(metadata: TableMetadata, types: BuildTypes, typeUtils: Types) {

    protected class MethodSpecBuilder(spec: MethodSpec.Builder) {
        def makeAbstract() = spec.addModifiers(Modifier.ABSTRACT)
    }

    protected implicit def asMethodSpecBuilder(spec: MethodSpec.Builder): MethodSpecBuilder = new MethodSpecBuilder(spec)

    def buildSpec(): TypeSpec = {
        val methodSpecs: java.lang.Iterable[MethodSpec] = getMethodsToBuild(metadata, types).map { it => it.build() }

        TypeSpec.interfaceBuilder(metadata.entityName + metadata.accessorSuffix)
            .addModifiers(Modifier.PUBLIC)
            .addMethods(methodSpecs)
            .addAnnotation(classOf[Accessor])
            .build()
    }

    private def getMethodsToBuild(metadata: TableMetadata, types: BuildTypes): Seq[MethodSpec.Builder] = {
        val partitionKeys = metadata.keys.filter { it => it.isPartitionKey }.sortBy { it => it.keyIndex }
        val clusteringKeys = metadata.keys.filter { it => it.isClusteringKey }.sortBy { it => it.keyIndex }
        val keysCount = partitionKeys.size + clusteringKeys.size

        val getMethods = clusteringKeys.spread(partitionKeys).flatMap { props =>
            Seq(
                buildGet(types, props, keysCount == props.size),
                buildGetAsync(types, props, keysCount == props.size)
            )
        }

        Seq(
            buildGetAll(types),
            buildGetAllAsync(types),
            buildDeleteByPartition(partitionKeys, types),
            buildDeleteByPartitionAsync(partitionKeys, types),
            buildDeleteAll(types),
            buildDeleteAllAsync(types)
        ).union(getMethods).union(buildCustomMethods())
    }

    /********************** Get *********************/
    private def buildGet(types: BuildTypes, properties: Seq[KeyMetadata], isSingle: Boolean): MethodSpec.Builder = {
        val spec = MethodSpecUtils.public("get").addParameters(properties.asParameterSpecs)
            .addAnnotation(buildGetQueryAnnotation(properties, isSingle))
            .returns(if (isSingle) types.nullableEntity else types.entityResult)
            .makeAbstract()

        val lastKey = properties.last
        val qp = lastKey.queryParams

        qp match {
            case Some(p) => spec.addAnnotation(
                    AnnotationSpec.builder(classOf[QueryParameters])
                        .addMember("consistency", "$S", p.consistency)
                        .addMember("fetchSize", p.fetchSize.toString)
                        .addMember("tracing", p.tracing.toString)
                        .build()
                )
            case None => spec
        }
    }

    private def buildGetAsync(types: BuildTypes, properties: Seq[KeyMetadata], isSingle: Boolean): MethodSpec.Builder = {
        MethodSpecUtils.public("getAsync").addParameters(properties.asParameterSpecs)
            .addAnnotation(buildGetQueryAnnotation(properties, isSingle))
            .returns(if (isSingle) types.entityGuavaFuture else types.entityResultGuavaFuture)
            .makeAbstract()
    }

    /********************** GetAll *********************/
    private def buildGetAll(types: BuildTypes): MethodSpec.Builder = {
        MethodSpecUtils.public("getAll")
            .addAnnotation(buildGetQueryAnnotation(isSingle = false))
            .returns(types.entityResult)
            .makeAbstract()
    }

    private def buildGetAllAsync(types: BuildTypes): MethodSpec.Builder = {
        MethodSpecUtils.public("getAllAsync")
            .addAnnotation(buildGetQueryAnnotation(isSingle = false))
            .returns(types.entityResultGuavaFuture)
            .makeAbstract()
    }

    /********************** DeleteAll *********************/
    private def buildDeleteAll(types: BuildTypes): MethodSpec.Builder = {
        MethodSpecUtils.public("deleteAll")
            .addAnnotation(buildDeleteAllQueryAnnotation())
            .returns(types.entityResult)
            .makeAbstract()
    }

    private def buildDeleteAllAsync(types: BuildTypes): MethodSpec.Builder = {
        MethodSpecUtils.public("deleteAllAsync")
            .addAnnotation(buildDeleteAllQueryAnnotation())
            .returns(types.entityResultGuavaFuture)
            .makeAbstract()
    }

    private def buildDeleteByPartition(partitionKeys: List[KeyMetadata], types: BuildTypes): MethodSpec.Builder = {
        MethodSpecUtils.public("deleteByPartitionKey").addParameters(partitionKeys.asParameterSpecs)
          .addAnnotation(buildDeleteQueryAnnotation(partitionKeys))
          .returns(types.entityResult)
          .makeAbstract()
    }

    private def buildDeleteByPartitionAsync(partitionKeys: List[KeyMetadata], types: BuildTypes): MethodSpec.Builder = {
        MethodSpecUtils.public("deleteByPartitionKeyAsync").addParameters(partitionKeys.asParameterSpecs)
          .addAnnotation(buildDeleteQueryAnnotation(partitionKeys))
          .returns(types.entityResultGuavaFuture)
          .makeAbstract()
    }

    private def buildGetQueryAnnotation(whereItems: Seq[KeyMetadata] = Nil, isSingle: Boolean): AnnotationSpec = {
        val keysOrdered = whereItems.sortBy(it => (!it.isPartitionKey, !it.isClusteringKey, it.keyIndex)).map(_.name)
        val cql = CqlBuilder.select(metadata.name, keysOrdered, if (isSingle) Some(1) else None)
        AnnotationSpec.builder(classOf[Query]).addMember("value", s""""$cql"""").build()
    }

    private def buildDeleteAllQueryAnnotation(): AnnotationSpec = {
        AnnotationSpec.builder(classOf[Query]).addMember("value", s""""truncate ${metadata.name}"""").build()
    }

    private def buildDeleteQueryAnnotation(whereItems: Seq[KeyMetadata]): AnnotationSpec = {
        val keysOrdered = whereItems.map(it => it.name + " = ?").mkString(" AND ")
        val cql = s"delete from ${metadata.name} WHERE $keysOrdered"
        AnnotationSpec.builder(classOf[Query]).addMember("value", s""""$cql"""").build()
    }

    private def buildCustomMethods(): Seq[MethodSpec.Builder] = {
        metadata.customAccessor match {
            case Some(accessorElement) =>
                accessorElement.getEnclosedElements
                    .filter { it => it.getKind == ElementKind.METHOD }
                    .map { it => (it, it.getAnnotation(classOf[Query])) }
                    .filter { it => it._2 != null }
                    .map { it => buildMethodSpec(it._1.asInstanceOf[ExecutableElement], it._2)}
                    .toSeq
            case None => Nil
        }
    }

    private def buildMethodSpec(element: ExecutableElement, anno: Query): MethodSpec.Builder = {
        val paramSpecs = element.getParameters.asScala.map { it =>
            val paramName = it.getSimpleName.toString
            val paramType = it.asType().asTypeName()
            ParameterSpec.builder(paramType, paramName).build()
        }
        MethodSpec.methodBuilder(element.getSimpleName.toString)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameters(paramSpecs)
            .returns(element.getReturnType.asTypeName())
            .addAnnotation(
                AnnotationSpec.builder(classOf[Query])
                    .addMember("value", s""""${anno.value()}"""")
                    .build()
            )
    }
}

