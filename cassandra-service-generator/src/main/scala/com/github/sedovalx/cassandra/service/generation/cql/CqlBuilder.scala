package com.github.sedovalx.cassandra.service.generation.cql

/**
  * Created by Alexander 
  * on 31.12.2015.
  */
protected[generation] object CqlBuilder {
    def select(tableName: String, whereItems: Seq[String] = Nil, limit: Option[Int] = None, fields: String = "*"): String = {
        val and = " and"
        var cql = s"select $fields from $tableName"
        if (whereItems.nonEmpty) {
            val cqlWithWhere = whereItems.fold(cql + " where") { (cql, item) => s"$cql $item = ?$and" }
            cql = cqlWithWhere.dropRight(and.length)
        }

        limit match {
            case None => cql
            case Some(x) => cql + " limit 1"
        }
    }
}
