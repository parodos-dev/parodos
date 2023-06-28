# JDBC WorkFlow task

## Motivation
Provide a task that can be used for CRUD operations via JDBC

## Input
- URL - a JDBC URL that has all required connection properties. E.g. jdbc:postgresql://localhost:5432/service?user=service&password=service123&ssl=true&sslmode=verify-full.
- Statement - the textual message/request/query that is passed to the JDBC driver. E.g. "select * from items".
- Operation type - operations are divided into 3 types. Execute is for statements such as 'drop table', 'create user'. Update is for statements such as insert, update, delete records (e.g. rows in table). Query is for retrieving records/items/info. E.g. SQL select.
- Context key for result - the context key where result of query operation is stored.

## Result
A failure to perform the operation will result in failed task execution. Only operation of type query returns a result. The result is stored with the context key that is passed as input. The result is a JSON representation of the result set.

## Configuration
The task uses the JDBC driver specified in the URL. E.g. mysql. In order to load the driver you need to add it as a maven dependency. E.g.
```
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>