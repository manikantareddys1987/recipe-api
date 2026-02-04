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
 * Stress Test for Recipe API with OAuth2
 * Tests system behavior under heavy load
 * Run with: mvn gatling:test -Dgatling.simulationClass=com.recipe.gatling.StressTest
 */
public class StressTest extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    private static final int PEAK_USERS = Integer.parseInt(System.getProperty("peakUsers", "100"));
    private static final int DURATION_MINUTES = Integer.parseInt(System.getProperty("duration", "5"));

    // OAuth2 credentials
    private static final String CLIENT_ID = "recipe-client";
    private static final String CLIENT_SECRET = "recipe-secret";

    private static final Iterator<Map<String, Object>> searchFeeder =
        Stream.generate((Supplier<Map<String, Object>>) () ->
            Map.of(
                "servings", ThreadLocalRandom.current().nextInt(1, 8),
                "type", ThreadLocalRandom.current().nextBoolean() ? "VEGETARIAN" : "NON_VEGETARIAN"
            )
        ).iterator();

    private final HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Stress Test");

    /**
     * Get OAuth2 access token
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

    /**
     * Heavy read scenario with OAuth2
     */
    private final ScenarioBuilder heavyReadScenario = scenario("Heavy Read Load")
        .exec(getOAuth2Token)
        .during(Duration.ofMinutes(DURATION_MINUTES)).on(
            exec(
                http("Get Recipes")
                    .get("/api/v1/recipe/page/0/size/20")
                    .header("Authorization", "Bearer #{accessToken}")
                    .check(status().is(200))
            )
            .pause(Duration.ofMillis(100), Duration.ofMillis(500))
            .feed(searchFeeder)
            .exec(
                http("Search")
                    .post("/api/v1/recipe/search?page=0&size=10")
                    .header("Authorization", "Bearer #{accessToken}")
                    .body(StringBody(
                        """
                        {
                            "dataOption": "ALL",
                            "searchCriteriaRequests": [
                                {
                                    "filterKey": "NUMBER_OF_SERVINGS",
                                    "value": "#{servings}",
                                    "operation": "EQUAL"
                                }
                            ]
                        }
                        """
                    ))
                    .check(status().in(200, 404))
            )
            .pause(Duration.ofMillis(100), Duration.ofMillis(300))
        );

    /**
     * Spike scenario - sudden burst of traffic with OAuth2
     */
    private final ScenarioBuilder spikeScenario = scenario("Traffic Spike")
        .exec(getOAuth2Token)
        .exec(
            http("Health Check")
                .get("/actuator/health")
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(50))
        .exec(
            http("Get Recipes")
                .get("/api/v1/recipe/page/0/size/10")
                .header("Authorization", "Bearer #{accessToken}")
                .check(status().is(200))
        );

    {
        System.out.println("=".repeat(60));
        System.out.println("Recipe API Stress Test");
        System.out.println("=".repeat(60));
        System.out.println("Base URL: " + BASE_URL);
        System.out.println("Peak Users: " + PEAK_USERS);
        System.out.println("OAuth2: Enabled (client_credentials)");
        System.out.println("Duration: " + DURATION_MINUTES + " minutes");
        System.out.println("=".repeat(60));
        System.out.println("WARNING: This will generate heavy load!");
        System.out.println("=".repeat(60));

        setUp(
            // Gradual ramp-up
            heavyReadScenario.injectOpen(
                rampUsers(PEAK_USERS / 2).during(Duration.ofSeconds(30)),
                constantUsersPerSec(10).during(Duration.ofMinutes(DURATION_MINUTES))
            ),

            // Spike test - sudden burst
            spikeScenario.injectOpen(
                nothingFor(Duration.ofSeconds(60)),
                atOnceUsers(PEAK_USERS) // Sudden spike!
            )

        ).protocols(httpProtocol)
         .assertions(
             global().responseTime().max().lt(10000), // Max 10s under stress
             global().successfulRequests().percent().gt(80.0) // At least 80% success under stress
         );
    }
}
