package com.github.sedovalx.cassandra.service.generation.metadata

import javax.lang.model.element.TypeElement

import com.github.sedovalx.cassandra.service.generation.annotations.QueryParams

case class KeyMetadata(
    name: String,
    propertyName: String,
    tpe: TypeElement,
    isPartitionKey: Boolean,
    isClusteringKey: Boolean,
    keyIndex: Int,
    queryParams: Option[QueryParams]
)
