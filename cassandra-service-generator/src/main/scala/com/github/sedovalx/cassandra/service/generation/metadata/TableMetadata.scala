package com.github.sedovalx.cassandra.service.generation.metadata

import javax.lang.model.element.TypeElement

import com.github.sedovalx.cassandra.service.generation.annotations.QueryParams

/**
  * Created by Alexander
  * on 27.12.2015.
  */
case class TableMetadata(
    element: TypeElement,
    name: String,
    entityName: String,
    entityPackage: String,
    keys: List[KeyMetadata],
    queryParams: Option[QueryParams] = None,

    mapperSuffix: String,
    accessorSuffix: String,
    serviceSuffix: String,

    customAccessor: Option[TypeElement] = None
)
