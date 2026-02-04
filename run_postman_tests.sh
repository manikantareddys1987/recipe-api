#!/bin/bash

echo "=========================================="
echo "Recipe API - Postman Collection Runner"
echo "=========================================="
echo ""

# Check if newman is installed
if ! command -v newman &> /dev/null; then
    echo "❌ Newman is not installed!"
    echo ""
    echo "Newman is required to run Postman collections from command line."
    echo ""
    echo "To install Newman:"
    echo "  npm install -g newman"
    echo ""
    echo "Or using Homebrew on macOS:"
    echo "  brew install newman"
    echo ""
    exit 1
fi

BASE_URL="http://localhost:8080"
POSTMAN_COLLECTION="postman/postman_collection.json"
POSTMAN_ENV="postman/postman_environment.json"

if [ ! -f "$POSTMAN_COLLECTION" ]; then
    echo "❌ Error: $POSTMAN_COLLECTION not found"
    exit 1
fi

if [ ! -f "$POSTMAN_ENV" ]; then
    echo "❌ Error: $POSTMAN_ENV not found"
    exit 1
fi

echo "✓ Newman found: $(newman --version)"
echo "✓ Collection: $POSTMAN_COLLECTION"
echo "✓ Environment: $POSTMAN_ENV"
echo "✓ Target URL: $BASE_URL"
echo ""
echo "Running Postman collection tests..."
echo ""

newman run "$POSTMAN_COLLECTION" \
    -e "$POSTMAN_ENV" \
    --reporters cli,json \
    --reporter-json-export postman_test_results.json \
    --insecure \
    -n 1 \
    --delay-request 100 \
    --timeout-request 10000

EXIT_CODE=$?

echo ""
if [ $EXIT_CODE -eq 0 ]; then
    echo "=========================================="
    echo "✅ Test execution completed successfully!"
    echo "=========================================="
    echo ""
    echo "Results saved to: postman_test_results.json"
else
    echo "=========================================="
    echo "❌ Test execution failed!"
    echo "=========================================="
    echo ""
    echo "Check the output above for errors."
    echo "Common issues:"
    echo "  - Application not running (start with: docker-compose up -d)"
    echo "  - Wrong credentials in environment file"
    echo "  - Network connectivity issues"
fi

echo ""
exit $EXIT_CODE

