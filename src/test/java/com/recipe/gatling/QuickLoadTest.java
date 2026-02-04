package com.recipe.gatling;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Quick Load Test for Recipe API with OAuth2 Authentication
 * Simplified version for smoke testing
 * Run with: mvn gatling:test -Dgatling.simulationClass=com.recipe.gatling.QuickLoadTest
 */
public class QuickLoadTest extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    private static final int USERS = Integer.parseInt(System.getProperty("users", "5"));

    // OAuth2 credentials
    private static final String CLIENT_ID = "recipe-client";
    private static final String CLIENT_SECRET = "recipe-secret";

    private final HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Quick Load Test");

    /**
     * Quick smoke test scenario with OAuth2 authentication
     */
    private final ScenarioBuilder quickTest = scenario("Quick Load Test")
        // Step 1: Get OAuth2 token
        .exec(
            http("Get OAuth2 Token")
                .post("/oauth2/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .formParam("grant_type", "client_credentials")
                .formParam("client_id", CLIENT_ID)
                .formParam("client_secret", CLIENT_SECRET)
                .formParam("scope", "read write")
                .check(status().is(200))
                .check(jsonPath("$.access_token").saveAs("accessToken"))
        )
        .pause(Duration.ofMillis(500))

        // Step 2: Health check (no auth needed)
        .exec(
            http("Health Check")
                .get("/actuator/health")
                .check(status().is(200))
                .check(jsonPath("$.status").is("UP"))
        )
        .pause(1)

        // Step 3: Get Recipes (with auth)
        .exec(
            http("Get Recipes")
                .get("/api/v1/recipe/page/0/size/10")
                .header("Authorization", "Bearer #{accessToken}")
                .check(status().is(200))
                .check(jsonPath("$").exists())
        )
        .pause(1)

        // Step 4: Get Ingredients (with auth)
        .exec(
            http("Get Ingredients")
                .get("/api/v1/ingredient/page/0/size/10")
                .header("Authorization", "Bearer #{accessToken}")
                .check(status().is(200))
                .check(jsonPath("$").exists())
        )
        .pause(1)

        // Step 5: Search Recipes (with auth) - 404 is OK if no matching data
        .exec(
            http("Search Recipes")
                .post("/api/v1/recipe/search?page=0&size=5")
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
                .check(status().in(200, 404))  // Accept 200 (found) or 404 (not found)
        );

    {
        System.out.println("=".repeat(50));
        System.out.println("Recipe API Quick Load Test");
        System.out.println("=".repeat(50));
        System.out.println("Base URL: " + BASE_URL);
        System.out.println("Users: " + USERS);
        System.out.println("OAuth2: Enabled (client_credentials)");
        System.out.println("=".repeat(50));

        setUp(
            quickTest.injectOpen(
                atOnceUsers(USERS)
            )
        ).protocols(httpProtocol)
         .assertions(
             global().responseTime().max().lt(3000),
             global().successfulRequests().percent().gt(90.0)
         );
    }
}

