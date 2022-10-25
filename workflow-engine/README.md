# Parodos Workflow Engine

Parodos sits on top of existing tools and process in an enterprise environment. As a result, its assumed there will be existing workflow tool and business rules engine. However, within Parodos itself there is a need to execute tasks asynchronously while collecting their results.

There are existing Workflow engines in Java, but many to be very complex (BPMN implementations), and as previously its assumed where Parodos is running there will will most likely be such engines/frameworks. As a result Parodos's current implementation for internally executing Workflows is https://github.com/j-easy/easy-flows.

## Comments On Easy Flow Usage

Easy-Flows is under an MIT license, so we can use it in Parodos but its also in Read Only mode with no further development planned. As result, Parodos will use this for it's initial launch. Based on the feedback of this launch, a choice will need to be made for this project. Do we build upon what Easy-Flow started,create a new Workflow Management tool or integrate a different library? Care must be taken in this decision as such libraries are a slippery slope to implement and the requirements for Parodos are very simple.

In the meantime, we have only brought in the following concepts:

- Work: An executable task. Used in Infrastructure Assessment, Events related to requesting infrastructure and the abstraction that sits above Pipelines
- Workflow: A collection of Work that is executed in specific fashion. There are multiple implementations of Workflow included in the project
- WorkContext: Resource passed into Work (can contain arguments for execution or the results of the work)
- WorkReport: Returned from Work after its execution, useful in determining if the Work execution was successful

Should Parodos decide to replace Easy Flow, these contracts provide a good measure of the needs of Parodos.

To learn more about how to use these Workflows, please refer to the Wiki:
https://github.com/j-easy/easy-flows/wiki

Big thank you to Mahmoud Ben Hassine (https://github.com/fmbenhassine) for creating this project.

# Author

Luke Shannon (Github: lshannon)
