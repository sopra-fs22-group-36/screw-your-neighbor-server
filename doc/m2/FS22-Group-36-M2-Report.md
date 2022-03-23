## Diagrams

### Component Diagram

![Component diagram](uml/component_diagram.svg)

### Class Diagram

### Activity Diagram

## UI Mockups

## REST Interfaces

#### /game endpoint

| Mapping  | Method | Parameter  | Parameter Type | Status Code  | Response | Description  |
| ------------- | ------------- | ------------- | ------------- | ------------- | ------------- | ------------- |
| /game  | POST  | playerIDs: String[] | Body  | ... | ...  | initiate a new game  |
| /game/{gameID}  | PUT  | roundsPlayed: int, ... | Body  | ... | ...  | change state of a new game  |

#### /players endpoint

| Mapping  | Method | Parameter  | Parameter Type | Status Code  | Response | Description  |
| ------------- | ------------- | ------------- | ------------- | ------------- | ------------- | ------------- |
| /players  | POST  | playerName: String  | Body | 201<br>409  | Player<br>duplicate playerName | create a new player  |
| /players/{gameID}  | GET  | gameID: String  | Query  | 200<br>404 | list of users<br>no such game or no players found | retrieve a list of players in a game |
| /players/{playerID}  | GET  | userID: long  | Query  | 200<br>404  | player<br>player not found | retrieve a player by ID  |
| /players/{playerID} | PUT | currentScore: integer | Body | 204<br>404 | <br>player not found<br> | change players score during game |


#### /users endpoint

| Mapping  | Method | Parameter  | Parameter Type | Status Code  | Response | Description  |
| ------------- | ------------- | ------------- | ------------- | ------------- | ------------- | ------------- |
| /players/{playerID}  | GET  | userID: long  | Query  | 200<br>404  | player<br>player not found | retrieve a player by ID  |
| /users  | POST  | userName: String, password: String  | Body  | ... | ...  | register a new user  |
| /users/{userID} | GET  | userID: String  | Query | 200<br>404  | user<br>user not found | retrieve a user by ID  |
| /users/{userName} | GET  | userName: String  | Query | 200<br>404  | user<br>user not found | retrieve a user by their username  |
| /users/{userID} | PUT | newGameScore: integer | Body | 204<br>404 |  <br>user not found<br> | change users overall game score |

#### other endpoints

| Mapping  | Method | Parameter  | Parameter Type | Status Code  | Response | Description  |
| ------------- | ------------- | ------------- | ------------- | ------------- | ------------- | ------------- |
| /login/ | POST  | userName: String, password: String  | Body  | ...  | user<br>user not found | login with username and password  |
| /logout/ | POST  | ?  | Body  | ? | ? | terminate user session by logging out  |
| /scoreboard/{gameID}  | GET  | gameID: String | Query  | 200<br>404 | list of scores<br>no such game or no players found | retrieve a list of scores (not sure if we need this endpoint, or if we interact via players endpoint where we also get the score of a player? |



