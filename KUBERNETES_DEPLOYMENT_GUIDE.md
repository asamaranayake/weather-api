# 🚀 Weather API Kubernetes Deployment Guide

## 📋 Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Deployment Steps](#deployment-steps)
3. [YAML Configuration Files](#yaml-configuration-files)
4. [Network Flow](#network-flow)
5. [Environment Management](#environment-management)
6. [Troubleshooting](#troubleshooting)

---

## 🏗️ Architecture Overview

### High-Level Architecture Diagram
```
┌─────────────────────────────────────────────────────────────────┐
│                          EXTERNAL ACCESS                        │
│  Browser/Client → http://localhost/dev|qa|prod/weather-api     │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────────┐
│                    INGRESS CONTROLLER                           │
│  • Routes traffic based on URL paths                           │
│  • /dev/* → Dev Namespace                                      │
│  • /qa/*  → QA Namespace                                       │
│  • /prod/* → Prod Namespace                                    │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────────┐
│                      KUBERNETES CLUSTER                         │
│                                                                 │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐   │
│  │   DEV NAMESPACE │ │   QA NAMESPACE  │ │  PROD NAMESPACE │   │
│  │                 │ │                 │ │                 │   │
│  │ ┌─────────────┐ │ │ ┌─────────────┐ │ │ ┌─────────────┐ │   │
│  │ │   INGRESS   │ │ │ │   INGRESS   │ │ │ │   INGRESS   │ │   │
│  │ │ localhost/  │ │ │ │ localhost/  │ │ │ │ localhost/  │ │   │
│  │ │    dev/*    │ │ │ │    qa/*     │ │ │ │   prod/*    │ │   │
│  │ └─────────────┘ │ │ └─────────────┘ │ │ └─────────────┘ │   │
│  │        │        │ │        │        │ │        │        │   │
│  │ ┌─────────────┐ │ │ ┌─────────────┐ │ │ ┌─────────────┐ │   │
│  │ │   SERVICE   │ │ │ │   SERVICE   │ │ │ │   SERVICE   │ │   │
│  │ │   Port 80   │ │ │ │   Port 80   │ │ │ │   Port 80   │ │   │
│  │ │→ Target 8081│ │ │ │→ Target 8081│ │ │ │→ Target 8081│ │   │
│  │ └─────────────┘ │ │ └─────────────┘ │ │ └─────────────┘ │   │
│  │        │        │ │        │        │ │        │        │   │
│  │ ┌─────────────┐ │ │ ┌─────────────┐ │ │ ┌─────────────┐ │   │
│  │ │ DEPLOYMENT  │ │ │ │ DEPLOYMENT  │ │ │ │ DEPLOYMENT  │ │   │
│  │ │  1 Replica  │ │ │ │  2 Replicas │ │ │ │  3 Replicas │ │   │
│  │ └─────────────┘ │ │ └─────────────┘ │ │ └─────────────┘ │   │
│  │        │        │ │        │        │ │        │        │   │
│  │ ┌─────────────┐ │ │ ┌─────────────┐ │ │ ┌─────────────┐ │   │
│  │ │    PODS     │ │ │ │    PODS     │ │ │ │    PODS     │ │   │
│  │ │weather-api:1│ │ │ │weather-api:2│ │ │ │weather-api:3│ │   │
│  │ │Port 8081    │ │ │ │Port 8081    │ │ │ │Port 8081    │ │   │
│  │ └─────────────┘ │ │ └─────────────┘ │ │ └─────────────┘ │   │
│  │        │        │ │        │        │ │        │        │   │
│  │ ┌─────────────┐ │ │ ┌─────────────┐ │ │ ┌─────────────┐ │   │
│  │ │ CONFIGMAP   │ │ │ │ CONFIGMAP   │ │ │ │ CONFIGMAP   │ │   │
│  │ │ Dev Config  │ │ │ │ QA Config   │ │ │ │ Prod Config │ │   │
│  │ └─────────────┘ │ │ └─────────────┘ │ │ └─────────────┘ │   │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### Key Architectural Components

1. **Namespaces**: Logical isolation between environments
2. **Ingress**: External traffic routing and SSL termination
3. **Services**: Internal load balancing and service discovery
4. **Deployments**: Pod lifecycle management and scaling
5. **ConfigMaps**: Configuration management
6. **Pods**: Application runtime containers

---

## 🚀 Deployment Steps

### Prerequisites
```bash
# 1. Ensure Docker is running
docker --version

# 2. Ensure Kubernetes cluster is running
kubectl cluster-info

# 3. Build the application Docker image
docker build -t weather-api:latest .

# 4. Verify image exists
docker images | grep weather-api
```

### Step-by-Step Deployment

#### Phase 1: Foundation Setup
```bash
# Step 1: Create Namespaces (Isolation Layer)
kubectl apply -f k8s/namespace.yaml
```
**What happens**: Creates three isolated environments in Kubernetes

#### Phase 2: Configuration Management
```bash
# Step 2: Deploy ConfigMaps (Configuration Layer)
kubectl apply -f k8s/configmap.yaml
```
**What happens**: Stores environment-specific configuration data

#### Phase 3: Application Deployment
```bash
# Step 3: Deploy Applications (Compute Layer)
kubectl apply -f k8s/deployment.yaml
```
**What happens**: Creates and manages application pods

#### Phase 4: Internal Networking
```bash
# Step 4: Create Services (Networking Layer)
kubectl apply -f k8s/service.yaml
```
**What happens**: Enables internal communication and load balancing

#### Phase 5: External Access
```bash
# Step 5: Configure Ingress (Access Layer)
kubectl apply -f k8s/ingress.yaml
```
**What happens**: Enables external access to applications

### Verification Commands
```bash
# Check all resources
kubectl get all -n weather-api-dev
kubectl get all -n weather-api-qa
kubectl get all -n weather-api-prod

# Check ingress
kubectl get ingress -A

# Test endpoints
curl http://localhost/dev/weather-api
curl http://localhost/qa/weather-api
curl http://localhost/prod/weather-api
```

---

## 📄 YAML Configuration Files

### 1. namespace.yaml
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: weather-api-dev
  labels:
    name: weather-api-dev
    environment: dev
```

**Purpose**: Creates isolated environments
**Architecture Role**: Provides logical separation between dev/qa/prod
**Key Features**:
- Isolates resources between environments
- Enables RBAC (Role-Based Access Control)
- Provides resource quotas and limits
- Prevents naming conflicts

### 2. configmap.yaml
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: weather-api-config
  namespace: weather-api-dev
data:
  server.port: "8081"
  logging.level.root: "DEBUG"
  spring.profiles.active: "dev"
```

**Purpose**: Stores configuration data
**Architecture Role**: Centralized configuration management
**Key Features**:
- Environment-specific configurations
- Decouples configuration from code
- Hot-reload capability
- Version control for configurations

**Environment Differences**:
- **Dev**: Debug logging, relaxed security
- **QA**: Info logging, staging configurations
- **Prod**: Warn logging, production optimizations

### 3. deployment.yaml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: weather-api-dev
  namespace: weather-api-dev
spec:
  replicas: 1
  selector:
    matchLabels:
      app: weather-api
      environment: dev
  template:
    spec:
      containers:
      - name: weather-api
        image: weather-api:latest
        ports:
        - containerPort: 8081
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
```

**Purpose**: Manages application pods
**Architecture Role**: Ensures desired state of applications
**Key Features**:
- **Replica Management**: Ensures specified number of pods
- **Health Checks**: Monitors application health
- **Resource Limits**: Prevents resource exhaustion
- **Rolling Updates**: Zero-downtime deployments

**Environment Scaling**:
- **Dev**: 1 replica (development)
- **QA**: 2 replicas (load testing)
- **Prod**: 3 replicas (high availability)

**Health Checks Explained**:
- **Liveness Probe**: Restarts pod if application is unhealthy
- **Readiness Probe**: Removes pod from service if not ready
- **Startup Probe**: Gives application time to start

### 4. service.yaml
```yaml
apiVersion: v1
kind: Service
metadata:
  name: weather-api-service
  namespace: weather-api-dev
spec:
  selector:
    app: weather-api
    environment: dev
  ports:
  - name: http
    port: 80
    targetPort: 8081
    protocol: TCP
  type: ClusterIP
```

**Purpose**: Provides stable network endpoint
**Architecture Role**: Internal load balancer and service discovery
**Key Features**:
- **Load Balancing**: Distributes traffic across pods
- **Service Discovery**: Stable DNS name for pods
- **Port Mapping**: External port 80 → Internal port 8081
- **Health-based Routing**: Only routes to healthy pods

**Service Types**:
- **ClusterIP**: Internal access only (our choice)
- **NodePort**: External access via node ports
- **LoadBalancer**: Cloud provider load balancer

### 5. ingress.yaml
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: weather-api-ingress-dev
  namespace: weather-api-dev
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /$2
    nginx.ingress.kubernetes.io/use-regex: "true"
spec:
  rules:
  - host: localhost
    http:
      paths:
      - path: /dev(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: weather-api-service
            port:
              number: 80
```

**Purpose**: Manages external access
**Architecture Role**: Reverse proxy and traffic router
**Key Features**:
- **Path-based Routing**: Routes by URL path
- **URL Rewriting**: Strips environment prefix
- **SSL Termination**: Handles HTTPS (if configured)
- **Rate Limiting**: Controls traffic flow

**Routing Logic**:
```
localhost/dev/weather-api → weather-api-dev namespace
localhost/qa/weather-api  → weather-api-qa namespace
localhost/prod/weather-api → weather-api-prod namespace
```

**Annotations Explained**:
- `rewrite-target: /$2`: Removes `/dev` from URL before forwarding
- `use-regex: "true"`: Enables regex in path matching
- `ssl-redirect: "false"`: Allows HTTP access

---

## 🌐 Network Flow

### Request Flow Diagram
```
1. Client Request
   ↓
   http://localhost/dev/weather-api
   
2. Ingress Controller
   ↓
   Matches: host=localhost, path=/dev/*
   Rewrites: /weather-api (removes /dev)
   Routes to: weather-api-service in weather-api-dev namespace
   
3. Service (weather-api-service)
   ↓
   Load balances to healthy pods
   Port mapping: 80 → 8081
   
4. Pod (weather-api container)
   ↓
   Spring Boot application on port 8081
   Processes request
   
5. Response
   ↓
   Returns JSON response through same path
```

### Network Policies
```yaml
# Example network policy (not implemented)
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: weather-api-network-policy
spec:
  podSelector:
    matchLabels:
      app: weather-api
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
  egress:
  - to: []
    ports:
    - protocol: TCP
      port: 443  # HTTPS for external APIs
```

---

## 🔧 Environment Management

### Environment Characteristics

| Environment | Purpose | Replicas | Resources | Logging | Access |
|-------------|---------|----------|-----------|---------|--------|
| **Dev** | Development & Testing | 1 | 256Mi-512Mi | DEBUG | localhost/dev/* |
| **QA** | Quality Assurance | 2 | 256Mi-512Mi | INFO | localhost/qa/* |
| **Prod** | Production | 3 | 512Mi-1Gi | WARN | localhost/prod/* |

### Configuration Differences

#### Development Environment
```yaml
# Higher logging verbosity
logging.level.root: "DEBUG"
logging.level.com.example: "DEBUG"

# Development tools enabled
spring.devtools.restart.enabled: true
spring.devtools.livereload.enabled: true

# Relaxed CORS
spring.web.cors.allowed-origins: "*"
```

#### QA Environment
```yaml
# Moderate logging
logging.level.root: "INFO"
logging.level.com.example: "INFO"

# Staging profile
spring.profiles.active: "staging"

# Moderate CORS
spring.web.cors.allowed-origins: "http://qa-domain.com"
```

#### Production Environment
```yaml
# Minimal logging
logging.level.root: "WARN"
logging.level.com.example: "INFO"

# Production optimizations
spring.jpa.show-sql: false
spring.devtools.restart.enabled: false

# Strict CORS
spring.web.cors.allowed-origins: "https://production-domain.com"
```

### Scaling Strategies

#### Horizontal Pod Autoscaler (HPA)
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: weather-api-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: weather-api-prod
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

---

## 🔍 Troubleshooting

### Common Issues and Solutions

#### 1. Pod Not Starting
```bash
# Check pod status
kubectl get pods -n weather-api-dev

# Check pod logs
kubectl logs -n weather-api-dev deployment/weather-api-dev

# Check pod events
kubectl describe pod -n weather-api-dev <pod-name>
```

**Common Causes**:
- Image pull errors
- Resource constraints
- Configuration errors
- Health check failures

#### 2. Service Not accessible
```bash
# Check service endpoints
kubectl get endpoints -n weather-api-dev

# Test service directly
kubectl port-forward -n weather-api-dev service/weather-api-service 8080:80
curl http://localhost:8080/weather-api
```

**Common Causes**:
- Selector mismatch
- Pod not ready
- Port configuration errors

#### 3. Ingress Not Working
```bash
# Check ingress status
kubectl get ingress -A

# Check ingress controller logs
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller
```

**Common Causes**:
- Ingress controller not installed
- DNS resolution issues
- Path configuration errors

### Debugging Commands

#### Resource Status
```bash
# Check all resources in namespace
kubectl get all -n weather-api-dev

# Check resource usage
kubectl top pods -n weather-api-dev
kubectl top nodes
```

#### Logs and Events
```bash
# Application logs
kubectl logs -f -n weather-api-dev deployment/weather-api-dev

# System events
kubectl get events -n weather-api-dev --sort-by='.lastTimestamp'

# Cluster events
kubectl get events --all-namespaces --sort-by='.lastTimestamp'
```

#### Network Debugging
```bash
# Test pod connectivity
kubectl exec -it -n weather-api-dev <pod-name> -- curl localhost:8081/actuator/health

# Test service connectivity
kubectl run debug --image=curlimages/curl -it --rm -- curl weather-api-service.weather-api-dev.svc.cluster.local/weather-api
```

### Performance Monitoring

#### Metrics Collection
```bash
# Enable metrics server
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# View resource usage
kubectl top pods -n weather-api-dev
kubectl top nodes
```

#### Health Check Endpoints
```bash
# Application health
curl http://localhost/dev/actuator/health

# Detailed health info
curl http://localhost/dev/actuator/health/readiness
curl http://localhost/dev/actuator/health/liveness

# Metrics
curl http://localhost/dev/actuator/metrics
```

---

## 🎯 Best Practices

### Security
- Use specific image tags, not `latest`
- Implement network policies
- Use secrets for sensitive data
- Enable RBAC
- Regular security scans

### Performance
- Set appropriate resource requests/limits
- Use horizontal pod autoscaling
- Implement caching strategies
- Monitor and optimize database queries

### Reliability
- Implement proper health checks
- Use multiple replicas for high availability
- Plan for graceful shutdowns
- Implement circuit breakers

### Monitoring
- Centralized logging
- Application metrics
- Infrastructure monitoring
- Alerting and notifications

---

## 📚 Additional Resources

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [NGINX Ingress Controller](https://kubernetes.github.io/ingress-nginx/)
- [Kubernetes Best Practices](https://kubernetes.io/docs/concepts/configuration/overview/)

---

**Last Updated**: $(date)
**Version**: 1.0
**Author**: Weather API Deployment Team 