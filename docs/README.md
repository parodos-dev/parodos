# Parodos Architecture And Requirements

## Technical Requirements

The following are the requirements to run the Parodos services.

| Service      | Description | Requirement | Notes |
| ------------ | ----------- | ----------- | ----- |
| Backstage.io | A popular framework for developing and operating Developer Portals (https://backstage.io/). Parodos workflows can be ran as Backstage plugins | 1.3 | This is an old version of Backstage. There are plans to update this as time permits |
| Node.js | For running Backstage locally | Please refer to the Backstage documentation  | https://backstage.io/docs/getting-started/ |
| nvm | For installing different tools | Please refer to the implemenation-examples/backstage folder | https://backstage.io/docs/getting-started/configuration |
| yarn | For starting Backstage |  Please refer to the implemenation-examples/backstage folder | |
| Keycloak | All Parodos services integrate with it, it in turn can be integrated with different OAuth providers | 17.0+ |
| JDK | Required running the Parodos API | JDK/JVM 11 | At present JVM 11 seems to be the highest version of Java the clients running Parodos are approved to use. This can be updated on demand |
| Spring Boot | All of Parodos serivces (API, Standalone) are built with Spring Boot | 2.5.5 | This is only relevant for those looking change the Parodos code |
| React | All of the Parodos services are written in React | 17.02 | The applications makes use of the Context for State and is styled with Material UI 4.12.4  |
| Database (API layer)     | Persistence API layer | Postgres 11.18+ | APIs use JPA and Liquid base to generate schemas. In theory they should work with most popular Database by changing the driver and recompiling the code. When starting any Parodos API in the 'local' profile, an H2 database is configured for evaluations |
| Database (Backstage) | When running Parodos as a set of Backstage plugins the requirements for Backstage must be met | PostgreSQL 15.1+ | Backstage.io can also run SQLite for evaluations |

## Technical Notes

### Database Notes

- Database size for Parodos APIs will vary based on the amount of records persisted. The notification API and project-history API can generate a very large amount of records depending on how the Parodos APIs are configured. In the case of notifications, it might make sense to prune records older than a certain data from the system as these messages are often only relevant at the time they were sent. These schemas can be seen in the src/main/resources/db/changelog folder of each API. If this folder does not exist, it can be assumed the application does not use a DB

- To change the Database drivers, the pom.xml will need to be updated and affected application rebuilt. Please note the existing configurations have only been tested with Postgres

- Database size for Backstage.io is unknown at this time. Please consult the Backstage.io documentation for more details: https://backstage.io/docs/overview/architecture-overview

- Following microservices practices its reccomended each service have its own Database. The following services persist to a DB: infrastructure-service, project-history, notification

### Ports and IPs

All the components of Parodos can be configured to use whatever port is desired. By default Backstage runs on Port 9000 of the host. Each of the API, within the application.yml file the server port is defined. This can be defined in the 'dev' and 'local' profiles allows for different ports for differecting test/development environments or set as an Environment Variable.

```yaml

server:
  port: ${SERVER_PORT:8088}

```

### Security Notes

All API are secured with Spring Security with Keycloak as the configured OAuth2 resource server. If Keycloak is not available in an environment, any OAuth2 provider can be put in its place.

```yaml

 security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${keycloak_url:http://localhost:3434/realms/Parodos/protocol/openid-connect/certs}

```

For the Backstage.io configuration, please review the implementations/backstage folder on how Keycloak was integrated with Backstage.

## Run Time Specifics For Services

The follow is for more a 'production' footprint. For evaluation purposes a lesser footprint can be used for each of these services.

| Service      | CPU | Memory |
| infrastructure-service | 2+ | 2GB+ |
| notification-service | 2+ | 2GB+ |
| project-history-service | 2+ | 2 GB+ |
| backstage.io | 1 | 1 GB+ |