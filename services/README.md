# Parados Services

This directory contains that backing Java service for the Parodos workflows.

Each service has its own folder and README explaining how the services is built, configured, started and consumed.

A quick summary of the available services are:

**infrastructure-service**

Assesses applications/code, provides InfrastructureOptions suitable for the user and code and executes a series of calls to existing services to provision and configure the Tools, components and environments that compose the InfrastructureOption

**parodos-model-api**

This is the model used by the infrastructure service. It is a seperate package allowing for projects to be created configuring Infrastructure Workflows outside of the infrastructure-service code base

**workflow-examplese**

A stand alone project that can be added to the infrastructure service classpath to provide some samples of what WorkFlows could look like. This is basically a 'Hello Wold' for the infrastructure-service

**workflow-engine**

The current library for exeucting WorkFlow(s) in Parodos service


## Building the Code

In the root of this folder execute:

```shell

mvn clean install

```

This will build the dependencies and install them into your local mvn directory. This last part is important as the Parodos 'workflow-engine' is not in Maven Central. If you wish to use it in a project like the simple-workflow-example-infrastructure-service, you will need to build it locally.
