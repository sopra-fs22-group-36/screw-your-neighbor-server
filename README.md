<h1 align="center">
   <img src="icon.png" width="auto" height="500">
</h1>

# Screw your neighbor server [^1]

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
World famous card game played with 36 Swiss "Jasskarten". 

## Technologies
Briefly overview about used main technologies: 

![spring](./doc/img/spring.png)
![java](./doc/img/gradle.png)
![rapid api](./doc/img/rapis_api.png)

![mockito](./doc/img/mockito.png)
![hibarnate](./doc/img/hibernate.png)
![plant](./doc/img/plant_uml.png)


### Spring Framework
The Rapid Api Development Architecture provided by spring-data-rest was used to develop the api.
The PagingAndSortingRepositories provide request handling, deserialization, crud on the database,
serialization and rendering of errors. Validation was implemented with Bean validations and if
necessary with Spring event handlers. Side effects were also done with Spring event handlers.

### Hibernate, JPA

### Jitsi API
(move to FE README)

### Mockito

### Git


## High-level components
### UI

### Database

### Game Logic (side effects)

### Game Validations

### Security

## Launch & Deployment

## Roadmap
### Top features to contribute
- Sound effects when playing card, winning / loosing trick, winning the game etc.
- Implement different manners of score point calculation
- User profiles to store players playing statistics

## Contributors
Screw your neighbor application was developed in context of the SoPra (Software Praktikum / Software Engineerin Lab)
module at the _Institut für Informatik_ at the University of Zurich by Lucius Bachmann, Beat Furrer, Carmen Kirchdorfer, Moris Camporesi
and Salome Wildermuth.

Any new contributor hearts, welcome to our Screw-your-neighbor-family. Please don't hesitate to open a PR or an issue if there's
something that needs improvement or if you want to help implementing one of our roadmap features!

## License

This project is licensed under the **MIT license**.

See [LICENSE](LICENSE.txt) for more information.
