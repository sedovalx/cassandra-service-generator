![logo image](http://www.codeguru.com.ua/up/news/img/001/n-166.gif)

[![Build Status](https://travis-ci.org/sedovalx/cassandra-service-generator.svg?branch=master)](https://travis-ci.org/sedovalx/cassandra-service-generator)

# cassandra-service-generator
This library is targeted to provide a typesafe way of passing keys to query methods of the [DataStax Driver](https://github.com/datastax/java-driver). For this purpose the library includes an annotation processor that generates a thin service class layer over the Mapper<T> instances for each annotated domain entity class. This service class contains read, write and delete methods which have input parameters that strictly depends on the key set of the source domain entity class. 
Also the generator generates a variety of an entity accessor's query methods to cover all (?) possible combinations of the WHERE clause for the table. I believe it reduce the number of typo errors in the accessor's CQL queries and its parameters.

## Good to know

* https://docs.datastax.com/en/developer/java-driver/2.1/java-driver/reference/objectMappingApi.html

## Installation

No release yet, possible bugs. You can find it in the https://oss.sonatype.org/content/repositories/snapshots repository.

``` xml
<dependency>
    <groupId>com.github.sedovalx.cassandra.services</groupId>
    <artifactId>cassandra-service-base</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.github.sedovalx.cassandra.services</groupId>
    <artifactId>cassandra-service-generator</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

## CassandraService annotation processing

First of all you need to annotate your table class with the CassandraService annotation  

``` java
@CassandraService
@Table(keyspace = "sample", name = "client_report")
public class ClientReport {
    @PartitionKey(0)
    @Column(name = "region")
    private Long region;

    @ClusteringColumn(0)
    @Column(name = "tpl_code")
    private String templateCode;

    @ClusteringColumn(1)
    @Column(name = "p_year")
    private Integer periodYear;

    @ClusteringColumn(2)
    @Column(name = "p_code")
    private Integer periodCode;

    @ClusteringColumn(3)
    @Column(name = "client_id")
    private Long clientId;

    @ClusteringColumn(4)
    @Column(name = "data_id")
    private UUID dataId;

    @Column(name = "data")
    private String data;

    @Column(name = "ver")
    private Date ver;

    @Column(name = "is_deleted")
    private boolean isDeleted;
    
    // setters and getters
}
```

After successful compilation there are four generated services in the target/generated-sources/annotation/your_entity_package/ 
folder. By default it are an accessor, a mapper, a service and the accessor's java8 adapter. Don't forget to annotate
your table class with @Table, the generator won't work otherwise. 
 
### Accessor
 
There are very limited amount of WHERE clauses for the SELECT statement available for any Cassandra-table. This amount equals 
to the clustering keys count plus one. The generated accessor contains all of them as overloadings of a methods with "get"/"getAsync" names.
The overloads have different number of parameters, each parameter corresponds to one of the **primary** key parts as they 
are specified in the table class. Both sync and async versions of methods are generated, async version has name "getAsync".
Methods "getAll" and "deleteAll" are generated too (sync/async).

 
For the example above next accessor will be generated:

``` java
@Accessor
public interface ClientReportAccessor {
  @Query("select * from client_report")
  Result<ClientReport> getAll();

  @Query("select * from client_report")
  ListenableFuture<Result<ClientReport>> getAllAsync();

  @Query("truncate client_report")
  Result<ClientReport> deleteAll();

  @Query("truncate client_report")
  ListenableFuture<Result<ClientReport>> deleteAllAsync();

  @Query("select * from client_report where region = ?")
  Result<ClientReport> get(Integer region);

  @Query("select * from client_report where region = ?")
  ListenableFuture<Result<ClientReport>> getAsync(Integer region);

  @Query("select * from client_report where region = ? and tpl_code = ?")
  Result<ClientReport> get(Integer region, String templateCode);

  @Query("select * from client_report where region = ? and tpl_code = ?")
  ListenableFuture<Result<ClientReport>> getAsync(Integer region, String templateCode);

  @Query("select * from client_report where region = ? and tpl_code = ? and p_year = ?")
  Result<ClientReport> get(Integer region, String templateCode, Integer periodYear);

  @Query("select * from client_report where region = ? and tpl_code = ? and p_year = ?")
  ListenableFuture<Result<ClientReport>> getAsync(Integer region, String templateCode, Integer periodYear);

  @Query("select * from client_report where region = ? and tpl_code = ? and p_year = ? and p_code = ?")
  Result<ClientReport> get(Integer region, String templateCode, Integer periodYear, Integer periodCode);

  @Query("select * from client_report where region = ? and tpl_code = ? and p_year = ? and p_code = ?")
  ListenableFuture<Result<ClientReport>> getAsync(Integer region, String templateCode, Integer periodYear, Integer periodCode);

  @Query("select * from client_report where region = ? and tpl_code = ? and p_year = ? and p_code = ? and client_id = ?")
  Result<ClientReport> get(Integer region, String templateCode, Integer periodYear, Integer periodCode, Long clientId);

  @Query("select * from client_report where region = ? and tpl_code = ? and p_year = ? and p_code = ? and client_id = ?")
  ListenableFuture<Result<ClientReport>> getAsync(Integer region, String templateCode, Integer periodYear, Integer periodCode, Long clientId);

  @Query("select * from client_report where region = ? and tpl_code = ? and p_year = ? and p_code = ? and client_id = ? and data_id = ? limit 1")
  ClientReport get(Integer region, String templateCode, Integer periodYear, Integer periodCode, Long clientId, UUID dataId);

  @Query("select * from client_report where region = ? and tpl_code = ? and p_year = ? and p_code = ? and client_id = ? and data_id = ? limit 1")
  ListenableFuture<ClientReport> getAsync(Integer region, String templateCode, Integer periodYear, Integer periodCode, Long clientId, UUID dataId);
}
```

Check that "full primary key" methods return a single object instead of an Iterable. 

If you for any reason want to exclude some of the primary key parts from generation process specify `excludeKeys` 
property of the annotation:

``` java
@CassandraService(excludeKeys = { "dataId" })
```

The `QueryParams` annotation can be used to control consistency level and etc for the generated "get" methods. Just 
annotate the whole table class or any of the primary key's fields with it:

``` java
@QueryParams(consistency = "ALL", tracing = true)
@ClusteringColumn(0)
@Column(name = "tpl_code")
private String templateCode;
```

### Java8 adapter

Java8 is everywhere so the adapter for the accessor is generated. This adapter is nothing more then slim shell over the 
accessor. It change return types of the accessor's methods by the following rules:
 
+ If method name starts with "delete" then void or CompletableFuture<Void> returns
+ T translates to Optional<T>
+ ListenableFuture<T> translates to CompletableFuture<T>
+ ResultSetFuture translates to CompletableFuture<ResultSet> 
+ Others don't change

For complete list of possible return types for an accessor see [DataStax documentation](https://docs.datastax.com/en/developer/java-driver/2.1/common/drivers/reference/accessorAnnotatedInterfaces.html)
 
Example for the table above:
  
``` java
public class ClientReportAccessorAdapter extends AbstractAccessorJava8Adapter<ClientReport> {
  private ClientReportAccessor accessor;

  public ClientReportAccessorAdapter(MappingManager mappingManager) {
    this.accessor = mappingManager.createAccessor(ClientReportAccessor.class);
  }

  public Result<ClientReport> getAll() {
    return this.accessor.getAll();
  }

  public CompletableFuture<Result<ClientReport>> getAllAsync() {
    return toCompletableFutureResult(this.accessor.getAllAsync());
  }

  public void deleteAll() {
    this.accessor.deleteAll();
  }

  public CompletableFuture<Void> deleteAllAsync() {
    return toVoidFuture(this.accessor.deleteAllAsync());
  }

  public Result<ClientReport> get(Integer region) {
    return this.accessor.get(region);
  }

  public CompletableFuture<Result<ClientReport>> getAsync(Integer region) {
    return toCompletableFutureResult(this.accessor.getAsync(region));
  }

  public Result<ClientReport> get(Integer region, String templateCode) {
    return this.accessor.get(region, templateCode);
  }

  public CompletableFuture<Result<ClientReport>> getAsync(Integer region, String templateCode) {
    return toCompletableFutureResult(this.accessor.getAsync(region, templateCode));
  }

  public Result<ClientReport> get(Integer region, String templateCode, Integer periodYear) {
    return this.accessor.get(region, templateCode, periodYear);
  }

  public CompletableFuture<Result<ClientReport>> getAsync(Integer region, String templateCode, Integer periodYear) {
    return toCompletableFutureResult(this.accessor.getAsync(region, templateCode, periodYear));
  }

  public Result<ClientReport> get(Integer region, String templateCode, Integer periodYear, Integer periodCode) {
    return this.accessor.get(region, templateCode, periodYear, periodCode);
  }

  public CompletableFuture<Result<ClientReport>> getAsync(Integer region, String templateCode, Integer periodYear, Integer periodCode) {
    return toCompletableFutureResult(this.accessor.getAsync(region, templateCode, periodYear, periodCode));
  }

  public Optional<ClientReport> get(Integer region, String templateCode, Integer periodYear, Integer periodCode, Long clientId) {
    return toOptional(this.accessor.get(region, templateCode, periodYear, periodCode, clientId));
  }

  public CompletableFuture<Optional<ClientReport>> getAsync(Integer region, String templateCode, Integer periodYear, Integer periodCode, Long clientId) {
    return toCompletableFutureEntity(this.accessor.getAsync(region, templateCode, periodYear, periodCode, clientId));
  }

  public Result<ClientReport> markAllByRegion(int region) {
    return this.accessor.markAllByRegion(region);
  }

  public void deleteAllInRegion(int region) {
    this.accessor.deleteAllInRegion(region);
  }

  public CompletableFuture<Result<ClientReport>> markAllByClient(int region, String templateCode, int periodYear, int periodCode, long clientId) {
    return toCompletableFutureResult(this.accessor.markAllByClient(region, templateCode, periodYear, periodCode, clientId));
  }

  public Statement updateData(String newData, int region, String templateCode, int periodYear, int periodCode, long clientId) {
    return this.accessor.updateData(newData, region, templateCode, periodYear, periodCode, clientId);
  }
}
```
  
### Mapper
  
The purpose of the generated mapper is very like as for the generated adapter - to provide java8 API and safer form of "get/getAsync" 
methods. It wraps DataStax's `mapper.get(Object... objects)` call with a method with type safe signature:
   
``` java
public class ClientReportMapper extends CassandraMapperGenericImpl<ClientReport> {
  ClientReportMapper(MappingManager mappingManager) {
    super(mappingManager);
  }

  public Statement getQuery(Integer region, String templateCode, Integer periodYear, Integer periodCode, Long clientId) {
    return this.mapper().getQuery(region, templateCode, periodYear, periodCode, clientId);
  }

  public Optional<ClientReport> get(Integer region, String templateCode, Integer periodYear, Integer periodCode, Long clientId, Mapper.Option... options) {
    Object[] args = combineArgumentsToVarargs(options, region, templateCode, periodYear, periodCode, clientId);
    return getOptionalEntity(args);
  }

  public CompletableFuture<Optional<ClientReport>> getAsync(Integer region, String templateCode, Integer periodYear, Integer periodCode, Long clientId, Mapper.Option... options) {
    Object[] args = combineArgumentsToVarargs(options, region, templateCode, periodYear, periodCode, clientId);
    return getOptionalFuture(args);
  }

  public Statement deleteQuery(Integer region, String templateCode, Integer periodYear, Integer periodCode, Long clientId) {
    return this.mapper().deleteQuery(region, templateCode, periodYear, periodCode, clientId);
  }

  public void delete(Integer region, String templateCode, Integer periodYear, Integer periodCode, Long clientId, Mapper.Option... options) {
    Object[] args = combineArgumentsToVarargs(options, region, templateCode, periodYear, periodCode, clientId);
    this.mapper().delete(args);
  }

  public CompletableFuture<Void> deleteAsync(Integer region, String templateCode, Integer periodYear, Integer periodCode, Long clientId, Mapper.Option... options) {
    Object[] args = combineArgumentsToVarargs(options, region, templateCode, periodYear, periodCode, clientId);
    return deleteAsyncWithMapper(args);
  }
}
```

### Service

For the sake of convenience the service is generated also. It just aggregate the adapter and the mapper into itself. 

### Other parameters

Except `excludeKeys` parameter the CassandraService annotation has a bunch of settings that can help slightly change the generation 
process:
+ excludeKeys - Exclude some keys from accessor/mapper generating process
+ generateMapper - Should generate mappers?
+ generateAccessor - Should generate accessors?
+ mapperSuffix - Suffix for generated mappers to use. Default: "Mapper"
+ accessorSuffix - Suffix for generated accessors to use. Default: "Accessor"
+ serviceSuffix - Suffix for generated services to use. Default: "Service"
+ customAccessor - Custom accessor interface which Query-methods will be merged into the generated accessor (see below)

### Custom accessor

It's easy to see that SELECT only queries in the accessor are not enough. But I don't see the possibility to generate 
UPDATE-queries for every case. What you can do is to create another ordinary accessor for the table by hands and include  
any queries you want into it. Then use `customAccessor` parameter of the CassandraService annotation to merge that queries into 
the generated accessor. Additional java8-adapter methods you'll get for free. Btw it's possible to limit the visibility of the custom 
accessor to the package level if you want.

``` java
@CassandraService(customAccessor = ClientReportUpdateAccessor.class)
```

## Usage

The typical scenario is to define a Spring (or other IoC) config for generated classes. Be aware of the MappingManager bean that should be placed into the IoC-container:

``` java
@Configuration
public class CassandraConfig {
    @Bean
    public MappingManager mappingManager() {
        Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").withPort(9142).build()
        Session session = cluster.connect("sample");
        return new MappingManager(session);
    }
}

@Configuration
public class ServicesConfig {
    @Autowired
    private MappingManager mappingManager;
    
    @Bean
    public ClientReportService clientReportService(){
        return new ClientReportService(mappingManager);
    }
}
```

For cassandra-unit tests mentioned beans should be lazy to give the cassandra service time to start. See the example in the cassandra-service-samples module.
