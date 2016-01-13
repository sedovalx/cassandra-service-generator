package com.github.sedovalx.cassandra.domain.generation

import java.util
import javax.annotation.processing._
import javax.lang.model.SourceVersion
import javax.lang.model.element._
import javax.lang.model.util.{Elements, Types}
import javax.tools.Diagnostic

import com.datastax.driver.mapping.annotations.{ClusteringColumn, Column, PartitionKey, Table}
import com.github.sedovalx.cassandra.domain.generation.annotations.{CassandraService, QueryParams}
import com.github.sedovalx.cassandra.domain.generation.builders._
import com.github.sedovalx.cassandra.domain.generation.metadata.{KeyMetadata, TableMetadata}
import com.github.sedovalx.cassandra.domain.generation.utils.JavaxModelHelper
import com.squareup.javapoet.{ClassName, JavaFile, TypeSpec}

import scala.collection.JavaConversions.{collectionAsScalaIterable, setAsJavaSet}


class CassandraServiceProcessor extends AbstractProcessor {

    private case class TypeSpecEx(spec: TypeSpec, packageName: String)

    protected var typeUtils: Types = _
    protected var elementUtils: Elements = _
    protected var messager: Messager = _
    protected var filer: Filer = _

    override def init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        typeUtils = processingEnv.getTypeUtils
        elementUtils = processingEnv.getElementUtils
        messager = processingEnv.getMessager
        filer = processingEnv.getFiler
    }

    override def process(annotations: util.Set[_ <: TypeElement], roundEnv: RoundEnvironment): Boolean = {
        if (annotations == null || roundEnv == null) {
            error(null, "Arguments exception: annotations and roundEnv should be not null")
            return false
        }

        val elements = getAppropriateElements(roundEnv)
        elements.map { _.asInstanceOf[TypeElement] }
            .map { it =>
                val meta = collectTableMetadata(it)
                (it, meta._1, meta._2)
            }
            .foreach { it =>
                val (element, meta, anno) = it
                try {
                    note(s"Processing ${meta.entityPackage}.${meta.entityName}")
                    val specs = generate(meta, anno, BuildTypes(meta.element))
                    specs.foreach { it => saveType(it.packageName, it.spec) }
                } catch {
                    case e: Exception => error(element, e.getMessage)
                }
            }
        true
    }

    private case class PropertyMeta(element: Element, anno: Column)

    private def collectTableMetadata(classElement: TypeElement): (TableMetadata, CassandraService) = {
        val tableAnnotation = classElement.getAnnotation(classOf[Table])
        val serviceAnnotation = classElement.getAnnotation(getAnnotationClass)

        val excludeKeys: Array[String] = serviceAnnotation.excludeKeys()
        val propertyMetadata = classElement.getEnclosedElements
            .filter(it => it.getKind == ElementKind.FIELD)
            .map { it => PropertyMeta(it, it.getAnnotation(classOf[Column])) }
            .filter { it => it.anno != null }
            .map { it => collectKeyMetadata(it.element.asInstanceOf[VariableElement], it.anno) }
            .filter { it => it.isPartitionKey || it.isClusteringKey }
            .filter { it => !excludeKeys.contains(it.propertyName) }
            .toList
            .sortBy(it => (!it.isPartitionKey, !it.isClusteringKey, it.keyIndex))

        (TableMetadata(
            element = classElement,
            entityName = classElement.getSimpleName.toString,
            entityPackage = getPackageName(classElement.getQualifiedName.toString),
            name = tableAnnotation.name,
            keys = propertyMetadata,
            queryParams = Option(classElement.getAnnotation(classOf[QueryParams])),
            accessorSuffix = serviceAnnotation.accessorSuffix(),
            mapperSuffix = serviceAnnotation.mapperSuffix(),
            serviceSuffix = serviceAnnotation.serviceSuffix(),
            customAccessor = JavaxModelHelper.getAnnotationPropTypeMirror(typeUtils, classElement, getAnnotationClass, "customAccessor")
        ), serviceAnnotation)
    }

    private def collectKeyMetadata(propertyElement: VariableElement, annotation: Column): KeyMetadata = {
        val partitionKeyAnno = propertyElement.getAnnotation(classOf[PartitionKey])
        val clusteringKeyAnno = propertyElement.getAnnotation(classOf[ClusteringColumn])

        KeyMetadata(
            name = annotation.name,
            propertyName = propertyElement.getSimpleName.toString,
            tpe = typeUtils.asElement(propertyElement.asType()).asInstanceOf[TypeElement],
            isPartitionKey = partitionKeyAnno != null,
            isClusteringKey = clusteringKeyAnno != null,
            keyIndex = getKeyIndex(partitionKeyAnno, clusteringKeyAnno),
            queryParams = Option(propertyElement.getAnnotation(classOf[QueryParams]))
        )
    }

    private def getKeyIndex(partitionKey: PartitionKey, clusteringColumn: ClusteringColumn): Int = {
        if (partitionKey != null)
            partitionKey.value()
        else if (clusteringColumn != null)
            clusteringColumn.value()
        else 0
    }

    private def getPackageName(qualifiedName: String): String = {
        qualifiedName.substring(0, qualifiedName.lastIndexOf('.'))
    }

    private def getAppropriateElements(roundEnv: RoundEnvironment): Iterable[Element] = {
        roundEnv.getElementsAnnotatedWith(getAnnotationClass).filter(isElementAppropriate)
    }

    private def isElementAppropriate(element: Element): Boolean = {
        if (element.getKind != ElementKind.CLASS) {
            error(element, "Only classes can be annotated with @%s", getAnnotationClass.getSimpleName)
            return false
        }

        val classElement = element.asInstanceOf[TypeElement]
        if (!classElement.getModifiers.contains(Modifier.PUBLIC)) {
            error(element, "The class [%s] is not public.", classElement.getQualifiedName.toString)
            return false
        }

        if (classElement.getModifiers.contains(Modifier.ABSTRACT)) {
            error(classElement, "The class [%s] is abstract. You can't annotate abstract classes with @%",
                classElement.getQualifiedName, getAnnotationClass.getSimpleName)
            return false
        }

        if (classElement.getAnnotation(classOf[Table]) == null){
            error(classElement, "The class [%s] should be annotated with @%s annotation",
                classElement.getQualifiedName, classOf[Table].getCanonicalName)
        }

        true
    }

    private def generate(metadata: TableMetadata, anno: CassandraService, types: BuildTypes): Iterable[TypeSpecEx] = {
        val maybeAccessor = buildAccessorTypeSpec(metadata, anno, types)
        val maybeMapper = buildMapperTypeSpec(metadata, anno, types)
        val maybeService = if (maybeAccessor.isDefined || maybeMapper.isDefined)
            Some(TypeSpecEx(new ServiceSpecBuilder(maybeAccessor.map(_._2), maybeMapper.map(_._2), metadata).buildSpec(), metadata.entityPackage + ".services"))
        else None

        Seq(maybeAccessor.map(_._1), maybeAccessor.map(_._3), maybeMapper.map(_._1), maybeService).filter(_.isDefined).map(_.get)
    }

    private def buildAccessorTypeSpec(metadata: TableMetadata, anno: CassandraService, types: BuildTypes): Option[(TypeSpecEx, ClassName, TypeSpecEx)] = {
        val accessorsPackageName = metadata.entityPackage + ".accessors"
        val adapterPackageName = accessorsPackageName + ".java8"

        if (anno.generateAccessor()) {
            val accessorSpec = new AccessorSpecBuilder(metadata, types, typeUtils).buildSpec()
            val adapterSpec = new AccessorAdapterSpecBuilder(ClassName.get(metadata.element), accessorsPackageName, accessorSpec, types).buildSpec()
            Some(
                TypeSpecEx(accessorSpec, accessorsPackageName),
                ClassName.get(adapterPackageName, adapterSpec.name),
                TypeSpecEx(adapterSpec, adapterPackageName)
            )
        } else None
    }

    private def buildMapperTypeSpec(metadata: TableMetadata, anno: CassandraService, types: BuildTypes): Option[(TypeSpecEx, ClassName)] = {
        val mappersPackageName = metadata.entityPackage + ".mappers"

        if (anno.generateMapper()) {
            val spec = new MapperSpecBuilder(metadata, types).buildSpec()
            Some(
                TypeSpecEx(spec, mappersPackageName),
                ClassName.get(mappersPackageName, spec.name)
            )
        } else None
    }

    private def getAnnotationClass = classOf[CassandraService]

    override def getSupportedSourceVersion: SourceVersion = {
        SourceVersion.latestSupported()
    }

    override def getSupportedAnnotationTypes: util.Set[String] = Set(getAnnotationClass.getCanonicalName)

    private def error(e: Element, msg: String, args: AnyRef*) = {
        messager.printMessage(
            Diagnostic.Kind.ERROR,
            java.lang.String.format(msg, args),
            e
        )
    }

    private def note(msg: String) {
        messager.printMessage(Diagnostic.Kind.NOTE, s"${getClass.getSimpleName}: $msg")
    }

    private def saveType(packageName: String, spec: TypeSpec): Unit = {
        JavaFile.builder(packageName, spec).build().writeTo(filer)
    }
}
