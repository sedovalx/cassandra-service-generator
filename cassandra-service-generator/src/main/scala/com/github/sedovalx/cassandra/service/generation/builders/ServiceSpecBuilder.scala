package com.github.sedovalx.cassandra.service.generation.builders

import java.util.function.Supplier
import javax.lang.model.element.Modifier

import com.datastax.driver.mapping.MappingManager
import com.squareup.javapoet.{ParameterizedTypeName, MethodSpec, ClassName, TypeSpec}
import com.github.sedovalx.cassandra.service.generation.metadata.TableMetadata

import scala.collection.JavaConverters._

/**
  * Author alsedov on 12.01.2016
  */
class ServiceSpecBuilder(accessor: Option[ClassName], mapper: Option[ClassName], tableMetadata: TableMetadata) {
    def buildSpec(): TypeSpec = {
        var methodSpecs = Seq.empty[MethodSpec]
        val mappingSupplierType = ParameterizedTypeName.get(
            ClassName.get(classOf[Supplier[_]]),
            ClassName.get(classOf[MappingManager])
        )
        var specBuilder = TypeSpec.classBuilder(tableMetadata.entityName + tableMetadata.serviceSuffix)
            .addModifiers(Modifier.PUBLIC)
            .addField(mappingSupplierType, "mappingManagerSupplier", Modifier.PRIVATE)

        val ctrBuilder = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(mappingSupplierType, "mappingManagerSupplier")
            .addStatement("this.mappingManagerSupplier = mappingManagerSupplier")

        if (accessor.isDefined){
            methodSpecs = methodSpecs :+ MethodSpec.methodBuilder("accessor")
                .addModifiers(Modifier.PUBLIC)
                .returns(accessor.get)
                .beginControlFlow("if (this.accessor == null)")
                .addStatement("this.accessor = new $T(this.mappingManagerSupplier.get())", accessor.get)
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
                .addStatement("this.mapper = new $T(this.mappingManagerSupplier.get())", mapper.get)
                .endControlFlow()
                .addStatement("return this.mapper")
                .build()
            specBuilder = specBuilder.addField(mapper.get, "mapper", Modifier.PRIVATE)
        }

        specBuilder.addMethod(ctrBuilder.build()).addMethods(methodSpecs.asJava).build()
    }
}
