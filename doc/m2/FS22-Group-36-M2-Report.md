## Diagrams

### Component Diagram

![Component diagram](uml/component_diagram.svg)

### Class Diagram

The Rapid Api Development Architecture provided by spring-data-rest will be used to develop the api.
The PagingAndSortingRepositories provide request handling, deserialization, crud on the database, serialization
and rendering of errors. Validation will be implemented with Bean validations and if necessary with Spring event
handlers. Side effects will also be done with Spring event handlers. We decided to only include the Domain classes
in the diagram, because that tells the relations between the main concepts in the application.
Validation, the spring data rest resources (implemented with PagingAndSortingRepositories) and the event handlers
are depicted as package.

![Class diagram](uml/class_diagram.svg)

### Activity Diagram

## UI Mockups
