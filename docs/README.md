# Parodos Architecture And Requirements

## Technical Requirements

The following are the requirements to run the Parodos services.

| Service      | Description | Requirement | Notes |
| ------------ | ----------- | ----------- | ----- |
| Database (API layer)     | Persistence API layer | Postgres 11.18+ | APIs use JPA and Liquid base to generate schemas. In theory they should work with most popular Database by changing the driver and recompiling the code. When starting any Parodos API in the 'local' profile, an H2 database is configured for evaluations |
| Database (Backstage) | When running Parodos as a set of Backstage plugins the requirements for Backstage must be met | PostgreSQL 15.1+ | Backstage.io can also run SQLite for evaluations |
| JDK | Required running the Parodos API | JDK/JVM 11 | At present JVM 11 seems to be the highest version of Java the clients running Parodos are approved to use. This can be updated on demand |
| Spring Boot | All of Parodos serivces (API, Standalone) are built with Spring Boot | 2.5.5 | This is only relevant for those looking change the Parodos code |
| 


## Technical Notes

### Database Notes

- Database size for Parodos APIs will vary based on the amount of records persisted. The notification API and project-history API can generate a very large amount of records depending on how the Parodos APIs are configured. In the case of notifications, it might make sense to prune records older than a certain data from the system as these messages are often only relevant at the time they were sent

- Database size for Backstage.io is unknown at this time. Please consult the Backstage.io documentation for more details: https://backstage.io/docs/overview/architecture-overview