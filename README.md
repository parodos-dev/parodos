# What Is Parodos?

Parodos is a Java toolkit to help enterprise's with a legacy footprint build internal developer platforms (IDP) that
enable their developers to get access to the tools and environments required to start coding with less time, friction
and frustration.

This repository contains the backing services for Parodos. The user interface for Parodos are
Backstage (https://backstage.io/) plugins that will be contributed to Red Hat's Janus-IDP initiative (this work is in
progress).
https://github.com/janus-idp

The focus of Parodos is around Workflows (composed of WorkflowTasks) that bring together the existing tools and
processes of the enterpise in an end-to-end experience that developers, quality assurance, production support and other
enterprise software development/delivery team members can consume (through our Backstage plugins or by your own
interfaces calling these backing services) to get the outcomes they need with fewer tickets/meetings/frustration.

# Building Developer Platforms In Enterprise Environments

Building an IDP provides a centralized place to improve the experience for developers trying to build and release code
in large environments.

For many enterprise environments, especially regulated ones, the source of some friction preventing a positive
developer experience is that they are entangled with long-standing processes and tools which are tied to audit,
compliance and regulation. These components are often:

- Unique to the enterprise
- Providing a necessary safeguard
- Difficult to change or remove

For more thoughts and opinions on the challenges in making changes to an existing software culture in an enterprise
environment, please review the following blog:

https://www.redhat.com/en/blog/modernization-why-is-it-hard

For more information about building IDPs in regulated enterprise environments, review the following:

https://www.redhat.com/en/blog/considerations-when-implementing-developer-portals-regulated-enterprise-environments

Parodos is focused on building solutions based on technical stacks that are both familiar, and have a history of success
for enterprises described in the preceding articles.

# The Focus Of Parodos

Although frameworks and ecosystems might exist to help build developer portals, getting some of these approved for
production use in certain enterprises, especially if they have a large ecosystem written by disparate developers, might
be difficult.

Some enterprises may struggle with Javascript heavy approaches. That is not to say such libraries, frameworks and
platforms are not suited for the task. On the contrary they might be just what is needed, but a chasm might exist to
fully adopt in an enterprise environment that has legacy technology and processes.

Other enterprises might have existing IDP tools that are home-grown over years and might need to be leveraged in the
initial stages of any new IDP work.

Parodos is ancient Greek and translates to 'a side-entrance to the stage'. In this theme, Parodos provides Java based
building blocks (specifically Spring beans) to bring together backend processes and components that might be considered
more 'legacy' as workflows (including existing IDP components) that can be consumed in Backstage.

The most common use case for Parodos is giving developers a place where they can provide inputs for Assessments (ie: a
link to their project code and/or an application identifying code), and based on logic determined by the enterprise a
list of Workflows are presented to them.

Examples might be:

- Upgrade to existing tool stack and environments to newer versions
- Migrate to new tooling and environments
- Onboard for the first time to tooling and environments
- Add/Remove developers to a project
- Change properties of an environment (ie: add more memory to QA)

Developers will be presented with simple Wizard based steppers that collect information as needed and update them of any
back-end approvals that are kicked off during the workflow which might result in a pause in the workflow.

In the backend Parodos will be calling the exact tooling to create enterprises approved blueprints for that specific use
case.

All the logic of Parodos can be maintained in a seperate Java project using Spring beans as a means of creating and
configuring workflows (future versions of Parodos will support other means of configuration).

# Who Is Parodos For?

The following describes who would benefit the most from Parodos:

1. Enterprises that are struggling logistically (or technically due to challenges hiring in a specific technology stack)
   Javascript heavy products in the developer portal space

2. Teams that are comfortable building and operating a custom Java based application

3. Environments where there are existing tools for building, deploying and observing application that can be integrated
   with

If all of these exist, your enterprise may benefit from using Parodos to build some workflows to help developers deliver
code faster.

## Parodos Components

Parodos is composed of the following components

**workflow-service**

Provides the APIs to run Workflows defined using the Parodos model. It also persists Workflow definitions (and tracks
changes of definitions), persists execution state and provides scheduling for long-running WorkFlowTasks.

**parodos-model-api**

This is the model used by all services. It also contains some abstract definition of WorkflowTask for specific use
cases (ie: Assessment, checking a downstream approval). At present all Workflows and WorkflowTasks are define as Spring
Framework beans. More information can be found the READ of the workflow-service, parodos-model-api and 
workflow-examples.

**workflow-examples**

A standalone project that can be added to the workflow-service's classpath to provide some samples of what WorkFlows
could look like. This is basically a 'Hello Wold' for the workflow-service

**workflow-engine**

The current library for executing WorkFlow(s) in Parodos service

**notification-service**

Simple API for posting read-only messages related to downstream processes. The Parodos Backstage plugins provide a UI to
display these messages. This service might be useful for those team members who might not have access to other commonly
used communication systems (like Slack). It also provides a means of providing the users with updates in the same
interface where the workflow is being executed

**pattern-detection-library**

A java library that can be used in AssessmentTasks to identify application/configuration patterns that can be associated
with specific workflows (ie: Batch applications might have a different pipeline and environments than a .NET based MVC
application).

For more information on each of these, please review the README location in the root folder of each component.

Stay tuned to this repository as more Parodos components will be added in the coming weeks.

## Building the Code

In the root of this folder execute:

```shell

mvn clean install

```

This will build the dependencies and install them into your local mvn directory. This last part is important as the
Parodos 'workflow-engine' is not in Maven Central. If you wish to use it in a project like the workflow-examples, you
will need to build it locally first.

## Releasing the Code

In the root of this folder we can execute:

to push module snapshots to sonatype repository:

```shell

mvn deploy

```

to release and push modules to sonatype repository which is later synchronized to maven central:

```shell

mvn deploy -P release

```

In order to push modules or snapshots to sonatype repository following changes needs to be configured:
* [Jira](https://issues.sonatype.org) user details provided in settings file with permissions to push

```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>your-jira-id</username>
      <password>your-jira-pwd</password>
    </server>
  </servers>
</settings>
```
* [GPG client](http://www.gnupg.org/) installed and on your command line path as required by the
  [Maven GPG plugin](http://maven.apache.org/plugins/maven-gpg-plugin/)

## Is Parodos an Application?

Parodos provides an API and object model that can be used as a backing service for an IDP. If Backstage is in use, the
Parodos Janus-IDP plugins can be used to provide a user experience to teams. If the enterprise has chosen to build their
own IDP, or have an existing custom IDP they wish to enhance, the Parodos backing services can be consumed directly.

## Deployment Models

### Backstage Plugin

In this model the Parodos backing APIs are deployed in the enterprise environment along with Backstage where the Parodos
plugins are deployed. Users interact with the plugins via Backstage, the plugins maintain state and call backend systems
via the Parodos backing services

### Backing Services Only

In the event that an environment is choosing to enhancing an existing IDP, or create their own, Parodos's Java APIs can
be used to provide backing Workflows. 


trigger on rebase
