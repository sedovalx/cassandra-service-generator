package com.github.sedovalx.cassandra.service.generation.builders

import javax.lang.model.element.Modifier

import com.datastax.driver.mapping.MappingManager
import com.squareup.javapoet.{MethodSpec, ClassName, TypeSpec}
import com.github.sedovalx.cassandra.service.generation.metadata.TableMetadata

import scala.collection.JavaConverters._

/**
  * Author alsedov on 12.01.2016
  */
class ServiceSpecBuilder(accessor: Option[ClassName], mapper: Option[ClassName], tableMetadata: TableMetadata) {
    def buildSpec(): TypeSpec = {
        var methodSpecs = Seq.empty[MethodSpec]
        val ctrBuilder = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(classOf[MappingManager], "mappingManager")
            .addStatement("this.mappingManager = mappingManager")

        var specBuilder = TypeSpec.classBuilder(tableMetadata.entityName + tableMetadata.serviceSuffix)
            .addModifiers(Modifier.PUBLIC)
            .addField(classOf[MappingManager], "mappingManager", Modifier.PRIVATE)

        if (accessor.isDefined){
            methodSpecs = methodSpecs :+ MethodSpec.methodBuilder("accessor")
                .addModifiers(Modifier.PUBLIC)
                .returns(accessor.get)
                .beginControlFlow("if (this.accessor == null)")
                .addStatement("this.accessor = new $T(this.mappingManager)", accessor.get)
                .endControlFlow()
                .addStatement("return this.accessor")
                .build()
            specBuilder = specBuilder.addField(accessor.get, "accessor", Modifier.PRIVATE)
        }

        if (mapper.isDefined) {
            methodSpecs = methodSpecs :+ MethodSpec.methodBuilder("mapper")
                .addModifiers(Modifier.PUBLIC)
                .returns(mapper.get)
                .beginControlFlow("if (this.mapper == null)")
                .addStatement("this.mapper = new $T(this.mappingManager)", mapper.get)
                .endControlFlow()
                .addStatement("return this.mapper")
                .build()
            specBuilder = specBuilder.addField(mapper.get, "mapper", Modifier.PRIVATE)
        }

        specBuilder.addMethod(ctrBuilder.build()).addMethods(methodSpecs.asJava).build()
    }
}
