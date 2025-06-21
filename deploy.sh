#!/bin/bash

# Weather API Kubernetes Deployment Script
# This script automates the entire deployment process

set -e  # Exit on any error

echo "🚀 Starting Weather API Kubernetes Deployment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Step 1: Check prerequisites
print_step "Checking prerequisites..."

# Check if kubectl is installed
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed. Please install kubectl first."
    exit 1
fi

# Check if Docker is installed and running
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

if ! docker info &> /dev/null; then
    print_error "Docker daemon is not running. Please start Docker."
    exit 1
fi

# Check if Kubernetes cluster is accessible
if ! kubectl cluster-info &> /dev/null; then
    print_error "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
    exit 1
fi

print_status "All prerequisites are met!"

# Step 2: Build the Docker image
print_step "Building Docker image..."
docker build -t weather-api:latest . || {
    print_error "Failed to build Docker image"
    exit 1
}
print_status "Docker image built successfully!"

# Step 3: Load image to Kubernetes (for local development)
if kubectl config current-context | grep -q "docker-desktop\|minikube\|kind"; then
    print_step "Loading Docker image to Kubernetes..."
    # For Docker Desktop, the image is already available
    # For minikube, we would need: minikube image load weather-api:latest
    # For kind, we would need: kind load docker-image weather-api:latest
    
    current_context=$(kubectl config current-context)
    if [[ "$current_context" == *"minikube"* ]]; then
        minikube image load weather-api:latest
    elif [[ "$current_context" == *"kind"* ]]; then
        kind load docker-image weather-api:latest
    fi
    print_status "Image loaded to Kubernetes!"
fi

# Step 4: Deploy to Kubernetes
print_step "Deploying to Kubernetes..."

# Apply all manifests using kustomize
kubectl apply -k k8s/ || {
    print_error "Failed to deploy to Kubernetes"
    exit 1
}

print_status "Kubernetes resources created successfully!"

# Step 5: Wait for deployment to be ready
print_step "Waiting for deployment to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/weather-api -n weather-api || {
    print_error "Deployment failed to become ready within 5 minutes"
    exit 1
}

print_status "Deployment is ready!"

# Step 6: Display deployment status
print_step "Deployment Status:"
echo ""
kubectl get all -n weather-api

# Step 7: Display access information
print_step "Access Information:"
echo ""
print_status "Application is deployed successfully!"
print_status "Namespace: weather-api"
print_status "Service: weather-api-service"
print_status "Port: 80 (maps to container port 8081)"

# Get service details
SERVICE_TYPE=$(kubectl get service weather-api-service -n weather-api -o jsonpath='{.spec.type}')

if [ "$SERVICE_TYPE" = "NodePort" ]; then
    NODE_PORT=$(kubectl get service weather-api-service -n weather-api -o jsonpath='{.spec.ports[0].nodePort}')
    print_status "Access URL: http://localhost:$NODE_PORT"
elif [ "$SERVICE_TYPE" = "LoadBalancer" ]; then
    print_status "Waiting for LoadBalancer IP..."
    kubectl get service weather-api-service -n weather-api --watch
else
    print_status "To access the application locally, run:"
    print_status "kubectl port-forward -n weather-api service/weather-api-service 8080:80"
    print_status "Then access: http://localhost:8080"
fi

print_status "Health check endpoint: /actuator/health"
print_status "Metrics endpoint: /actuator/metrics"

echo ""
print_status "🎉 Deployment completed successfully!"
print_warning "Note: For production deployment, update the image tag and push to a container registry." 