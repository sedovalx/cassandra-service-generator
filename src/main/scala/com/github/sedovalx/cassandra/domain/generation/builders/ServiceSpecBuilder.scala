package com.github.sedovalx.cassandra.domain.generation.builders

import javax.lang.model.element.Modifier

import com.squareup.javapoet.{MethodSpec, ClassName, TypeSpec}
import com.github.sedovalx.cassandra.domain.generation.metadata.TableMetadata

import scala.collection.JavaConverters._

/**
  * Author alsedov on 12.01.2016
  */
class ServiceSpecBuilder(accessor: Option[ClassName], mapper: Option[ClassName], tableMetadata: TableMetadata) {
    def buildSpec(): TypeSpec = {
        var methodSpecs = Seq.empty[MethodSpec]
        var ctrBuilder = MethodSpec.constructorBuilder()
        var specBuilder = TypeSpec.classBuilder(tableMetadata.entityName + tableMetadata.serviceSuffix).addModifiers(Modifier.PUBLIC)

        if (accessor.isDefined){
            ctrBuilder = ctrBuilder.addParameter(accessor.get, "accessor").addStatement("this.accessor = accessor")
            methodSpecs = methodSpecs :+ MethodSpec.methodBuilder("accessor")
                .addModifiers(Modifier.PUBLIC)
                .returns(accessor.get)
                .addStatement("return this.accessor")
                .build()
            specBuilder = specBuilder.addField(accessor.get, "accessor")
        }

        if (mapper.isDefined) {
            ctrBuilder = ctrBuilder.addParameter(mapper.get, "mapper").addStatement("this.mapper = mapper")
            methodSpecs = methodSpecs :+ MethodSpec.methodBuilder("mapper")
                .addModifiers(Modifier.PUBLIC)
                .returns(mapper.get)
                .addStatement("return this.mapper")
                .build()
            specBuilder = specBuilder.addField(mapper.get, "mapper")
        }

        specBuilder.addMethod(ctrBuilder.build()).addMethods(methodSpecs.asJava).build()
    }
}
