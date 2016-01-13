package com.github.sedovalx.cassandra.domain.generation.builders

import javax.lang.model.element.Modifier

import com.datastax.driver.mapping.{Result, MappingManager}
import com.google.common.util.concurrent.ListenableFuture
import com.squareup.javapoet._
import com.github.sedovalx.cassandra.domain.generation.base.AbstractAccessorJava8Adapter

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

    private def buildAdapterMethodSpec(source: MethodSpec): MethodSpec = {
        val sourceMethodCall = s"this.accessor.${getMethodCallString(source)}"
        def defaultReturnAndStatement(t: TypeName) = (Some(t), s"return $sourceMethodCall")

        val isDeleteMethod = source.name.startsWith("delete")
        val (returnType, statement) = source.returnType match {
            // T -> Optional[T]
            case _: ClassName =>
                (
                    Some(types.optionalEntity),
                    s"return toOptional($sourceMethodCall)"
                )
            case t: ParameterizedTypeName =>
                t.rawType match {
                    // Result[T] -> Iterable[T]
                    case rt if isGenericResult(rt) =>
                        if (isDeleteMethod)
                            (
                                None,
                                sourceMethodCall
                            )
                        else
                            (
                                Some(types.entityIterable),
                                s"return $sourceMethodCall"
                            )
                    case fx if isGenericFuture(fx) =>
                        t.typeArguments.asScala.head match {
                            // ListenableFuture[T] -> CompletableFuture[Optional[T]]
                            case _: ClassName =>
                                (
                                    Some(types.optionalEntityFuture),
                                    s"return singleToCompletableFuture($sourceMethodCall)"
                                )
                            case fxa: ParameterizedTypeName =>
                                fxa.rawType match {
                                    // ListenableFuture[Result[T]] -> CompletableFuture[Iterable[T]]
                                    case frt if isGenericResult(frt) =>
                                        if (isDeleteMethod)
                                            (
                                                Some(types.voidFuture),
                                                s"return toVoidFuture($sourceMethodCall)"
                                            )
                                        else
                                            (
                                                Some(types.entityIterableFuture),
                                                s"return listToCompletableFuture($sourceMethodCall)"
                                            )
                                    case _ => defaultReturnAndStatement(t)
                                }
                            case _ => defaultReturnAndStatement(t)
                        }
                    case _ => defaultReturnAndStatement(t)
                }
            case t => defaultReturnAndStatement(t)
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
