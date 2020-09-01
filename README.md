# Vert.x REST Pokédex 
 This is a small microservice implementing the Pokédex of the first game generation. 
 
## Endpoints 
### GET: /pokedex/pokemon?name=name&type=type
This endpoint can retrieve a list of pokemon, optionally filtered by name or by type. 
The response will look as follows:

```json
 {
  "list" : [ {
    "id" : "001",
    "spriteUrls" : [ "https://img.pokemondb.net/sprites/omega-ruby-alpha-sapphire/dex/normal/bulbasaur.png", "https://img.pokemondb.net/artwork/bulbasaur.jpg", "https://img.pokemondb.net/sprites/black-white/anim/normal/bulbasaur.gif" ],
    "name" : "Bulbasaur",
    "types" : [ "Grass", "Poison" ],
    "total" : 318,
    "hp" : 45,
    "attack" : 49,
    "defense" : 49,
    "specialAttack" : 65,
    "specialDefense" : 65,
    "speed" : 45
  }, {
    "id" : "002",
    "spriteUrls" : [ "https://img.pokemondb.net/sprites/omega-ruby-alpha-sapphire/dex/normal/ivysaur.png", "https://img.pokemondb.net/artwork/ivysaur.jpg", "https://img.pokemondb.net/sprites/black-white/anim/normal/ivysaur.gif" ],
    "name" : "Ivysaur",
    "types" : [ "Grass", "Poison" ],
    "total" : 405,
    "hp" : 60,
    "attack" : 62,
    "defense" : 63,
    "specialAttack" : 80,
    "specialDefense" : 80,
    "speed" : 60
  }, {
    "and": "so on ..."
  }]
 }
```
 
### GET: /pokedex/pokemon/:id
This endpoint fetches one specific Pokémon by id. 
The response will look as follows: 
```json
{
    "id" : "001",
    "spriteUrls" : [ "https://img.pokemondb.net/sprites/omega-ruby-alpha-sapphire/dex/normal/bulbasaur.png", "https://img.pokemondb.net/artwork/bulbasaur.jpg", "https://img.pokemondb.net/sprites/black-white/anim/normal/bulbasaur.gif" ],
    "name" : "Bulbasaur",
    "types" : [ "Grass", "Poison" ],
    "total" : 318,
    "hp" : 45,
    "attack" : 49,
    "defense" : 49,
    "specialAttack" : 65,
    "specialDefense" : 65,
    "speed" : 45
}
```

### Usage
#### Local
Locally you can run the service by cloning the project and running: 
```
mvn clean install vertx:run 
```
Then you can use the endpoints under `localhost:8080` .

#### With Docker
You can also build a docker image with: 
```shell script
docker build -t erdeanmich/vertx-rest-pokedex .
```
Afterwards you can run it with: 
```shell script
docker run -t -i -p 8080:8080 erdeanmich/vertx-rest-pokedex
```

## Credits
The underlying data is a minimized version of the json file found at 
<a href="https://github.com/joseluisq/pokemons">José Luis Quintana's GitHub account</a>.
Thank you for publishing! <3
