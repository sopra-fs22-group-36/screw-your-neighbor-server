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
| /scoreboard/{gameID}  | GET  | gameID: String | Query  | Content Cell  | Content Cell  | retrieve a list of scores  |
| /players  | POST  | playerName: String  | Content Cell  | Content Cell  | Content Cell  | create a new player  |
| /players/{gameID}  | GET  | gameID: String  | Query  | Content Cell  | Content Cell  | get list of players in a game  |
| /players/{playerID}  | GET  | userID: long  | Query  | Content Cell  | Content Cell  | retrieve a player by ID  |
| /users  | POST  | userName: String, password: String  | Body  | Content Cell  | Content Cell  | register a new user  |
| /users/ | GET  | Content Cell  | Query | Content Cell  | Content Cell  | retrieve a user by ID  |
| /login/ | POST  | userName: String, password: String  | Body  | Content Cell  | Content Cell  | retrieve a user by ID  |
| /logout/ | POST  | ?  | Body  | Content Cell  | Content Cell  | retrieve a user by ID  |



