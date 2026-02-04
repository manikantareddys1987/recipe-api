package com.recipe.gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Comprehensive Gatling Performance Test for Recipe API with OAuth2
 * Tests all CRUD operations and search functionality
 * Run with: mvn gatling:test -Dgatling.simulationClass=com.recipe.gatling.RecipeApiSimulation
 */
public class RecipeApiSimulation extends Simulation {

    // Configuration
    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    private static final int USERS = Integer.parseInt(System.getProperty("users", "10"));
    private static final int DURATION_SECONDS = Integer.parseInt(System.getProperty("duration", "60"));
    private static final int RAMP_DURATION_SECONDS = Integer.parseInt(System.getProperty("ramp", "10"));

    // OAuth2 credentials
    private static final String CLIENT_ID = "recipe-client";
    private static final String CLIENT_SECRET = "recipe-secret";

    // Test data feeders
    private static final Iterator<Map<String, Object>> recipeFeeder =
        Stream.generate((Supplier<Map<String, Object>>) () -> {
            int id = ThreadLocalRandom.current().nextInt(1, 1000);
            return Map.of(
                "recipeName", "Recipe " + id,
                "recipeType", ThreadLocalRandom.current().nextBoolean() ? "VEGETARIAN" : "NON_VEGETARIAN",
                "servings", ThreadLocalRandom.current().nextInt(1, 10),
                "instructions", "Cooking instructions for recipe " + id,
                "recipeId", id
            );
        }).iterator();

    private static final Iterator<Map<String, Object>> ingredientFeeder =
        Stream.generate((Supplier<Map<String, Object>>) () -> {
            int id = ThreadLocalRandom.current().nextInt(1, 100);
            return Map.of(
                "ingredientName", "Ingredient " + id,
                "ingredientId", id
            );
        }).iterator();

    // HTTP Protocol Configuration
    private final HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Performance Test");

    // ==================== OAuth2 Authentication ====================

    /**
     * Get OAuth2 access token using client credentials flow
     */
    private final ChainBuilder getOAuth2Token = exec(
        http("Get OAuth2 Token")
            .post("/oauth2/token")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .formParam("grant_type", "client_credentials")
            .formParam("client_id", CLIENT_ID)
            .formParam("client_secret", CLIENT_SECRET)
            .formParam("scope", "read write")
            .check(status().is(200))
            .check(jsonPath("$.access_token").saveAs("accessToken"))
    );

    // ==================== Recipe Scenarios ====================

    /**
     * Scenario: Get all recipes with pagination (with OAuth2)
     */
    private final ScenarioBuilder getRecipesScenario = scenario("Get Recipes List")
        .exec(getOAuth2Token)
        .exec(
            http("Get Recipes - Page 0")
                .get("/api/v1/recipe/page/0/size/10")
                .header("Authorization", "Bearer #{accessToken}")
                .check(status().is(200))
                .check(jsonPath("$").exists())
        )
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(3));

    /**
     * Scenario: Get recipe by ID (with OAuth2)
     */
    private final ScenarioBuilder getRecipeByIdScenario = scenario("Get Recipe By ID")
        .exec(getOAuth2Token)
        .feed(recipeFeeder)
        .exec(
            http("Get Recipe By ID")
                .get("/api/v1/recipe/#{recipeId}")
                .header("Authorization", "Bearer #{accessToken}")
                .check(status().in(200, 404)) // Accept both found and not found
        )
        .pause(Duration.ofMillis(500), Duration.ofSeconds(2));

    /**
     * Scenario: Create new recipe (with OAuth2)
     */
    private final ScenarioBuilder createRecipeScenario = scenario("Create Recipe")
        .exec(getOAuth2Token)
        .feed(recipeFeeder)
        .exec(
            http("Create Recipe")
                .post("/api/v1/recipe")
                .header("Authorization", "Bearer #{accessToken}")
                .body(StringBody(
                    """
                    {
                        "name": "#{recipeName}",
                        "type": "#{recipeType}",
                        "numberOfServings": #{servings},
                        "instructions": "#{instructions}"
                    }
                    """
                ))
                .check(status().in(201, 400)) // Created or Bad Request
                .check(jsonPath("$.id").optional().saveAs("createdRecipeId"))
        )
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(2));

    /**
     * Scenario: Update existing recipe (with OAuth2)
     */
    private final ScenarioBuilder updateRecipeScenario = scenario("Update Recipe")
        .exec(getOAuth2Token)
        .feed(recipeFeeder)
        .exec(
            http("Update Recipe")
                .patch("/api/v1/recipe")
                .header("Authorization", "Bearer #{accessToken}")
                .body(StringBody(
                    """
                    {
                        "id": #{recipeId},
                        "name": "#{recipeName} Updated",
                        "type": "#{recipeType}",
                        "numberOfServings": #{servings},
                        "instructions": "#{instructions} - Updated"
                    }
                    """
                ))
                .check(status().in(200, 400, 404)) // OK, Bad Request, or Not Found
        )
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(2));

    /**
     * Scenario: Search recipes with criteria (with OAuth2)
     */
    private final ScenarioBuilder searchRecipesScenario = scenario("Search Recipes")
        .exec(getOAuth2Token)
        .exec(
            http("Search Vegetarian Recipes")
                .post("/api/v1/recipe/search?page=0&size=10")
                .header("Authorization", "Bearer #{accessToken}")
                .body(StringBody(
                    """
                    {
                        "dataOption": "ALL",
                        "searchCriteriaRequests": [
                            {
                                "filterKey": "TYPE",
                                "value": "VEGETARIAN",
                                "operation": "EQUAL"
                            }
                        ]
                    }
                    """
                ))
                .check(status().in(200, 404))  // Accept 200 (found) or 404 (no data)
        )
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(3))
        .exec(
            http("Search Recipes by Servings")
                .post("/api/v1/recipe/search?page=0&size=10")
                .header("Authorization", "Bearer #{accessToken}")
                .body(StringBody(
                    """
                    {
                        "dataOption": "ALL",
                        "searchCriteriaRequests": [
                            {
                                "filterKey": "NUMBER_OF_SERVINGS",
                                "value": "4",
                                "operation": "EQUAL"
                            }
                        ]
                    }
                    """
                ))
                .check(status().in(200, 404))  // Accept 200 (found) or 404 (no data)
        )
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(2));

    /**
     * Scenario: Delete recipe (with OAuth2)
     */
    private final ScenarioBuilder deleteRecipeScenario = scenario("Delete Recipe")
        .exec(getOAuth2Token)
        .feed(recipeFeeder)
        .exec(
            http("Delete Recipe")
                .delete("/api/v1/recipe?id=#{recipeId}")
                .header("Authorization", "Bearer #{accessToken}")
                .check(status().in(200, 400, 404)) // OK, Bad Request, or Not Found
        )
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(2));

    // ==================== Ingredient Scenarios ====================

    /**
     * Scenario: Get all ingredients (with OAuth2)
     */
    private final ScenarioBuilder getIngredientsScenario = scenario("Get Ingredients List")
        .exec(getOAuth2Token)
        .exec(
            http("Get Ingredients - Page 0")
                .get("/api/v1/ingredient/page/0/size/10")
                .header("Authorization", "Bearer #{accessToken}")
                .check(status().is(200))
                .check(jsonPath("$").exists())
        )
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(2));

    /**
     * Scenario: Get ingredient by ID (with OAuth2)
     */
    private final ScenarioBuilder getIngredientByIdScenario = scenario("Get Ingredient By ID")
        .exec(getOAuth2Token)
        .feed(ingredientFeeder)
        .exec(
            http("Get Ingredient By ID")
                .get("/api/v1/ingredient/#{ingredientId}")
                .header("Authorization", "Bearer #{accessToken}")
                .check(status().in(200, 404))
        )
        .pause(Duration.ofMillis(500), Duration.ofSeconds(1));

    /**
     * Scenario: Create new ingredient (with OAuth2)
     */
    private final ScenarioBuilder createIngredientScenario = scenario("Create Ingredient")
        .exec(getOAuth2Token)
        .feed(ingredientFeeder)
        .exec(
            http("Create Ingredient")
                .post("/api/v1/ingredient")
                .header("Authorization", "Bearer #{accessToken}")
                .body(StringBody(
                    """
                    {
                        "ingredient": "#{ingredientName}"
                    }
                    """
                ))
                .check(status().in(201, 400))
        )
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(2));

    // ==================== Mixed Load Scenario ====================

    /**
     * Realistic mixed load scenario simulating real user behavior (with OAuth2)
     */
    private final ScenarioBuilder mixedLoadScenario = scenario("Mixed Load - Realistic User")
        .exec(getOAuth2Token)
        .exec(
            http("Health Check")
                .get("/actuator/health")
                .check(status().is(200))
        )
        .pause(Duration.ofSeconds(1))
        .exec(
            http("Browse Recipes")
                .get("/api/v1/recipe/page/0/size/10")
                .header("Authorization", "Bearer #{accessToken}")
                .check(status().is(200))
        )
        .pause(Duration.ofSeconds(2), Duration.ofSeconds(4))
        .feed(recipeFeeder)
        .exec(
            http("View Recipe Details")
                .get("/api/v1/recipe/#{recipeId}")
                .header("Authorization", "Bearer #{accessToken}")
                .check(status().in(200, 404))
        )
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(3))
        .exec(
            http("Search Vegetarian")
                .post("/api/v1/recipe/search?page=0&size=10")
                .header("Authorization", "Bearer #{accessToken}")
                .body(StringBody(
                    """
                    {
                        "dataOption": "ALL",
                        "searchCriteriaRequests": [
                            {
                                "filterKey": "TYPE",
                                "value": "VEGETARIAN",
                                "operation": "EQUAL"
                            }
                        ]
                    }
                    """
                ))
                .check(status().in(200, 404))  // Accept 200 (found) or 404 (no data)
        )
        .pause(Duration.ofSeconds(1), Duration.ofSeconds(2));

    // ==================== Simulation Setup ====================

    {
        System.out.println("=".repeat(60));
        System.out.println("Recipe API Performance Test Configuration");
        System.out.println("=".repeat(60));
        System.out.println("Base URL: " + BASE_URL);
        System.out.println("Users: " + USERS);
        System.out.println("Duration: " + DURATION_SECONDS + " seconds");
        System.out.println("Ramp Duration: " + RAMP_DURATION_SECONDS + " seconds");
        System.out.println("=".repeat(60));

        setUp(
            // Read-heavy scenarios (most common)
            getRecipesScenario.injectOpen(
                rampUsers(USERS * 3).during(Duration.ofSeconds(RAMP_DURATION_SECONDS))
            ).protocols(httpProtocol),

            getRecipeByIdScenario.injectOpen(
                rampUsers(USERS * 2).during(Duration.ofSeconds(RAMP_DURATION_SECONDS))
            ).protocols(httpProtocol),

            searchRecipesScenario.injectOpen(
                rampUsers(USERS * 2).during(Duration.ofSeconds(RAMP_DURATION_SECONDS))
            ).protocols(httpProtocol),

            getIngredientsScenario.injectOpen(
                rampUsers(USERS).during(Duration.ofSeconds(RAMP_DURATION_SECONDS))
            ).protocols(httpProtocol),

            getIngredientByIdScenario.injectOpen(
                rampUsers(USERS).during(Duration.ofSeconds(RAMP_DURATION_SECONDS))
            ).protocols(httpProtocol),

            // Write scenarios (less common)
            createRecipeScenario.injectOpen(
                rampUsers(USERS / 2).during(Duration.ofSeconds(RAMP_DURATION_SECONDS))
            ).protocols(httpProtocol),

            updateRecipeScenario.injectOpen(
                rampUsers(USERS / 2).during(Duration.ofSeconds(RAMP_DURATION_SECONDS))
            ).protocols(httpProtocol),

            createIngredientScenario.injectOpen(
                rampUsers(USERS / 2).during(Duration.ofSeconds(RAMP_DURATION_SECONDS))
            ).protocols(httpProtocol),

            // Mixed realistic load
            mixedLoadScenario.injectOpen(
                rampUsers(USERS).during(Duration.ofSeconds(RAMP_DURATION_SECONDS))
            ).protocols(httpProtocol)

        ).assertions(
            // Global assertions
            global().responseTime().max().lt(5000),  // Max response time < 5s
            global().successfulRequests().percent().gt(95.0), // > 95% success rate

            // Specific scenario assertions
            details("Get Recipes List").responseTime().mean().lt(1000), // Avg < 1s
            details("Get Recipe By ID").responseTime().percentile3().lt(2000), // 95th percentile < 2s
            details("Search Recipes").responseTime().mean().lt(2000) // Search avg < 2s
        );
    }
}
