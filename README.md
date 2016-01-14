![logo](http://photos2.meetupstatic.com/photos/event/a/5/c/a/600_388362442.jpeg)
[![Build Status](https://travis-ci.org/sedovalx/cassandra-service-generator.svg?branch=master)](https://travis-ci.org/sedovalx/cassandra-service-generator)
# cassandra-service-generator
This processor generates for an annotated class:
* mapper adapter with typesafe get method instead of Object... varargs
* accessor for select methods with every possible WHERE conditions + deleteAll method (sync/async)
* java8 adapter for the generated accessor

More to come...
