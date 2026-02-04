#!/bin/bash

echo "=========================================="
echo "Recipe API - Gatling Performance Tests"
echo "=========================================="
echo ""

# Check if application is running
echo "Checking if application is running..."
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✓ Application is running"
else
    echo "⚠️  Application not detected at http://localhost:8080"
    echo "   Start with: docker-compose up -d"
    echo "   Or run: ./mvnw spring-boot:run"
    echo ""
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo ""
echo "Select test type:"
echo "1) Quick Load Test (5 users, ~30 seconds)"
echo "2) Full Performance Test (10+ users, 60 seconds)"
echo "3) Stress Test (100 users, 5 minutes)"
echo "4) Custom (specify simulation class)"
echo ""
read -p "Enter choice [1-4]: " choice

case $choice in
    1)
        SIMULATION="com.recipe.gatling.QuickLoadTest"
        echo "Running Quick Load Test..."
        ;;
    2)
        SIMULATION="com.recipe.gatling.RecipeApiSimulation"
        echo "Running Full Performance Test..."
        ;;
    3)
        SIMULATION="com.recipe.gatling.StressTest"
        echo "⚠️  WARNING: Stress test will generate heavy load!"
        read -p "Continue? (y/N): " -n 1 -r
        echo ""
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 0
        fi
        ;;
    4)
        read -p "Enter simulation class name: " SIMULATION
        ;;
    *)
        echo "Invalid choice. Using Quick Load Test..."
        SIMULATION="com.recipe.gatling.QuickLoadTest"
        ;;
esac

echo ""
echo "Running Gatling test: $SIMULATION"
echo ""
echo "Note: Skipping compilation (run './mvnw clean install -DskipTests' separately if needed)"
echo ""

./mvnw gatling:test -Dgatling.simulationClass=$SIMULATION

EXIT_CODE=$?

echo ""
if [ $EXIT_CODE -eq 0 ]; then
    echo "=========================================="
    echo "✅ Performance tests completed successfully!"
    echo "=========================================="
    echo ""
    echo "Results location: target/gatling/"
    echo ""
    echo "📊 To view HTML reports:"
    LATEST_REPORT=$(find target/gatling -name "index.html" -type f | sort -r | head -1)
    if [ -n "$LATEST_REPORT" ]; then
        echo "   $LATEST_REPORT"
        echo ""
        echo "Opening report in browser..."
        if [[ "$OSTYPE" == "darwin"* ]]; then
            open "$LATEST_REPORT"
        elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
            xdg-open "$LATEST_REPORT" 2>/dev/null
        fi
    fi
else
    echo "=========================================="
    echo "❌ Performance tests failed!"
    echo "=========================================="
    echo ""
    echo "Common issues:"
    echo "  - Application not running"
    echo "  - Authentication required (OAuth2)"
    echo "  - Network connectivity issues"
    echo "  - Resource limits exceeded"
fi

echo ""
exit $EXIT_CODE
