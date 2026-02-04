#!/bin/bash

################################################################################
# Rebuild Docker with Fixed Code - Complete Solution
################################################################################

set -e

echo "========================================================================"
echo "🔧 Rebuilding Recipe API Docker with Fixed Code"
echo "========================================================================"
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

cd /Users/manikantareddysiripireddy/Documents/Mani/Projects/ABN/RecipeAPI

# Step 1: Stop containers
echo -e "${BLUE}[1/6]${NC} Stopping existing containers..."
docker-compose down
echo -e "${GREEN}✓ Containers stopped${NC}"
echo ""

# Step 2: Clean build
echo -e "${BLUE}[2/6]${NC} Building application with latest code..."
./mvnw clean package -DskipTests > build.log 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Application built successfully${NC}"
else
    echo -e "${RED}✗ Build failed${NC}"
    echo "Check build.log for errors"
    exit 1
fi
echo ""

# Step 3: Rebuild Docker images
echo -e "${BLUE}[3/6]${NC} Rebuilding Docker images..."
docker-compose build --no-cache recipe-api > docker-build.log 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Docker image built${NC}"
else
    echo -e "${RED}✗ Docker build failed${NC}"
    echo "Check docker-build.log for errors"
    exit 1
fi
echo ""

# Step 4: Start all services
echo -e "${BLUE}[4/6]${NC} Starting all services..."
docker-compose up -d
echo -e "${GREEN}✓ Services started${NC}"
echo ""

# Step 5: Wait for services
echo -e "${BLUE}[5/6]${NC} Waiting for services to be ready (45 seconds)..."
for i in {1..45}; do
    echo -n "."
    sleep 1
done
echo ""
echo ""

# Step 6: Test services
echo -e "${BLUE}[6/6]${NC} Testing services..."
echo ""

# Test health
echo -n "  Testing health endpoint... "
HEALTH=$(curl -s http://localhost:8080/actuator/health)
if echo "$HEALTH" | grep -q "UP"; then
    echo -e "${GREEN}✓ OK${NC}"
else
    echo -e "${RED}✗ FAILED${NC}"
    echo "Response: $HEALTH"
fi

# Test OAuth2 token
echo -n "  Testing OAuth2 token generation... "
TOKEN_RESPONSE=$(curl -s -X POST http://localhost:8080/oauth2/token \
  -u recipe-client:recipe-secret \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&scope=read write")

if echo "$TOKEN_RESPONSE" | grep -q "access_token"; then
    echo -e "${GREEN}✓ OK${NC}"
    TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)
else
    echo -e "${RED}✗ FAILED${NC}"
    echo "Response: $TOKEN_RESPONSE"
    TOKEN=""
fi

# Test API with token
if [ -n "$TOKEN" ]; then
    echo -n "  Testing GET /api/v1/recipe/all... "
    API_RESPONSE=$(curl -s -w "%{http_code}" -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/recipe/all)
    HTTP_CODE="${API_RESPONSE: -3}"

    if [ "$HTTP_CODE" = "200" ]; then
        echo -e "${GREEN}✓ OK (200)${NC}"
    else
        echo -e "${RED}✗ FAILED ($HTTP_CODE)${NC}"
        echo "Response: ${API_RESPONSE:0:-3}"
    fi
fi

echo ""
echo "========================================================================"
echo -e "${GREEN}✅ Docker Rebuild Complete!${NC}"
echo "========================================================================"
echo ""
echo -e "${GREEN}Access Points:${NC}"
echo "  🚀 API:        http://localhost:8080/swagger-ui.html"
echo "  ❤️  Health:    http://localhost:8080/actuator/health"
echo "  🔑 OAuth2:     POST http://localhost:8080/oauth2/token"
echo "  📊 Grafana:    http://localhost:3000 (admin/admin)"
echo "  🗄️  pgAdmin:    http://localhost:5050 (admin@recipeapi.com/admin)"
echo "  📈 Prometheus: http://localhost:9090"
echo ""
echo -e "${YELLOW}OAuth2 Credentials:${NC}"
echo "  Client ID:     recipe-client"
echo "  Client Secret: recipe-secret"
echo ""
echo -e "${YELLOW}Database (PostgreSQL):${NC}"
echo "  Host:     localhost:5432"
echo "  Database: recipedb"
echo "  Username: recipeuser"
echo "  Password: recipepass123"
echo ""
echo -e "${GREEN}Test in Postman:${NC}"
echo "  1. Authentication → Generate JWT Token → Send"
echo "  2. Recipe - Read → Get All Recipes → Send"
echo "  3. Should work perfectly! ✅"
echo ""
echo "To view logs: docker logs recipe-api -f"
echo "To stop: docker-compose down"
echo ""
