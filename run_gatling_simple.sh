#!/bin/bash

# Simple Gatling test runner - no compilation
# Assumes project is already compiled

echo "=========================================="
echo "Recipe API - Gatling Tests (No Build)"
echo "=========================================="
echo ""

# Check if application is running
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✓ Application is running"
else
    echo "⚠️  Application not running at http://localhost:8080"
    echo ""
fi

echo ""
echo "Select test:"
echo "1) Quick Load Test"
echo "2) Full Performance Test"
echo "3) Stress Test"
echo ""
read -p "Choice [1-3]: " choice

case $choice in
    1) SIMULATION="com.recipe.gatling.QuickLoadTest" ;;
    2) SIMULATION="com.recipe.gatling.RecipeApiSimulation" ;;
    3) SIMULATION="com.recipe.gatling.StressTest" ;;
    *) echo "Invalid choice"; exit 1 ;;
esac

echo ""
echo "Running: $SIMULATION"
echo ""

./mvnw gatling:test -Dgatling.simulationClass=$SIMULATION

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Test completed!"
    echo ""
    REPORT=$(find target/gatling -name "index.html" -type f | sort -r | head -1)
    if [ -n "$REPORT" ]; then
        echo "Report: $REPORT"
        [[ "$OSTYPE" == "darwin"* ]] && open "$REPORT"
    fi
else
    echo ""
    echo "❌ Test failed!"
fi
