#!/bin/bash

# Recipe API Docker Startup Script
# This script provides easy commands to manage Docker containers

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker Desktop."
        exit 1
    fi
    print_success "Docker is running"
}

# Function to start all services
start_services() {
    print_info "Starting all Recipe API services..."
    docker-compose down -v
    docker-compose up -d --build

    print_info "Waiting for services to be healthy..."
    sleep 10

    print_info "Checking service status..."
    docker-compose ps

    print_success "All services started!"
    print_info "Access points:"
    echo "  - API Swagger: http://localhost:8080/swagger-ui.html"
    echo "  - API Health: http://localhost:8080/actuator/health"
    echo "  - Prometheus: http://localhost:9090"
    echo "  - Grafana: http://localhost:3000 (admin/admin)"
    echo "  - pgAdmin: http://localhost:5050 (admin@recipeapi.com/admin)"
}

# Function to stop all services
stop_services() {
    print_info "Stopping all Recipe API services..."
    docker-compose down
    print_success "All services stopped!"
}

# Function to view logs
view_logs() {
    print_info "Viewing logs for all services (Ctrl+C to exit)..."
    docker-compose logs -f
}

# Function to clean everything
clean_all() {
    print_warning "This will remove all containers, volumes, and data!"
    read -p "Are you sure? (yes/no): " -r
    if [[ $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
        print_info "Cleaning all Docker resources..."
        docker-compose down -v
        docker ps -a | grep recipe | awk '{print $1}' | xargs docker rm -f 2>/dev/null || true
        docker volume ls | grep recipe | awk '{print $2}' | xargs docker volume rm 2>/dev/null || true
        docker system prune -f
        print_success "Clean complete!"
    else
        print_info "Clean cancelled"
    fi
}

# Function to check service health
check_health() {
    print_info "Checking service health..."

    echo ""
    print_info "Container Status:"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

    echo ""
    print_info "API Health:"
    curl -s http://localhost:8080/actuator/health | jq '.' 2>/dev/null || echo "API not responding"

    echo ""
    print_info "Prometheus Health:"
    curl -s http://localhost:9090/-/healthy 2>/dev/null && echo "Healthy" || echo "Not responding"

    echo ""
    print_info "Grafana Health:"
    curl -s http://localhost:3000/api/health | jq '.' 2>/dev/null || echo "Not responding"

    echo ""
    print_info "Database Health:"
    docker exec recipe-postgres pg_isready -U recipeuser 2>/dev/null || echo "Database not responding"
}

# Function to rebuild API only
rebuild_api() {
    print_info "Rebuilding Recipe API..."
    ./mvnw clean package -DskipTests
    docker-compose up -d --build recipe-api
    print_success "API rebuilt and restarted!"
}

# Function to show menu
show_menu() {
    echo ""
    echo "======================================"
    echo "  Recipe API Docker Management"
    echo "======================================"
    echo "1. Start all services"
    echo "2. Stop all services"
    echo "3. View logs"
    echo "4. Check health"
    echo "5. Rebuild API only"
    echo "6. Clean all (removes data)"
    echo "7. Exit"
    echo "======================================"
    read -p "Select an option [1-7]: " choice

    case $choice in
        1) start_services ;;
        2) stop_services ;;
        3) view_logs ;;
        4) check_health ;;
        5) rebuild_api ;;
        6) clean_all ;;
        7) exit 0 ;;
        *) print_error "Invalid option" ;;
    esac

    show_menu
}

# Main execution
check_docker

# If no arguments, show menu
if [ $# -eq 0 ]; then
    show_menu
else
    # Handle command line arguments
    case "$1" in
        start) start_services ;;
        stop) stop_services ;;
        logs) view_logs ;;
        health) check_health ;;
        rebuild) rebuild_api ;;
        clean) clean_all ;;
        *)
            print_error "Unknown command: $1"
            echo "Usage: $0 {start|stop|logs|health|rebuild|clean}"
            exit 1
            ;;
    esac
fi
