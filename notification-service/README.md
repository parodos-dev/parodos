# Parodos Notification Service

This service is intended to provide an Endpoint to receive messages from downstream services/systems providing the users
with details/feedback related to workflow/tasks Parodos is orchestrating for them. This service works with the Parodos
Notification UI workflow (can be deployed stand alone or with a Backstage plugin).

## Domain Model

The service is based on the following Entities:

* NotificationsMessage - Contains metadata related to the Notification, target UserID(s) as well as a NotificationRecord
  reference

* NotificationRecord - Contains metadata around how the UI/User has interacted with the Notification (ie: Read it),
  contains a List of Notification Users

* NotificationUser - Contains metadata about a User as well as a List of NotificationGroup(s) the User is associated
  with

* NotificationGroup - Contains metadata about the group and list of a Users associated with it

## Services and Controllers

Each Domain entity has an associated Service and ServiceImpl for performing CRUD operations.

The **NotificationMessageController** provides a single endpoint for creating NotificationMessages. This is the endpoint
that downstream services/systems can POST to.

The **NotificationRecordController** provides both GET endpoints (for obtaining NotificationMessages) and PUT endpoints
for updating their metadata (ie: message has been read).

## Persistence

The service uses JPA to persist. Postgres is the recommended DB. For the 'dev' profile H2 is utilized.

## User Interface

The UI for this service can be found as a backstage Plugin for a stand-alone Java service (UI + API) in one Jar, check
out implementation-examples/java-services

## Starting the Service

From the root of the 'notification-service' folder, the follow command will start-up the server locally:

```shell

java -jar -Dspring.profiles.active=local -Dserver.port=8081  target/notification-service-1.0.12.jar

```

The service will run against a local h2 database in when running with way.

## FAQ

### We use Slack, is this required?

This feature is an optional component of Parodos. If there is another application (ie: Slack) you prefer to provide
updates to user on, feel free to integration Parodos's WorkFlows to that. However, keep in mind that some personas in
your environment might not have access to these tools, or experience a long delay to get access. As a result, this
notification flow might be interesting to some users
