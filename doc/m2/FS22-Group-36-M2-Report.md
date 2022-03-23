## Diagrams

### Component Diagram

![Component diagram](uml/component_diagram.svg)

### Class Diagram


### Activity Diagram

## UI Mockups

## REST Interfaces

### Endpoints

#### /games endpoint

| Mapping  | Method | Parameter  | Parameter Type | Status Code  | Response | Description |
| ------------- | ------------- | ------------- | ------------- | ------------- | ------------- | ------------- |
| /games  | POST  | playerIDs: String[] | Body  | ... | ...  | initiate a new game  |
| /games/{gameID}  | GET  | - | Query  | ... | ...  | retrieve all information of a game |
| /games/{gameID}  | PUT  | matchesPlayed: int, ... | Body  | ... | ...  | change state of a running game |

#### /matches endpoint

| Mapping  | Method | Parameter  | Parameter Type | Status Code  | Response | Description |
| ------------- | ------------- | ------------- | ------------- | ------------- | ------------- | ------------- |
| /matches  | POST  | gameID: String | Body  | ... | matchID: String, numberOfRounds: int  | initiate a new match  |
| /matches{matchID}  | GET  | - | Query  | ... |  | retrieve all information of a match  |
| /matches/{matchID}  | PUT  | roundsPlayed: int, ... | Body  | ... | ...  | change state of a running match |

#### /cards endpoint

| Mapping  | Method | Parameter  | Parameter Type | Status Code  | Response | Description |
| ------------- | ------------- | ------------- | ------------- | ------------- | ------------- | ------------- |
| /cards/distribute  | GET  | gameID: String, numberOfCards: int | Body  | ... | list of playerIDs + cards | get (distribute) cards to players in a game |
| /cards/draw  | GET  | playerID: String | Body  | ... | card | get (draw) one card to one player |
| /cards/{playerID}  | GET  | gameID: String | Query  | ... | ...  | get a players current cards collection |
| /cards/{collectionID}  | DELETE  | cards: Card | Body  | ... | ...  | remove a card of cards collection |
| /cards/{collectionID}  | PUT/POST?  | cards: Card | Body  | ... | ...  | add a card to cards collection |

#### /players endpoint

| Mapping  | Method | Parameter  | Parameter Type | Status Code  | Response | Description |
| ------------- | ------------- | ------------- | ------------- | ------------- | ------------- | ------------- |
| /players  | POST  | playerName: String  | Body | 201<br>409  | Player<br>duplicate playerName | create a new player  |
| /players/{gameID}  | GET  | gameID: String  | Query  | 200<br>404 | list of players<br>no such game or no players found | retrieve a list of players in a game |
| /players/{playerID}  | GET  | userID: long  | Query  | 200<br>404  | player<br>player not found | retrieve a player by ID  |
| /players/{playerID} | PUT | currentScore: int | Body | 204<br>404 | <br>player not found<br> | change players score during game |


#### /users endpoint

| Mapping  | Method | Parameter  | Parameter Type | Status Code  | Response | Description |
| ------------- | ------------- | ------------- | ------------- | ------------- | ------------- | ------------- |
| /users  | POST  | userName: String, password: String  | Body  | ... | ...  | register a new user  |
| /players/{playerID}  | GET  | userID: long  | Query  | 200<br>404  | player<br>player not found | retrieve a player by ID  |
| /users/{userID} | GET  | userID: String  | Query | 200<br>404  | user<br>user not found | retrieve a user by ID  |
| /users/{userName} | GET  | userName: String  | Query | 200<br>404  | user<br>user not found | retrieve a user by their username  |
| /users/{userID} | PUT | newGameScore: int


#### other endpoints

| Mapping  | Method | Parameter  | Parameter Type | Status Code  | Response | Description |
| ------------- | ------------- | ------------- | ------------- | ------------- | ------------- | ------------- |
| /login/ | POST  | userName: String, password: String  | Body  | ...  | user<br>user not found | login with username and password  |
| /logout/ | POST  | ?  | Body  | ? | ? | terminate user session by logging out  |


### Detailed Interface Specification

#### Game
| Interface Name  | Fieldname  | Type | Description  |
| ------------- | ------------- | ------------- | ------------- |
| GameGetDTO | gameID | String | Unique identifier of the game |
|  | players | PlayersGetDTO[] | List of players that participate in that game |


#### Match
| Interface Name  | Fieldname  | Type | Description  |
| ------------- | ------------- | ------------- | ------------- |
| MatchPostDTO | gameID | String | IDs of the game this match belongs |
| MatchGetDTO | matchID | String | Unique identifier of the match |
|  | noOfRounds | int | Number of rounds (i.e. cards per player) in this match |
|  | noOfTricksPerPlayer | int | Number of tricks per player in this match |
| MatchGetDTO | gameID | String | ID of the game this match belongs |


#### Cards
| Interface Name  | Fieldname  | Type | Description  |
| ------------- | ------------- | ------------- | ------------- |
| CardGetDTO | ------------- | ------------- | ------------- |
|  | cardID | String | Unique identifier of card |
|  | rank | int | Rank of a card |
|  | name | String | Name of a card |
| CardsGetDTO | cardsCollectionID | String | Unique identifier of the cards collection |
| | cards | CardGetDTO[] | List of cards |
| | playerID | String | String of cards holding player |
| CardsPostDTO | cardID | String | ID of a card |


#### Player

| Interface Name  | Fieldname  | Type | Description  |
| ------------- | ------------- | ------------- | ------------- |
| PlayerPostDTO | playersName | String | Name of the player (must be unique) |
| PlayerGetDTO | playerID | String | Players unique identifier |
|  | playerid | String | Unique identifier of player |
|  | playersName | String | Name of the player |
|  | score | int | Players score in a running game |
|  | gameID | String | Unique identifier of game, a player is (or was?) participating |


#### User

#### Login, Logout

| Interface Name  | Fieldname  | Type | Description  |
| ------------- | ------------- | ------------- | ------------- |
| LoginPostDTO | userName | String | User's name (must be unique) |
| | password | String | User's password |
| LogoutPostDTO | userSessionID ? | String | ID of the user login session ? |


