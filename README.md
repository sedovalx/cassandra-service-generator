[![Build Status](https://travis-ci.org/sedovalx/cassandra-service-generator.svg?branch=master)](https://travis-ci.org/sedovalx/cassandra-service-generator)

# cassandra-service-generator

![logo image](http://www.codeguru.com.ua/up/news/img/001/n-166.gif)

This processor generates for an annotated class:
* mapper adapter with typesafe get method instead of Object... varargs
* accessor for select methods with every possible WHERE conditions + deleteAll method (sync/async)
* java8 adapter for the generated accessor

More to come...
