#!/bin/bash

echo "=========================================="
echo "Recipe API - Gatling Performance Tests"
echo "=========================================="
echo ""

echo "Building the project..."
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo ""
echo "✓ Build successful"
echo ""
echo "Running Gatling performance tests..."
echo ""

./mvnw gatling:test -Dgatling.simulationClass=com.recipe.gatling.RecipeApiSimulation

EXIT_CODE=$?

echo ""
if [ $EXIT_CODE -eq 0 ]; then
    echo "=========================================="
    echo "✅ Performance tests completed successfully!"
    echo "=========================================="
    echo ""
    echo "Results can be found in: target/gatling/"
    echo ""
    echo "To view the HTML report, open:"
    echo "  target/gatling/*/index.html"
else
    echo "=========================================="
    echo "❌ Performance tests failed!"
    echo "=========================================="
    echo ""
    echo "Common issues:"
    echo "  - Application not running (start with: docker-compose up -d)"
    echo "  - Gatling simulation class not found"
    echo "  - Network connectivity issues"
fi

echo ""
exit $EXIT_CODE
