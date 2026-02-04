package com.recipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class RecipeApplication {

	private static final Logger logger = LoggerFactory.getLogger(RecipeApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(RecipeApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void applicationReady() {
		logger.info("═══════════════════════════════════════════════════════════════");
		logger.info("Recipe Management API - v1.0.0");
		logger.info("═══════════════════════════════════════════════════════════════");
		logger.info("✓ API endpoints running on: http://localhost:8080");
		logger.info("✓ Swagger UI available at: http://localhost:8080/swagger-ui.html");
		logger.info("✓ OpenAPI docs at: http://localhost:8080/v3/api-docs");
		logger.info("✓ Health check: http://localhost:8080/actuator/health");
		logger.info("✓ Metrics endpoint: http://localhost:8080/actuator/metrics");
		logger.info("✓ Prometheus metrics: http://localhost:8080/actuator/prometheus");
		logger.info("✓ H2 Database Console: http://localhost:8080/h2-console");
		logger.info("═══════════════════════════════════════════════════════════════");
		logger.info("Features:");
		logger.info("  • Advanced Recipe Search with JPA Criteria API");
		logger.info("  • OAuth2 Resource Server Security");
		logger.info("  • Prometheus Metrics & Monitoring");
		logger.info("  • Comprehensive REST API Documentation");
		logger.info("═══════════════════════════════════════════════════════════════");
	}
}
