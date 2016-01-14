# cassandra-service-generator
This processor generates for an annotated class:
* mapper adapter with typesafe get method instead of Object... varargs
* accessor for select methods with every possible WHERE conditions + deleteAll method (sync/async)
* java8 adapter for the generated accessor

More to come...
