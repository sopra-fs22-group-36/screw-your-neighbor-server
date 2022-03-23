## Diagrams

### Component Diagram

![Component diagram](uml/component_diagram.svg)

### Class Diagram

### Activity Diagram

## UI Mockups

## REST Interfaces

| Mapping  | Method | Parameter  | Parameter Type | Status Code  | Response | Description  |
| ------------- | ------------- | ------------- | ------------- | ------------- | ------------- | ------------- |
| /game  | POST  | ... | ...  | ... | ...  | initiate a new game  |
| /scoreboard/{gameID}  | GET  | gameid '<string>'  | Content Cell  | Content Cell  | Content Cell  | retrieve a list of scores  |
| /players  | POST  | playername <string>  | Content Cell  | Content Cell  | Content Cell  | create a new player  |
| /players/{playerID}  | GET  | Content Cell  | Content Cell  | Content Cell  | Content Cell  | retrieve a player by ID  |
| /users  | POST  | username <string>, password <string>  | Content Cell  | Content Cell  | Content Cell  | register a new user  |
| /users/ | GET  | Content Cell  | Content Cell  | Content Cell  | Content Cell  | retrieve a user by ID  |
| /login/ | POST  | username <string>, password <string>  | Body  | Content Cell  | Content Cell  | retrieve a user by ID  |
| /logout/ | POST  | ?  | Body  | Content Cell  | Content Cell  | retrieve a user by ID  |



