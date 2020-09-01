package de.erdeanmich.uni

import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.client.WebClient
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(VertxUnitRunner::class)
class PokedexVerticleTest {

    companion object {
        private val logger = LoggerFactory.getLogger(PokedexVerticleTest::class.java)
        private lateinit var vertx: Vertx

        @JvmStatic
        @BeforeClass
        fun before(testContext: TestContext) {
            vertx = Vertx.vertx()
            val async = testContext.async()

            vertx.rxDeployVerticle(PokedexVerticle())
                .subscribe { _, error ->
                    if(error != null) {
                        testContext.fail(error)
                    } else {
                        logger.info("deployed verticle!")
                        async.complete()
                    }
                }
        }

        @JvmStatic
        @AfterClass
        fun after(testContext: TestContext) {
            vertx.close(testContext.asyncAssertSuccess())
        }
    }

    @Test
    fun should_get_all_pokemon(testContext: TestContext) {
        val async = testContext.async()
        WebClient.create(vertx).get(8080,"localhost", "/pokedex/pokemon")
            .rxSend()
            .subscribe { result, error ->
                try {
                    testContext.assertNull(error)
                    testContext.assertNotNull(result)
                    testContext.assertTrue(result.statusCode() in 200 .. 299)
                    val jsonPokemon = result.bodyAsJsonObject()
                        .getJsonArray("list")
                        .map { it as JsonObject }

                    testContext.assertEquals(jsonPokemon.size, 151)
                } catch (t: Throwable) {
                    testContext.fail(t)
                } finally {
                    async.complete()
                }
            }
    }

    @Test
    fun should_get_specific_pokemon_by_id(testContext: TestContext) {
        val async = testContext.async()
        WebClient.create(vertx).get(8080,"localhost", "/pokedex/pokemon/030")
            .rxSend()
            .subscribe { result, error ->
                try {
                    testContext.assertNull(error)
                    testContext.assertNotNull(result)
                    testContext.assertTrue(result.statusCode() in 200 .. 299)
                    val jsonPokemon = result.bodyAsJsonObject()

                    testContext.assertEquals(jsonPokemon.getString("id"), "030")
                    testContext.assertEquals(jsonPokemon.getString("name"), "Nidorina")
                    testContext.assertEquals(jsonPokemon.getJsonArray("types").size(), 1)
                    testContext.assertEquals(jsonPokemon.getJsonArray("types").getString(0), "Poison")
                } catch (t: Throwable) {
                    testContext.fail(t)
                } finally {
                    async.complete()
                }
            }
    }

    @Test
    fun should_return_not_found_with_unknown_id(testContext: TestContext) {
        val async = testContext.async()
        WebClient.create(vertx).get(8080,"localhost", "/pokedex/pokemon/666")
            .rxSend()
            .subscribe { result, error ->
                try {
                    testContext.assertNull(error)
                    testContext.assertNotNull(result)
                    testContext.assertEquals(404, result.statusCode())
                    testContext.assertEquals("Did not found Pokémon with id 666.", result.bodyAsString())
                } catch (t: Throwable) {
                    testContext.fail(t)
                } finally {
                    async.complete()
                }
            }
    }

    @Test
    fun should_filter_pokemon_by_name(testContext: TestContext) {
        val async = testContext.async()
        WebClient.create(vertx).get(8080, "localhost", "/pokedex/pokemon?name=Dugtrio")
            .rxSend()
            .subscribe { result, error ->
                try {
                    testContext.assertNull(error)
                    testContext.assertNotNull(result)
                    testContext.assertTrue(result.statusCode() in 200 .. 299)

                    val filterResult = result.bodyAsJsonObject()
                        .getJsonArray("list")
                        .map { it as JsonObject }

                    testContext.assertEquals(filterResult.size, 1)
                    testContext.assertEquals(filterResult.first().getString("name"), "Dugtrio" )
                    testContext.assertEquals(filterResult.first().getString("id"), "051" )
                } catch (t: Throwable) {
                    testContext.fail(t)
                } finally {
                    async.complete()
                }
            }
    }

    @Test
    fun should_filter_pokemon_by_type(testContext: TestContext) {
        val async = testContext.async()
        WebClient.create(vertx).get(8080, "localhost", "/pokedex/pokemon?type=Poison")
            .rxSend()
            .subscribe { result, error ->
                try {
                    testContext.assertNull(error)
                    testContext.assertNotNull(result)
                    testContext.assertTrue(result.statusCode() in 200 .. 299)

                    logger.info(result.bodyAsJsonObject().encodePrettily())

                    val filterResult = result.bodyAsJsonObject()
                        .getJsonArray("list")
                        .map { it as JsonObject }

                    testContext.assertEquals(filterResult.size, 33)
                    testContext.assertNotNull(filterResult.find { it.getString("name") == "Bellsprout" })
                    testContext.assertNotNull(filterResult.find { it.getString("name") == "Grimer" })
                } catch (t: Throwable) {
                    testContext.fail(t)
                } finally {
                    async.complete()
                }
            }
    }
}
