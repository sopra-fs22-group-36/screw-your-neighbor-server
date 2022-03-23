## Diagrams

### Component Diagram

![Component diagram](uml/component_diagram.svg)

### Class Diagram

### Activity Diagram

## UI Mockups

## REST Interfaces

| Mapping  | Method | Parameter  | Parameter Type | Status Code  | Response | Description  |
| ------------- | ------------- | ------------- | ------------- | ------------- | ------------- | ------------- |
| /game  | POST  | playerids: String[] | Body  | ... | ...  | initiate a new game  |
| /scoreboard/{gameID}  | GET  | gameid: String | Query  | Content Cell  | Content Cell  | retrieve a list of scores  |
| /players  | POST  | playername: String  | Content Cell  | Content Cell  | Content Cell  | create a new player  |
| /players/{playerID}  | GET  | Content Cell  | Query  | Content Cell  | Content Cell  | retrieve a player by ID  |
| /users  | POST  | username: String, password: String  | Body  | Content Cell  | Content Cell  | register a new user  |
| /users/ | GET  | Content Cell  | Query | Content Cell  | Content Cell  | retrieve a user by ID  |
| /login/ | POST  | username: String, password: String  | Body  | Content Cell  | Content Cell  | retrieve a user by ID  |
| /logout/ | POST  | ?  | Body  | Content Cell  | Content Cell  | retrieve a user by ID  |



