package de.erdeanmich.uni

import io.reactivex.Single
import io.vertx.core.Promise
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.http.HttpServer
import io.vertx.reactivex.core.http.HttpServerResponse
import io.vertx.reactivex.ext.web.Router
import io.vertx.reactivex.ext.web.RoutingContext

class PokedexVerticle : AbstractVerticle() {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val pokemon = ArrayList<Pokemon>()
    private val router by lazy { Router.router(vertx) }

    override fun start(promise: Promise<Void>) {
        createRestRoutes()
        parsePokemonFromFile().flatMap { _ ->
            startRestServer()
        }.subscribe { _, error ->
            if(error != null) {
                promise.fail(error)
                logger.error("Error while startup of Pokédexserver", error)
                throw error
            } else {
                promise.complete()
                logger.info("Successfully started Pokédexserver!")
            }
        }
    }

    private fun createRestRoutes() {
        router.get("/pokedex/pokemon").handler(this::getAllPokemonFiltered)
        router.get("/pokedex/pokemon/:id").handler(this::getPokemonById)
    }

    private fun getPokemonById(routingContext: RoutingContext) {
        val id = routingContext.request().getParam("id")
        logger.info("get Pokémon with id $id")
        val pokemon = pokemon.find { it.id == id }
        if(pokemon != null) {
            routingContext.response().addJsonContentType()
            routingContext.response().end(JsonObject.mapFrom(pokemon).encodePrettily())
        } else {
            routingContext.response().statusCode =  404
            routingContext.response().end("Did not found Pokémon with id $id.")
        }
    }

    private fun getAllPokemonFiltered(routingContext: RoutingContext) {
        logger.info("get all pokemon with filter ${routingContext.request().query()}")

        val filteredPokemon = pokemon
            .filter { filterByProperty("name", routingContext, it) }
            .filter { filterByProperty("type", routingContext, it) }
            .map { JsonObject.mapFrom(it) }


        val responseArray = JsonArray(filteredPokemon)
        routingContext.response().addJsonContentType()
        routingContext.response().end(JsonObject().put("list", responseArray).encodePrettily())
    }

    private fun filterByProperty(property: String, routingContext: RoutingContext, pokemon: Pokemon): Boolean {
        val filterProperty = routingContext.queryParam(property).firstOrNull() ?: return true

        return when(property) {
            "name" -> pokemon.name == filterProperty
            "type" -> pokemon.types.contains(filterProperty)
            else -> true // unknown properties are ignored
        }
    }

    private fun startRestServer(): Single<HttpServer> {
        return vertx.createHttpServer()
            .requestHandler(router)
            .rxListen(8080)
    }

    private fun parsePokemonFromFile() : Single<Boolean> {
        return vertx.fileSystem().rxReadFile("pokedex.json")
            .map { buffer -> JsonObject(buffer.toString()) }
            .map { json ->
                json.getJsonArray("results")
                    .map { it as JsonObject }
                    .map (this::convertJsonPokemonToPokemonType)
            }
            .map { parsedPokemon ->
                logger.info("Parsed ${parsedPokemon.size} Pokémon from file!")
                pokemon.clear()
                pokemon.addAll(parsedPokemon)
            }
    }

    private fun convertJsonPokemonToPokemonType(json: JsonObject): Pokemon {
        val sprites = json.getJsonObject("sprites")
        return Pokemon(
            json.getString("national_number"),
            listOf(sprites.getString("normal"), sprites.getString("large"), sprites.getString("animated")),
            json.getString("name"),
            json.getJsonArray("type").map { it as String},
            json.getInteger("total"),
            json.getInteger("hp"),
            json.getInteger("attack"),
            json.getInteger("defense"),
            json.getInteger("sp_atk"),
            json.getInteger("sp_def"),
            json.getInteger("speed")
        )
    }

    fun HttpServerResponse.addJsonContentType() : HttpServerResponse {
        return this.putHeader("content-type", "application/json")
    }
}
