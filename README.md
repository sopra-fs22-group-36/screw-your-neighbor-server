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
only one card. The game is build for two up to five players. 

## Technologies
### Gradle

![gradle](./doc/img/gradle.png)

For building and deploying the software Gradle was in use with the following plugins:
- Spring Boot (Spring Boot support in Gradle)
- Spring Dependency Management (project's dependencies version control)
- Spotless for code validation (clean code, adhering to coding standard)
- IDEA (Intellij IDEA customized import)
- JaCoCo (Code coverage)
- SonarQube (static code analysis and technical code quality evaluation)

### Spring Framework
![spring](./doc/img/spring.png) 

#### Spring Boot
Spring Boot helps with the creation of stand-alone Spring based applications.

#### Spring Data
The rapid api development architecture provided by spring-data-rest was used to develop the api.
It provides an easy way to build REST web services on top of the data repositories. The
PagingAndSortingRepositories provide request handling, deserialization, crud on the database,
serialization and rendering of errors. Validation was implemented with Bean validations and if
necessary with Spring event handlers. Side effects were also done with Spring event handlers.

### Hibernate
![hibernate](./doc/img/hibernate.png)

The data was stored in a hibernate database which offers an object relational mapping of the data.
This means object oriented entities can be stored in a relational database.

### JPA
Jakarta Persistence API is the API for storing, retrieving, updating and deleting relational database
entries in an object oriented context.

### Rapid API
![rapid api](./doc/img/rapid_api.png)

??? --> Frontend?

### PlantUML
![plant](./doc/img/plant_uml.png)

UML diagrams were created with PlantUML plain text language.

### Git, Github
The versioning of the source code and other files was maintained by Git ont the GitHub platform.
The GitHubs Actions feature was used for automated build, test and deployment of the software.

## High-level components
### Database
All entities used for the game are persisted during a running game. The main reason is that Spring Data REST
offers various possibilities to implement validations and side effects in an easy way.
- Bean validation (uniqueness, not null, format etc.)
- Ensure referential integrity
- Locking data for updates (ensure consistency)
- Side effects: triggering some additional (game) logic before or after creating, updating or deleting an entity

### Game Logic (side effects)
The game logic was mainly part of the EventHandlers. They implemented so-called side effects which were triggered
by certain database interactions. Spring ApplicationListener listens to database creating, saving and deleting
events and executes then the logic with the corresponding annotation (e.g. a method with the `@HandleAfterSave`
annotation is executed after the update of an entity).

### Validations
Validations are implemented in several places. Format validations are implemented on entities level
by bean validations. Some Validator classes dedicated to specific entities do further game validations.
The CardValidator class for example ensures that a player can not play two cards in the same round.

Validation errors as well as technical exceptions from Java or Hibernate are catched and transformed 
in appropriate HTTP status codes with informal exception messages to prevent technical errors in the
frontend.

### Security
#### Authentication
For entering the game lobby, creating and/or participating in a game an authenticated instance of a player
is required. On the starting page a visitor can register as a player. At the moment of storing a player, 
a security context with a dedicated authentication token is established with spring security libraries. 
This transferred between frontend and backend by cookies within the http requests.

All interactions on entities which are exposed for http requests are protected the WebSecurity configuration
which only allows to call the /players endpoint without any valid authentication.

| File                                                                                                                                                                                                  | Responsability                                                                                                    |
|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| [PlayerEventHandler](https://github.com/sopra-fs22-group-36/screw-your-neighbor-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs22/screwyourneighborserver/sideeffects/PlayerEventHandler.java) | Creates security context with authentication token when a new player is created.                                  |
| [WebSecurityConfig](https://github.com/sopra-fs22-group-36/screw-your-neighbor-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs22/screwyourneighborserver/security/WebSecurityConfig.java)      | Controls requests access. Declares which endpoints can be called non-authenticated and which need authentication. |


#### Authorization
An authenticated instance does not automatically allow interaction with the backend. Data is protected by
expression-based access control. Save or delete acces is only granted to objects with the role 'PLAYER'.
Methods are protected with the `@PreAuthorize("hasRole('PLAYER')")` annotation.

There is more game specific authorization logic implemented.

| File                                                                                                                                                                                                                                               | Authorization                                          | Responsibility                                                                                           | Usage                            |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|----------------------------------------------------------------------------------------------------------|----------------------------------|
| [CustomMethodSecurity ExpressionHandler](https://github.com/sopra-fs22-group-36/screw-your-neighbor-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs22/screwyourneighborserver/security/expressions/CustomMethodSecurityExpressionRoot.java) | playsIn: Participates in a specific a game.            | Interaction on any object of a game is only allowed for players being part of this game themselves. | GameRepository                   |
| [CustomMethodSecurity ExpressionHandler](https://github.com/sopra-fs22-group-36/screw-your-neighbor-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs22/screwyourneighborserver/security/expressions/CustomMethodSecurityExpressionRoot.java) | isOwnCard: Card entity belongs to the hand of a player. | Whether a card can be read or updated depends on the ownership of the card.                              | CardValidator<br/>CardSerializer |

### Serialization/Deserialization
Data transfer is done with Jackson JSON serializer and deserializer. Only for transfer of card objects
the serializer is overwritten by a special serializer ([CardSerializer](https://github.com/sopra-fs22-group-36/screw-your-neighbor-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs22/screwyourneighborserver/serialization/CardSerializer.java)). The reason for that is that displaying
cards to a user depends on the round. Normally players can see their own cards and cards of the other players
are hidden. In round 5 where only one card is distributed per player, the rule is reversed. As a consequence no default
rule of displaying cards can be applied. Therefore the serialize method had to be overwritten for round 5 such that
players can't see their own cards but all the other player's cards instead.

## Launch & Deployment
For developers using Intellij, the backend project can be set up by simply importing it. Gradle ensures the dependencies
setup and the IDEA plugin guarantees a proper import with Intellij.

To **build the project**, run the [build.gradle file](https://github.com/sopra-fs22-group-36/screw-your-neighbor-server/blob/main/build.gradle).
It does not only build the source code, it also runs tests and checks adherance to code quality standards.

To **run the tests** in isolation, one can run the test folder or one specific test file.

To **run the project**, locally all that needs to be done is to execute the command `./gradlew bootrun`. The already mentioned Spring
Boot Gradle Plugin manages the projects dependencies, packages and runs the application. New dependencies have to be added to the 
build.gardle file.

To **enhance the documentation**, adding an .md file to the doc folder or any subfolder or adding contents to an existing .md file is the
way to go. UML diagrams can be pushed as .puml files. The CI pipeline in Github will convert them to .svg files and include them into the
.md files where they are referenced.

To **deploy and release source code to production** the code has to be merged from a feature branch to the main branch. Pushing directly
to the main branch is not possible because any code being published on the main branch is automatically deployed by a CD pipeline to Heroku.
Standard process is to create a pull request which can be merged when at least one other developer gave their approval. Merging is not blocked
without, but individual deployment without code review and approval is not appreciated by the team.

## Roadmap
### Top features to contribute
- Sound effects when playing card, winning / loosing trick, winning the game etc.
- Implement different manners of score point calculation
- User profiles to store players performance

## Contributors
Screw your neighbor application was developed in context of the SoPra (Software Praktikum / Software Engineerin Lab)
module at the _Institut für Informatik_ at the University of Zurich by Lucius Bachmann, Beat Furrer, Carmen Kirchdorfer, Moris Camporesi
and Salome Wildermuth.

Any new contributor hearts, welcome to our Screw-your-neighbor-family. Please don't hesitate to open a PR or an issue if there's
something that needs improvement or if you want to help implementing one of our roadmap features!

## License

This project is licensed under the **MIT license**.

See [LICENSE](LICENSE.txt) for more information.
