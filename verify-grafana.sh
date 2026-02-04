#!/bin/bash

echo "=================================================="
echo "Grafana Metrics Verification Script"
echo "=================================================="
echo ""

# Check containers
echo "1. Checking Container Status..."
if docker ps | grep -q "recipe-api.*healthy"; then
    echo "   ✅ recipe-api is running and healthy"
else
    echo "   ❌ recipe-api is not healthy"
fi

if docker ps | grep -q "recipe-prometheus.*healthy"; then
    echo "   ✅ prometheus is running and healthy"
else
    echo "   ❌ prometheus is not healthy"
fi

if docker ps | grep -q "recipe-grafana.*healthy"; then
    echo "   ✅ grafana is running and healthy"
else
    echo "   ❌ grafana is not healthy"
fi
echo ""

# Check metrics endpoint
echo "2. Checking Application Metrics Endpoint..."
if curl -s http://localhost:8080/actuator/prometheus | grep -q "recipes_created_total"; then
    echo "   ✅ Metrics endpoint is working"
    echo "   Sample metrics:"
    curl -s http://localhost:8080/actuator/prometheus | grep -E "recipes_created|recipes_updated|recipes_deleted" | head -6
else
    echo "   ❌ Metrics endpoint not responding"
fi
echo ""

# Check Prometheus scraping
echo "3. Checking Prometheus Scraping..."
if curl -s http://localhost:9090/api/v1/targets | grep -q '"health":"up"'; then
    echo "   ✅ Prometheus is successfully scraping recipe-api"
else
    echo "   ❌ Prometheus is not scraping properly"
fi
echo ""

# Query Prometheus for data
echo "4. Querying Prometheus for Recipe Metrics..."
QUERY_RESULT=$(curl -s "http://localhost:9090/api/v1/query?query=recipes_created_total" | grep -o '"value":\[[^]]*\]')
if [ -n "$QUERY_RESULT" ]; then
    echo "   ✅ Prometheus has recipe data: $QUERY_RESULT"
else
    echo "   ⚠️  Prometheus doesn't have recipe data yet (generate traffic)"
fi
echo ""

# Check Grafana datasource
echo "5. Checking Grafana Datasource..."
DATASOURCE=$(curl -s -u admin:admin http://localhost:3000/api/datasources 2>/dev/null | grep -o '"name":"Prometheus"')
if [ -n "$DATASOURCE" ]; then
    echo "   ✅ Grafana datasource 'Prometheus' is configured"
    DS_UID=$(curl -s -u admin:admin http://localhost:3000/api/datasources 2>/dev/null | grep -o '"uid":"[^"]*"' | head -1 | cut -d'"' -f4)
    echo "   UID: $DS_UID"
else
    echo "   ❌ Grafana datasource not found"
fi
echo ""

# Network connectivity
echo "6. Checking Network Connectivity..."
if docker exec recipe-grafana ping -c 1 prometheus > /dev/null 2>&1; then
    echo "   ✅ Grafana can reach Prometheus"
else
    echo "   ❌ Grafana cannot reach Prometheus"
fi
echo ""

echo "=================================================="
echo "Verification Summary"
echo "=================================================="
echo ""

# Count successes
SUCCESS_COUNT=0
if docker ps | grep -q "recipe-api.*healthy"; then ((SUCCESS_COUNT++)); fi
if docker ps | grep -q "recipe-prometheus.*healthy"; then ((SUCCESS_COUNT++)); fi
if docker ps | grep -q "recipe-grafana.*healthy"; then ((SUCCESS_COUNT++)); fi
if curl -s http://localhost:8080/actuator/prometheus | grep -q "recipes_created_total"; then ((SUCCESS_COUNT++)); fi
if curl -s http://localhost:9090/api/v1/targets | grep -q '"health":"up"'; then ((SUCCESS_COUNT++)); fi
if docker exec recipe-grafana ping -c 1 prometheus > /dev/null 2>&1; then ((SUCCESS_COUNT++)); fi

echo "Passed: $SUCCESS_COUNT/6 checks"
echo ""

if [ $SUCCESS_COUNT -eq 6 ]; then
    echo "✅ All checks passed!"
    echo ""
    echo "Next Steps:"
    echo "1. Open Grafana: http://localhost:3000"
    echo "2. Login with: admin/admin"
    echo "3. Go to Dashboard → Recipe API Application Metrics"
    echo "4. If showing 'No Data':"
    echo "   - Click time range → Select 'Last 6 hours'"
    echo "   - Generate traffic: ./run_gatling_simple.sh"
    echo "   - Refresh dashboard"
else
    echo "⚠️  Some checks failed. See details above."
    echo ""
    echo "Quick Fix:"
    echo "docker-compose restart grafana"
    echo "sleep 10"
    echo "./verify-grafana.sh"
fi

echo ""
echo "=================================================="
