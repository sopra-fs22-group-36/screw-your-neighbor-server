<h1 align="center">
   <img src="icon.png" width="auto" height="500">
</h1>

# Screw your neighbor: backend solution [^1]

[^1]: By group 36 of the course "Software Praktikum"@UZH in FS 22.

## Github Badges:

[![Checks](https://github.com/sopra-fs22-group-36/screw-your-neighbor-server/actions/workflows/checks.yml/badge.svg)](https://github.com/sopra-fs22-group-36/screw-your-neighbor-server/actions/workflows/checks.yml)
[![Deploy](https://github.com/sopra-fs22-group-36/screw-your-neighbor-server/actions/workflows/deploy-heroku.yml/badge.svg)](https://github.com/sopra-fs22-group-36/screw-your-neighbor-server/actions/workflows/deploy-heroku.yml)

## SonarCloud Badges:

[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=sopra-fs22-group-36_screw-your-neighbor-server&metric=bugs)](https://sonarcloud.io/summary/new_code?id=sopra-fs22-group-36_screw-your-neighbor-server)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=sopra-fs22-group-36_screw-your-neighbor-server&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=sopra-fs22-group-36_screw-your-neighbor-server)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=sopra-fs22-group-36_screw-your-neighbor-server&metric=coverage)](https://sonarcloud.io/summary/new_code?id=sopra-fs22-group-36_screw-your-neighbor-server)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=sopra-fs22-group-36_screw-your-neighbor-server&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=sopra-fs22-group-36_screw-your-neighbor-server)

## Api Documentation

Start the application and visit /swagger-ui.html to see a swagger ui with the api documentation.

## Introduction
We are developing an online version of the world-famous card game "Härdöpfle" also known as "Screw your neighbour" 
played with the 36 Swiss "Jasskarten". All main functions are implemented as well as the stack rule, and the special round with 
only one card. The game is build for 2 to 5 players. 

## Technologies
### Gradle

![gradle](./doc/img/gradle.png)

Dependencies and build are managed with gradle. The following gradle plugins are in use:
- Spring Boot (Spring Boot support in Gradle)
- Spring Dependency Management (asserts that the correct combination of spring dependencies are used)
- Spotless for code validation (clean code, adhering to coding standard)
- IDEA (Intellij IDEA customized import)
- JaCoCo (Code coverage)
- SonarQube (static code analysis and technical code quality evaluation)

### Spring Framework
![spring](./doc/img/spring.png) 

#### Spring Boot
Spring boot allows for automatic dependency injection implemented with classpath scanning instead of
using xml configuration for the dependency injection.

#### Spring Data Rest
The rapid api development architecture provided by spring-data-rest was used to develop the api.
It provides an easy way to build REST web services on top of the data repositories. The
PagingAndSortingRepositories and the corresponding entities provide everything for CRUD operations on the database
(Deserialization, Database Operation, Serialization, Basic Error Handling).
Validation was implemented with Bean validations and if necessary with Spring event handlers.
Side effects were also done with Spring event handlers.

### Hibernate & H2
![hibernate](./doc/img/hibernate.png)
![h2](./doc/img/h2.png)

The data is stored in a H2 database. The data is accessed through the Object Relational Mapper of the Hibernate framework.

### PlantUML
![plant](./doc/img/plant_uml.png)

UML diagrams were created with PlantUML.

## High-level components
### Database
We use an in memory H2 database for persistence. The schema is defined with the 
[entities](src/main/java/ch/uzh/ifi/hase/soprafs22/screwyourneighborserver/entity). The data can be accessed with
the [repositories](src/main/java/ch/uzh/ifi/hase/soprafs22/screwyourneighborserver/repository).

### Game Logic (side effects)
The game logic is done with RepositoryEventHandlers in [sideeffects](src/main/java/ch/uzh/ifi/hase/soprafs22/screwyourneighborserver/sideeffects).
They implement side effects which are triggered by certain database interactions.
Spring ApplicationListener listens to database creating, saving and deleting events and executes then the logic with
the corresponding annotation (e.g. a method with the `@HandleAfterSave` annotation is executed after the update of an entity).

### Validations
Validations are implemented in several places. Format validations are implemented on entities level
by bean validations. Some Validator classes dedicated to specific entities do further game validations.
They can be found in the [validation package](src/main/java/ch/uzh/ifi/hase/soprafs22/screwyourneighborserver/validation).
The CardValidator class for example ensures (amongst other things) that a player can not play two cards in the same round.

Validation errors as well as technical exceptions from Java or Hibernate are catched and transformed 
in appropriate HTTP status codes with informal exception messages to prevent technical errors in the
frontend.

### Security
#### Authentication
For entering the game lobby, creating and/or participating in a game an authenticated instance of a player
is required. On the starting page a visitor can register as a player. At the moment of storing a player, 
a security context with a dedicated authentication token is established with spring security libraries. 
This token is transferred between frontend and backend by cookies within the http requests.

All interactions on entities which are exposed for http requests are protected according to the WebSecurityConfiguration
file which only allows to call the /players endpoint without any valid authentication.

| File                                                                                                                      | Responsability                                                                                                    |
|---------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| [PlayerEventHandler](src/main/java/ch/uzh/ifi/hase/soprafs22/screwyourneighborserver/sideeffects/PlayerEventHandler.java) | Creates security context with authentication token when a new player is created.                                  |
| [WebSecurityConfig](src/main/java/ch/uzh/ifi/hase/soprafs22/screwyourneighborserver/security/WebSecurityConfig.java)      | Controls requests access. Declares which endpoints can be called non-authenticated and which need authentication. |


#### Authorization
An authenticated instance is not automatically allowed for interaction on all resources. Data is protected by
expression-based access control. Most endpoints need an authentication, and some have stricter requirements for access
like PATCH /cards/{id}, which only allows players to update their own card.
This is done with SPeL (Spring Security Expression Language), which is added to the @HandleBefore(Save,Create) methods
as in [CardValidator](src/main/java/ch/uzh/ifi/hase/soprafs22/screwyourneighborserver/validation/CardValidator.java),
and on the corresponding methods in the repositories (e.g. [CardRepository](src/main/java/ch/uzh/ifi/hase/soprafs22/screwyourneighborserver/repository/CardRepository.java)).

Game logic steering authorizations:

| File                                                                                                                                                                   | Authorization                                           | Responsibility                                                                                      | Usage                            |
|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------|-----------------------------------------------------------------------------------------------------|----------------------------------|
| [CustomMethodSecurity ExpressionHandler](src/main/java/ch/uzh/ifi/hase/soprafs22/screwyourneighborserver/security/expressions/CustomMethodSecurityExpressionRoot.java) | playsIn: Participates in a specific a game.             | Interaction on any object of a game is only allowed for players being part of this game themselves. | GameRepository                   |
| [CustomMethodSecurity ExpressionHandler](src/main/java/ch/uzh/ifi/hase/soprafs22/screwyourneighborserver/security/expressions/CustomMethodSecurityExpressionRoot.java) | isOwnCard: Card entity belongs to the hand of a player. | Whether a card can be read or updated depends on the ownership of the card.                         | CardValidator<br/>CardSerializer |

### Serialization/Deserialization
Data transfer is done with Jackson JSON serializer and deserializer. For transfer of card objects
the serializer is overwritten by a special serializer ([CardSerializer](src/main/java/ch/uzh/ifi/hase/soprafs22/screwyourneighborserver/serialization/CardSerializer.java)).
The reason for that is that displaying
cards to a user depends on the round. Normally players can see their own cards and cards of the other players
are hidden. In round 5 where only one card is distributed per player, the rule is reversed. As a consequence no default
rule of displaying cards can be applied. Therefore the serialize method had to be overwritten for round 5 such that
players can't see their own cards but all the other player's cards instead.

## Launch & Deployment
The project is set up with gradle and the gradle wrapper.

For developers using Intellij, the backend project can be set up by simply importing it.

```
# Windows users must use gradlew.bat

# build and test
./gradlew build

# format the code
./gradlew spotlessApply

# run the application
./gradlew bootrun

# run tests
./gradlew test
```

To **enhance the documentation**, adding an .md file to the doc folder or any subfolder or adding contents to an existing .md file is the
way to go. UML diagrams can be pushed as .puml files. The CI pipeline in Github will convert them to .svg files and include them into the
.md files where they are referenced.

To **deploy and release source code to production** the code has to be merged from a feature branch to the main branch. Pushing directly
to the main branch is disabled by the repository configuration.
Standard process is to create a pull request which can be merged when at least one other developer gave their approval. Merging is not blocked
without, but individual deployment without code review and approval is not appreciated by the team.

## Roadmap
### Top features to contribute
- Implement different manners of score point calculation
- User profiles to store players performance
- Validations for announcing the score

## Contributors
Screw your neighbor application was developed in context of the SoPra (Software Praktikum / Software Engineerin Lab)
module at the _Institut für Informatik_ at the University of Zurich by Lucius Bachmann, Beat Furrer, Carmen Kirchdorfer, Moris Camporesi
and Salome Wildermuth.

Any new contributor hearts, welcome to our Screw-your-neighbor-family. Please don't hesitate to open a PR or an issue if there's
something that needs improvement or if you want to help implementing one of our roadmap features!

## License

This project is licensed under the **MIT license**.

See [LICENSE](LICENSE.txt) for more information.
