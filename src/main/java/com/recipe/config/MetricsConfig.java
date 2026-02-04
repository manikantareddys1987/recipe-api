package com.recipe.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Metrics Configuration for Recipe API
 * IMPORTANT: Metrics must be created ONCE and reused.
 * - Counter/Timer objects accumulate data over time
 * - Creating new metrics on every call causes errors
 * - Micrometer registry doesn't allow duplicate metric names
 * This is the CORRECT way to use Micrometer metrics!
 */
@Configuration
@Component
public class MetricsConfig {

    private final MeterRegistry meterRegistry;

    /**
     * -- GETTER --
     *  Get the recipe created counter
     *  This counter accumulates ALL recipe creations over time
     */
    // ✅ Cache metrics - Created ONCE, used MANY times
    @Getter
    private Counter recipeCreatedCounter;
    /**
     * -- GETTER --
     *  Get the recipe deleted counter
     *  This counter accumulates ALL recipe deletions over time
     */
    @Getter
    private Counter recipeDeletedCounter;
    /**
     * -- GETTER --
     *  Get the recipe search counter
     *  This counter accumulates ALL searches over time
     */
    @Getter
    private Counter recipeSearchCounter;
    /**
     * -- GETTER --
     *  Get the recipe updated counter
     *  This counter accumulates ALL updates over time
     */
    @Getter
    private Counter recipeUpdatedCounter;
    /**
     * -- GETTER --
     *  Get the recipe search timer
     *  This timer records duration of EVERY search operation
     */
    @Getter
    private Timer recipeSearchTimer;
    /**
     * -- GETTER --
     *  Get the recipe retrieval timer
     *  This timer records duration of EVERY retrieval operation
     */
    @Getter
    private Timer recipeRetrievalTimer;

    public MetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Initialize all metrics when application starts
     * Metrics are created ONCE and accumulate data over time
     */
    @PostConstruct
    public void initializeMetrics() {
        // Create counters - they accumulate counts over time
        this.recipeCreatedCounter = Counter.builder("recipes.created")
                .description("Total number of recipes created")
                .register(meterRegistry);

        this.recipeDeletedCounter = Counter.builder("recipes.deleted")
                .description("Total number of recipes deleted")
                .register(meterRegistry);

        this.recipeSearchCounter = Counter.builder("recipes.searched")
                .description("Total number of recipe searches performed")
                .register(meterRegistry);

        this.recipeUpdatedCounter = Counter.builder("recipes.updated")
                .description("Total number of recipes updated")
                .register(meterRegistry);

        // Create timers - they record duration of operations
        this.recipeSearchTimer = Timer.builder("recipes.search.duration")
                .description("Time taken for recipe search operations")
                .register(meterRegistry);

        this.recipeRetrievalTimer = Timer.builder("recipes.retrieval.duration")
                .description("Time taken for recipe retrieval")
                .register(meterRegistry);
    }

}

