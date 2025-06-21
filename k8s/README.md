# Kubernetes Deployment Guide for Weather API

## Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   External      │    │   Ingress       │    │   Service       │
│   Traffic       │───▶│   Controller    │───▶│   (ClusterIP)   │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                                                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Deployment                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │    Pod 1    │  │    Pod 2    │  │    Pod 3    │             │
│  │             │  │             │  │             │             │
│  │ Weather-API │  │ Weather-API │  │ Weather-API │             │
│  │ Container   │  │ Container   │  │ Container   │             │
│  │             │  │             │  │             │             │
│  └─────────────┘  └─────────────┘  └─────────────┘             │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
                    ┌─────────────────┐
                    │   ConfigMap     │
                    │ (Configuration) │
                    └─────────────────┘
```

## File-by-File Explanation

### 1. `namespace.yaml` - Logical Isolation

**Purpose**: Creates a logical boundary for our application resources within the Kubernetes cluster.

**What Happens Architecturally**:
- Creates an isolated environment within the cluster
- Provides resource scoping and access control
- Enables resource quotas and network policies
- Separates our application from other applications

**Detailed Tag Explanations**:
```yaml
apiVersion: v1              # Kubernetes API version for core resources
kind: Namespace             # Resource type - creates a namespace
metadata:                   # Resource metadata
  name: weather-api         # Unique identifier for the namespace
  labels:                   # Key-value pairs for organization
    name: weather-api       # Label for identification
    environment: development # Label indicating deployment environment
```

**Student Teaching Points**:
- Namespaces are like folders in a file system
- Default namespace exists but it's better to create custom ones
- Resources in different namespaces are isolated
- DNS names include namespace: `service-name.namespace-name.svc.cluster.local`

---

### 2. `configmap.yaml` - Configuration Management

**Purpose**: Stores configuration data that can be consumed by pods without rebuilding container images.

**What Happens Architecturally**:
- Configuration is stored in etcd (Kubernetes' data store)
- Pods can mount ConfigMaps as environment variables or files
- Enables configuration changes without image rebuilds
- Promotes 12-factor app principles (config separation)

**Detailed Tag Explanations**:
```yaml
apiVersion: v1                    # Core API version
kind: ConfigMap                   # Resource type for configuration storage
metadata:
  name: weather-api-config        # ConfigMap identifier
  namespace: weather-api          # Must match target namespace
data:                            # Key-value configuration data
  server.port: "8081"            # Application port configuration
  management.endpoints.web.exposure.include: "health,info,metrics"  # Spring Boot actuator endpoints
  logging.level.root: "INFO"      # Root logging level
  logging.level.com.example: "DEBUG"  # Package-specific logging
```

**Student Teaching Points**:
- ConfigMaps are for non-confidential data only
- Data values must be strings in YAML
- Can be mounted as files or environment variables
- Changes require pod restart to take effect
- Use Secrets for sensitive data (passwords, tokens)

---

### 3. `deployment.yaml` - Application Lifecycle Management

**Purpose**: Manages the desired state of application pods, handles rolling updates, scaling, and self-healing.

**What Happens Architecturally**:
- Creates and manages ReplicaSet
- ReplicaSet creates and manages Pods
- Controller continuously monitors and maintains desired state
- Enables rolling updates and rollbacks
- Provides self-healing capabilities

**Detailed Tag Explanations**:
```yaml
apiVersion: apps/v1                 # Apps API group for workload resources
kind: Deployment                    # Workload controller type
metadata:
  name: weather-api                 # Deployment identifier
  namespace: weather-api            # Target namespace
  labels:                          # Deployment labels
    app: weather-api               # Application identifier
    version: v1.0                  # Version label for tracking
spec:
  replicas: 3                      # Desired number of pod instances
  selector:                        # Pod selection criteria
    matchLabels:                   # Labels that pods must have
      app: weather-api             # Must match template labels
  template:                        # Pod template specification
    metadata:
      labels:                      # Labels applied to created pods
        app: weather-api           # Must match selector
        version: v1.0              # Version tracking
    spec:                          # Pod specification
      containers:                  # Container definitions
      - name: weather-api          # Container name
        image: weather-api:latest  # Container image
        imagePullPolicy: Never     # Don't pull image (use local)
        ports:
        - containerPort: 8081      # Port exposed by container
          name: http               # Port name for reference
        env:                       # Environment variables
        - name: SERVER_PORT        # Environment variable name
          valueFrom:               # Source of the value
            configMapKeyRef:       # Reference to ConfigMap
              name: weather-api-config  # ConfigMap name
              key: server.port     # Key within ConfigMap
        resources:                 # Resource constraints
          requests:                # Minimum guaranteed resources
            memory: "256Mi"        # 256 Mebibytes of memory
            cpu: "250m"            # 250 milliCPU (0.25 CPU)
          limits:                  # Maximum allowed resources
            memory: "512Mi"        # 512 Mebibytes of memory
            cpu: "500m"            # 500 milliCPU (0.5 CPU)
        livenessProbe:             # Container alive check
          httpGet:                 # HTTP GET probe
            path: /actuator/health # Health check endpoint
            port: 8081             # Target port
          initialDelaySeconds: 30  # Wait before first check
          periodSeconds: 10        # Check interval
          timeoutSeconds: 5        # Timeout for each check
          failureThreshold: 3      # Failures before restart
        readinessProbe:            # Container ready check
          httpGet:                 # HTTP GET probe
            path: /actuator/health # Health check endpoint
            port: 8081             # Target port
          initialDelaySeconds: 20  # Wait before first check
          periodSeconds: 5         # Check interval
          timeoutSeconds: 3        # Timeout for each check
          failureThreshold: 3      # Failures before marking unready
      restartPolicy: Always        # Always restart failed containers
```

**Student Teaching Points**:
- Deployment → ReplicaSet → Pods (hierarchy)
- Labels and selectors must match for proper linking
- Resource requests vs limits (guaranteed vs maximum)
- Liveness probe: "Is the container alive?" (restarts if fails)
- Readiness probe: "Is the container ready to serve traffic?" (removes from load balancing)
- Rolling updates happen automatically on image changes

---

### 4. `service.yaml` - Network Access Layer

**Purpose**: Provides stable network endpoint for accessing pods, with load balancing and service discovery.

**What Happens Architecturally**:
- Creates a virtual IP (ClusterIP) within the cluster
- Maintains a list of healthy pod endpoints
- Provides load balancing across pods
- Enables service discovery via DNS
- Abstracts pod IP changes from clients

**Detailed Tag Explanations**:
```yaml
apiVersion: v1                    # Core API version
kind: Service                     # Service resource type
metadata:
  name: weather-api-service       # Service identifier
  namespace: weather-api          # Target namespace
  labels:                        # Service labels
    app: weather-api             # Application identifier
spec:
  selector:                      # Pod selection criteria
    app: weather-api             # Must match pod labels
  ports:                         # Port configuration
  - name: http                   # Port name
    port: 80                     # Service port (external)
    targetPort: 8081             # Pod port (internal)
    protocol: TCP                # Network protocol
  type: ClusterIP                # Service type (internal only)
```

**Service Types Explanation**:
- **ClusterIP**: Internal cluster access only (default)
- **NodePort**: Exposes service on each node's IP at a static port
- **LoadBalancer**: Creates external load balancer (cloud provider)
- **ExternalName**: Maps service to external DNS name

**Student Teaching Points**:
- Services provide stable endpoints for dynamic pods
- Selector links service to pods via labels
- kube-proxy handles the actual load balancing
- DNS name: `weather-api-service.weather-api.svc.cluster.local`
- Port mapping: Service port → Target port

---

### 5. `ingress.yaml` - External Access Gateway

**Purpose**: Manages external HTTP/HTTPS access to services, providing routing, SSL termination, and name-based virtual hosting.

**What Happens Architecturally**:
- Ingress Controller (like NGINX) processes rules
- Routes external traffic to appropriate services
- Provides HTTP/HTTPS termination
- Enables path-based and host-based routing
- Can handle SSL certificates and redirects

**Detailed Tag Explanations**:
```yaml
apiVersion: networking.k8s.io/v1    # Networking API group
kind: Ingress                       # Ingress resource type
metadata:
  name: weather-api-ingress         # Ingress identifier
  namespace: weather-api            # Target namespace
  annotations:                      # Controller-specific configuration
    nginx.ingress.kubernetes.io/rewrite-target: /     # URL rewriting
    nginx.ingress.kubernetes.io/ssl-redirect: "false" # Disable SSL redirect
spec:
  rules:                           # Routing rules
  - host: weather-api.local        # Domain name
    http:                          # HTTP rules
      paths:                       # Path-based routing
      - path: /                    # Match all paths
        pathType: Prefix           # Path matching type
        backend:                   # Target service
          service:
            name: weather-api-service  # Service name
            port:
              number: 80           # Service port
```

**Path Types Explanation**:
- **Exact**: Exact path match
- **Prefix**: Prefix-based matching
- **ImplementationSpecific**: Controller-dependent

**Student Teaching Points**:
- Ingress requires an Ingress Controller to function
- Popular controllers: NGINX, Traefik, HAProxy, Istio
- Annotations are controller-specific configurations
- Host-based routing enables multiple services on same IP
- Path-based routing enables microservice architectures

---

### 6. `kustomization.yaml` - Resource Management

**Purpose**: Manages and customizes Kubernetes resources without templating, providing a declarative way to handle configurations.

**What Happens Architecturally**:
- Processes all referenced YAML files
- Applies transformations (labels, name prefixes, etc.)
- Generates final manifests for deployment
- Enables environment-specific customizations
- Provides better resource organization

**Detailed Tag Explanations**:
```yaml
apiVersion: kustomize.config.k8s.io/v1beta1  # Kustomize API version
kind: Kustomization                          # Resource type
resources:                                   # List of resources to include
  - namespace.yaml                          # Namespace definition
  - configmap.yaml                          # Configuration
  - deployment.yaml                         # Application workload
  - service.yaml                            # Network service
  - ingress.yaml                            # External access
commonLabels:                               # Labels applied to all resources
  project: weather-api                      # Project identifier
  managed-by: kustomize                     # Management tool label
namePrefix: ""                              # Prefix for resource names
namespace: weather-api                      # Target namespace for all resources
```

**Student Teaching Points**:
- Kustomize is built into kubectl (kubectl apply -k)
- Alternative to Helm for simpler use cases
- Supports overlays for different environments (dev, staging, prod)
- No templating - uses patches and transformations
- Promotes configuration reuse and DRY principles

---

## Deployment Flow Architecture

### Step-by-Step Deployment Process:

1. **Namespace Creation**:
   - Kubernetes API server creates namespace
   - Resource quotas and RBAC policies apply
   - Network policies become effective

2. **ConfigMap Creation**:
   - Configuration data stored in etcd
   - Available for pod consumption
   - Can be updated independently

3. **Service Creation**:
   - Virtual IP allocated from cluster CIDR
   - Endpoints controller starts monitoring
   - DNS entry created in cluster DNS

4. **Deployment Creation**:
   - Deployment controller creates ReplicaSet
   - ReplicaSet controller creates pods
   - Scheduler assigns pods to nodes
   - kubelet pulls images and starts containers

5. **Ingress Creation**:
   - Ingress controller detects new rules
   - Load balancer configuration updated
   - External traffic routing configured

### Container Lifecycle:

```
Image Pull → Container Creation → Init → Readiness Probe → Traffic Routing
     ↓
Liveness Probe (ongoing) → Health Monitoring → Auto-restart if needed
```

### Network Flow:

```
External Request → Ingress Controller → Service → Pod (Round-robin)
```

## Production Considerations

### Security:
- Use Secrets for sensitive data
- Implement Network Policies
- Configure Pod Security Standards
- Use service accounts with minimal permissions

### Scalability:
- Horizontal Pod Autoscaler (HPA)
- Vertical Pod Autoscaler (VPA)
- Cluster Autoscaler
- Resource quotas and limits

### Monitoring:
- Prometheus for metrics
- Grafana for visualization
- Jaeger for distributed tracing
- ELK stack for logging

### High Availability:
- Multi-zone deployments
- Pod Disruption Budgets
- Health checks and circuit breakers
- Database clustering

## Common Kubectl Commands for Students

```bash
# Apply all resources
kubectl apply -k k8s/

# Check deployment status
kubectl get deployments -n weather-api

# View pod logs
kubectl logs -f deployment/weather-api -n weather-api

# Scale deployment
kubectl scale deployment weather-api --replicas=5 -n weather-api

# Port forward for local access
kubectl port-forward service/weather-api-service 8080:80 -n weather-api

# Delete all resources
kubectl delete -k k8s/
```

This comprehensive setup demonstrates modern Kubernetes practices and provides a solid foundation for teaching container orchestration concepts. 