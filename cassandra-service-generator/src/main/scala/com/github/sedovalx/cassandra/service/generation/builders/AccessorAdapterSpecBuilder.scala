package com.github.sedovalx.cassandra.service.generation.builders

import javax.lang.model.element.Modifier

import com.datastax.driver.core.{Statement, ResultSetFuture, ResultSet}
import com.datastax.driver.mapping.{Result, MappingManager}
import com.github.sedovalx.cassandra.services.base.AbstractAccessorJava8Adapter
import com.google.common.util.concurrent.ListenableFuture
import com.squareup.javapoet._

import scala.collection.JavaConverters._

/**
  * Author alsedov on 12.01.2016
  */
class AccessorAdapterSpecBuilder(entityType: ClassName, accessorPackageName: String, accessorSpec: TypeSpec, types: BuildTypes) {
    def buildSpec(): TypeSpec = {
        val accessorClassName = ClassName.get(accessorPackageName, accessorSpec.name)
        TypeSpec.classBuilder(accessorSpec.name + "Adapter")
            .addModifiers(Modifier.PUBLIC)
            .superclass(ParameterizedTypeName.get(
                ClassName.get(classOf[AbstractAccessorJava8Adapter[_]]),
                entityType
            ))
            .addField(accessorClassName, "accessor", Modifier.PRIVATE)
            .addMethod(buildConstructor(accessorClassName))
            .addMethods(buildMethodSpecs().asJava)
            .build()
    }

    private def buildMethodSpecs(): Seq[MethodSpec] = {
        accessorSpec.methodSpecs.asScala.map(it => buildAdapterMethodSpec(it))
    }

    private def buildConstructor(accessorClassName: ClassName): MethodSpec = {
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(classOf[MappingManager], "mappingManager")
            .addStatement("this.accessor = mappingManager.createAccessor($T.class)", accessorClassName)
            .build()
    }

    private case class ReturnTypeAndStatement(returnType: Option[TypeName], statement: String)

    private def getReturnTypeAndStatement(typeName: ClassName, isDeleteMethod: Boolean, sourceMethodCall: String): ReturnTypeAndStatement = {
        val defaultResult = ReturnTypeAndStatement(Some(typeName), s"return $sourceMethodCall")

        if (typeName.equals(ClassName.get(classOf[ResultSetFuture]))) {
            ReturnTypeAndStatement(
                Some(types.voidFuture),
                s"return toVoidFutureResultSet($sourceMethodCall)"
            )
        } else if (isDeleteMethod) {
            ReturnTypeAndStatement(
                None,
                sourceMethodCall
            )
        } else if (!(typeName.equals(ClassName.get(classOf[ResultSet])) || typeName.equals(ClassName.get(classOf[Statement])))) {
            // tries to pick only domain types
            ReturnTypeAndStatement(
                Some(types.optionalEntity),
                s"return toOptional($sourceMethodCall)"
            )
        } else defaultResult
    }

    private def getReturnTypeAndStatement(typeName: ParameterizedTypeName, isDeleteMethod: Boolean, sourceMethodCall: String): ReturnTypeAndStatement = {
        val defaultResult = ReturnTypeAndStatement(Some(typeName), s"return $sourceMethodCall")

        typeName.rawType match {
            case rt if isGenericResult(rt) && isDeleteMethod =>
                // Result[T] -> void
                ReturnTypeAndStatement(
                    None,
                    sourceMethodCall
                )
            case rt if isGenericResult(rt) => defaultResult
            case fx if isGenericFuture(fx) =>
                // a return type of DataStax accessor's method can't has more than one generic parameter
                val typeParameter = typeName.typeArguments.asScala.head
                typeParameter match {
                    case _: ClassName =>
                        ReturnTypeAndStatement(
                            Some(types.optionalEntityFuture),
                            s"return toCompletableFutureEntity($sourceMethodCall)"
                        )
                    case fxa: ParameterizedTypeName if isGenericResult(fxa.rawType) && isDeleteMethod =>
                        // ListenableFuture[Result[T]] -> CompletableFuture[Void]
                        ReturnTypeAndStatement(
                            Some(types.voidFuture),
                            s"return toVoidFuture($sourceMethodCall)"
                        )
                    case fxa: ParameterizedTypeName if isGenericResult(fxa.rawType) =>
                        // ListenableFuture[Result[T]] -> CompletableFuture[Iterable[T]]
                        ReturnTypeAndStatement(
                            Some(types.entityIterableFuture),
                            s"return toCompletableFutureIterable($sourceMethodCall)"
                        )
                    case _ => defaultResult
                }
            case _ => defaultResult
        }
    }

    private def buildAdapterMethodSpec(source: MethodSpec): MethodSpec = {
        val sourceMethodCall = s"this.accessor.${getMethodCallString(source)}"

        val isDeleteMethod = source.name.startsWith("delete")
        val ReturnTypeAndStatement(returnType, statement) = source.returnType match {
            case cn: ClassName => getReturnTypeAndStatement(cn, isDeleteMethod, sourceMethodCall)
            case ptn: ParameterizedTypeName => getReturnTypeAndStatement(ptn, isDeleteMethod, sourceMethodCall)
        }

        var targetSpec = MethodSpec.methodBuilder(source.name)
            .addModifiers(Modifier.PUBLIC)
            .addParameters(source.parameters)
            .addStatement(statement)

        if (returnType.isDefined){
            targetSpec = targetSpec.returns(returnType.get)
        }

        targetSpec.build()
    }

    private def isGenericResult(tpe: ClassName): Boolean = ClassName.get(classOf[Result[_]]).equals(tpe)

    private def isGenericFuture(tpe: ClassName): Boolean = ClassName.get(classOf[ListenableFuture[_]]).equals(tpe)

    private def getMethodCallString(methodSpec: MethodSpec): String = {
        val params = methodSpec.parameters.asScala.map(p => p.name).mkString(", ")
        s"${methodSpec.name}($params)"
    }
}
