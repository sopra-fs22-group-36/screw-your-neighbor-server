## Diagrams

### Component Diagram

![Component diagram](uml/component_diagram.svg)

### Class Diagram

### Activity Diagram

## UI Mockups

## REST Interfaces

| Mapping  | Method | Parameter  | Parameter Type | Status Code  | Response | Description  |
| ------------- | ------------- | ------------- | ------------- | ------------- | ------------- | ------------- |
| /game  | POST  | playerIDs: String[] | Body  | ... | ...  | initiate a new game  |
| /scoreboard/{gameID}  | GET  | gameID: String | Query  | ...  | ... | retrieve a list of scores  |
| /players  | POST  | playerName: String  | Body | ...  | ... | create a new player  |
| /players/{gameID}  | GET  | gameID: String  | Query  | ... | ... | retrieve a list of players in a game  |
| /players/{playerID}  | GET  | userID: long  | Query  | ...  | ... | retrieve a player by ID  |
| /users  | POST  | userName: String, password: String  | Body  | ... | ...  | register a new user  |
| /users/{userID} | GET  | userID: String  | Query | ... | ... | retrieve a user by ID  |
| /users/{userName} | GET  | userName: String  | Query | ... | ... | retrieve a user by their username  |
| /login/ | POST  | userName: String, password: String  | Body  | ...  | ... | login with username and password  |
| /logout/ | POST  | ?  | Body  | ... | ... | terminate user session by logging out  |
| /players/{playerID} | PUT | currentScore: integer | Body | - | - | change players score during game |
| /users/{userID} | PUT | newGameScore: integer | Body | - | - | change users overall game score |




