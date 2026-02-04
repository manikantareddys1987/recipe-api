#!/bin/bash

# Quick Database Access Script for Recipe API
# This script provides easy commands to check database content

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}=================================================================================${NC}"
echo -e "${BLUE}Recipe API - Database Quick Access${NC}"
echo -e "${BLUE}=================================================================================${NC}"
echo ""

# Function to execute SQL query
run_query() {
    docker exec recipe-postgres psql -U recipeuser -d recipedb -c "$1"
}

# Check if PostgreSQL container is running
if ! docker ps | grep -q recipe-postgres; then
    echo -e "${YELLOW}⚠️  PostgreSQL container is not running!${NC}"
    echo "Start it with: docker-compose up -d recipe-postgres"
    exit 1
fi

echo -e "${GREEN}✓ PostgreSQL container is running${NC}"
echo ""

# Show menu
echo "Select an option:"
echo "1. View all recipes"
echo "2. View all ingredients"
echo "3. View recipes with their ingredients"
echo "4. View vegetarian recipes only"
echo "5. Count total recipes"
echo "6. Search recipe by name"
echo "7. View recipes with specific ingredient"
echo "8. Run custom SQL query"
echo "9. Open psql interactive shell"
echo "10. Open pgAdmin in browser"
echo "0. Exit"
echo ""
read -p "Enter your choice [0-10]: " choice

case $choice in
    1)
        echo -e "\n${BLUE}All Recipes:${NC}"
        run_query "SELECT id, name, type, number_of_servings FROM recipes ORDER BY name;"
        ;;
    2)
        echo -e "\n${BLUE}All Ingredients:${NC}"
        run_query "SELECT * FROM ingredients ORDER BY ingredient;"
        ;;
    3)
        echo -e "\n${BLUE}Recipes with Ingredients:${NC}"
        run_query "
        SELECT r.name, r.type, r.number_of_servings,
               string_agg(i.ingredient, ', ' ORDER BY i.ingredient) as ingredients
        FROM recipes r
        LEFT JOIN recipe_ingredient ri ON r.id = ri.recipe_id
        LEFT JOIN ingredients i ON i.id = ri.ingredient_id
        GROUP BY r.id, r.name, r.type, r.number_of_servings
        ORDER BY r.name;"
        ;;
    4)
        echo -e "\n${BLUE}Vegetarian Recipes:${NC}"
        run_query "SELECT * FROM recipes WHERE type = 'VEGETARIAN' ORDER BY name;"
        ;;
    5)
        echo -e "\n${BLUE}Recipe Count:${NC}"
        run_query "
        SELECT
            COUNT(*) as total_recipes,
            COUNT(CASE WHEN type = 'VEGETARIAN' THEN 1 END) as vegetarian,
            COUNT(CASE WHEN type = 'NON_VEGETARIAN' THEN 1 END) as non_vegetarian
        FROM recipes;"
        ;;
    6)
        read -p "Enter recipe name (or part of it): " recipe_name
        echo -e "\n${BLUE}Search Results:${NC}"
        run_query "SELECT * FROM recipes WHERE name ILIKE '%${recipe_name}%';"
        ;;
    7)
        read -p "Enter ingredient name: " ingredient_name
        echo -e "\n${BLUE}Recipes with '${ingredient_name}':${NC}"
        run_query "
        SELECT DISTINCT r.*
        FROM recipes r
        JOIN recipe_ingredient ri ON r.id = ri.recipe_id
        JOIN ingredients i ON i.id = ri.ingredient_id
        WHERE i.ingredient ILIKE '%${ingredient_name}%';"
        ;;
    8)
        read -p "Enter your SQL query: " sql_query
        echo -e "\n${BLUE}Query Result:${NC}"
        run_query "${sql_query}"
        ;;
    9)
        echo -e "\n${BLUE}Opening PostgreSQL interactive shell...${NC}"
        echo "Type \\q to exit"
        echo ""
        docker exec -it recipe-postgres psql -U recipeuser -d recipedb
        ;;
    10)
        echo -e "\n${BLUE}Opening pgAdmin in browser...${NC}"
        echo "URL: http://localhost:5050"
        echo "Email: admin@recipeapi.com"
        echo "Password: admin"
        echo ""
        if command -v open &> /dev/null; then
            open http://localhost:5050
        else
            echo "Please open http://localhost:5050 in your browser"
        fi
        ;;
    0)
        echo "Goodbye!"
        exit 0
        ;;
    *)
        echo -e "${YELLOW}Invalid option!${NC}"
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}Done!${NC}"
echo ""
echo "Database Connection Details:"
echo "  Host: localhost"
echo "  Port: 5432"
echo "  Database: recipedb"
echo "  Username: recipeuser"
echo "  Password: recipepass123"
echo ""
echo "pgAdmin Web UI: http://localhost:5050"
echo ""
