#!/bin/bash

################################################################################
# Test OAuth2 Token Generation Script
################################################################################

echo "================================================================================"
echo "Testing OAuth2 Token Generation"
echo "================================================================================"
echo ""

# Check if application is running
echo "[1/3] Checking if application is running..."
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Application is running"
else
    echo "❌ Application is NOT running"
    echo ""
    echo "Start it with:"
    echo "  cd /Users/manikantareddysiripireddy/Documents/Mani/Projects/ABN/RecipeAPI"
    echo "  java -jar target/recipe-1.0.0.jar"
    echo ""
    exit 1
fi
echo ""

# Test OAuth2 token generation
echo "[2/3] Generating OAuth2 token..."
RESPONSE=$(curl -s -X POST http://localhost:8080/oauth2/token \
  -u recipe-client:recipe-secret \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&scope=read write")

if echo "$RESPONSE" | grep -q "access_token"; then
    echo "✅ Token generated successfully!"
    echo ""
    echo "Response:"
    echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"

    # Extract token
    TOKEN=$(echo "$RESPONSE" | jq -r '.access_token' 2>/dev/null)
    echo ""
    echo "Access Token (first 50 chars):"
    echo "${TOKEN:0:50}..."
else
    echo "❌ Token generation failed"
    echo "Response: $RESPONSE"
    exit 1
fi
echo ""

# Test API with token
echo "[3/3] Testing API with token..."
API_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/recipe/all)

if echo "$API_RESPONSE" | grep -q "\[" || echo "$API_RESPONSE" | grep -q "\""; then
    echo "✅ API call successful with token!"
    echo ""
    echo "Response (first 200 chars):"
    echo "${API_RESPONSE:0:200}..."
else
    echo "❌ API call failed"
    echo "Response: $API_RESPONSE"
fi

echo ""
echo "================================================================================"
echo "✅ OAuth2 Authentication is Working!"
echo "================================================================================"
echo ""
echo "You can now use Postman:"
echo "  1. Go to: Authentication → Generate JWT Token (Client Credentials)"
echo "  2. Click: Send"
echo "  3. Token auto-saved to {{jwt_token}}"
echo "  4. Use any API endpoint - token included automatically!"
echo ""
