package com.github.sedovalx.cassandra.service.generation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author alsedov on 11.01.2016
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CassandraService {
    /**
     * Exclude some keys from accessor/mapper generating process
     */
    String[] excludeKeys() default {};

    /**
     * Should generate mappers?
     */
    boolean generateMapper() default true;

    /**
     * Should generate accessors?
     */
    boolean generateAccessor() default true;

    /**
     * Suffix for generated mappers to use. Default: "Mapper", i.e. DomainType -> DomainTypeMapper
     */
    String mapperSuffix() default "Mapper";

    /**
     * Suffix for generated accessors to use. Default: "Accessor", i.e. DomainType -> DomainTypeAccessor
     */
    String accessorSuffix() default "Accessor";

    /**
     * Suffix for generated services to use. Default: "Service", i.e. DomainType -> DomainTypeService
     */
    String serviceSuffix() default "Service";

    Class<?> customAccessor() default void.class;
}
