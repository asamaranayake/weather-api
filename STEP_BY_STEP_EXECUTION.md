# Step-by-Step Kubernetes Deployment Execution Guide

## Complete Process Overview

This document explains what happens **exactly** when you deploy your Weather API to Kubernetes, step by step, so you can teach students the complete flow.

---

## Phase 1: Pre-Deployment Setup

### Step 1: Environment Verification
```bash
# Check Docker is running
docker info
```
**What Happens**: 
- Docker daemon connectivity test
- Container runtime verification
- Resource availability check

### Step 2: Kubernetes Cluster Access
```bash
# Check cluster connectivity
kubectl cluster-info
kubectl get nodes
```
**What Happens**:
- kubeconfig file is read (`~/.kube/config`)
- TLS certificate validation
- API server authentication
- Cluster component status verification

---

## Phase 2: Container Image Preparation

### Step 3: Build Docker Image
```bash
docker build -t weather-api:latest .
```
**What Happens Internally**:
1. **Dockerfile Processing**:
   - Stage 1: Maven build container created
   - Source code copied to `/app`
   - `mvn clean package` executes
   - JAR file created in `/app/target/`

2. **Stage 2: Runtime container**:
   - New lightweight base image
   - JAR copied from build stage
   - Health check configuration applied
   - ENTRYPOINT defined

3. **Image Storage**:
   - Layers created and cached
   - Image tagged as `weather-api:latest`
   - Stored in local Docker registry

### Step 4: Image Availability (Local Development)
**For Docker Desktop**: Image automatically available to Kubernetes
**For Minikube**: `minikube image load weather-api:latest`
**For Kind**: `kind load docker-image weather-api:latest`

---

## Phase 3: Kubernetes Resource Deployment

### Step 5: Namespace Creation
```bash
kubectl apply -f k8s/namespace.yaml
```
**What Happens**:
1. **API Server Processing**:
   - YAML parsed and validated
   - Resource quota defaults applied
   - RBAC policies activated

2. **etcd Storage**:
   - Namespace object stored
   - Cluster DNS updated
   - Network policies initialized

**Architecture Impact**: Creates isolated environment with DNS subdomain `*.weather-api.svc.cluster.local`

### Step 6: ConfigMap Creation
```bash
kubectl apply -f k8s/configmap.yaml
```
**What Happens**:
1. **Data Storage**:
   - Key-value pairs stored in etcd
   - Available for pod consumption
   - Versioned for tracking changes

2. **Access Preparation**:
   - Environment variable mapping prepared
   - Volume mount options configured

**Architecture Impact**: Configuration externalized from container images, enabling environment-specific deployments.

### Step 7: Service Creation
```bash
kubectl apply -f k8s/service.yaml
```
**What Happens**:
1. **Virtual IP Allocation**:
   - ClusterIP assigned from service CIDR range
   - Internal DNS entry created: `weather-api-service.weather-api.svc.cluster.local`

2. **Endpoints Controller Activation**:
   - Monitors pods with matching labels (`app: weather-api`)
   - Maintains healthy endpoint list
   - Updates load balancer pool

3. **kube-proxy Configuration**:
   - iptables/IPVS rules created on each node
   - Traffic routing rules established

**Architecture Impact**: Stable network endpoint created, abstracting pod IP changes.

### Step 8: Deployment Creation
```bash
kubectl apply -f k8s/deployment.yaml
```
**What Happens**:
1. **Deployment Controller**:
   - Creates ReplicaSet with desired state (3 replicas)
   - Monitors and maintains pod count
   - Handles rolling updates

2. **ReplicaSet Controller**:
   - Creates pod specifications
   - Ensures desired replica count
   - Manages pod lifecycle

3. **Scheduler**:
   - Evaluates node resources (CPU, memory)
   - Considers node affinity rules
   - Assigns pods to appropriate nodes

4. **kubelet (on each node)**:
   - Pulls container image (if needed)
   - Creates containers with specified resources
   - Sets up networking and storage
   - Starts health check monitoring

**Container Startup Sequence**:
```
Image Pull → Container Creation → Application Boot → Health Checks
```

5. **Health Check Process**:
   - **Liveness Probe**: HTTP GET to `/actuator/health` every 10s
   - **Readiness Probe**: HTTP GET to `/actuator/health` every 5s
   - **Service Registration**: Only after readiness probe succeeds

**Architecture Impact**: Application becomes highly available with self-healing capabilities.

### Step 9: Ingress Creation
```bash
kubectl apply -f k8s/ingress.yaml
```
**What Happens**:
1. **Ingress Controller Detection**:
   - NGINX/Traefik controller processes new rules
   - Load balancer configuration updated
   - SSL certificates (if configured) applied

2. **Traffic Routing Setup**:
   - Host-based routing: `weather-api.local`
   - Path-based routing: `/` → `weather-api-service:80`
   - External access point established

**Architecture Impact**: External traffic can now reach the application.

---

## Phase 4: Runtime Architecture

### Container Resource Management
```yaml
resources:
  requests:      # Guaranteed resources
    memory: "256Mi"
    cpu: "250m"
  limits:        # Maximum allowed
    memory: "512Mi"
    cpu: "500m"
```

**What This Means**:
- Each pod guaranteed 256MB RAM and 0.25 CPU cores
- Can burst up to 512MB RAM and 0.5 CPU cores
- Kubernetes scheduler considers these for placement
- OOM killer triggers if memory limit exceeded

### Health Check System
**Liveness Probe** (Every 10 seconds):
```
kubelet → HTTP GET → Pod:8081/actuator/health
```
- **Success**: Container considered alive
- **Failure** (3 consecutive): Container restarted

**Readiness Probe** (Every 5 seconds):
```
kubelet → HTTP GET → Pod:8081/actuator/health
```
- **Success**: Pod added to service endpoints
- **Failure**: Pod removed from service endpoints (no traffic)

### Load Balancing Flow
```
External Request → Ingress Controller → Service (ClusterIP) → Pod (Round-robin)
```

1. **Client Request**: `curl http://weather-api.local/api/weather`
2. **DNS Resolution**: `weather-api.local` → Ingress Controller IP
3. **Ingress Processing**: Route to `weather-api-service:80`
4. **Service Load Balancing**: Select healthy pod from endpoint list
5. **Pod Processing**: Handle request on port 8081

---

## Phase 5: Monitoring and Management

### Real-time Status Checking
```bash
# Overall status
kubectl get all -n weather-api

# Detailed pod information
kubectl describe pods -n weather-api

# Resource usage
kubectl top pods -n weather-api

# Logs streaming
kubectl logs -f deployment/weather-api -n weather-api
```

### Scaling Operations
```bash
# Manual scaling
kubectl scale deployment weather-api --replicas=5 -n weather-api
```
**What Happens**:
1. Deployment spec updated (replicas: 3 → 5)
2. ReplicaSet creates 2 additional pods
3. Scheduler assigns new pods to nodes
4. kubelet starts new containers
5. Health checks validate new pods
6. Service endpoints updated automatically

### Rolling Update Process
```bash
# Update image
kubectl set image deployment/weather-api weather-api=weather-api:v2.0 -n weather-api
```
**What Happens**:
1. **New ReplicaSet Created**: With new image version
2. **Gradual Rollout**: 
   - Start 1 pod with new version
   - Wait for readiness
   - Terminate 1 old pod
   - Repeat until complete
3. **Traffic Shifting**: Only ready pods receive traffic
4. **Rollback Capability**: Previous ReplicaSet kept for rollback

---

## Phase 6: Network Architecture Deep Dive

### DNS Resolution Chain
```
weather-api-service.weather-api.svc.cluster.local
    ↓
Service ClusterIP (e.g., 10.96.145.23)
    ↓
Endpoint IPs (e.g., 10.244.1.15:8081, 10.244.2.33:8081, 10.244.3.44:8081)
```

### Traffic Flow with Component Interaction
```
[Client] → [Ingress Controller] → [Service] → [kube-proxy] → [Pod]
   ↓              ↓                    ↓           ↓            ↓
DNS Resolve   Host Routing        ClusterIP    iptables     Container
   ↓              ↓                    ↓           ↓            ↓
Load Balancer  Path Matching      Port Forward  DNAT Rule   Application
```

### Pod-to-Pod Communication
- **Same Node**: Direct container networking
- **Different Nodes**: CNI plugin (Flannel, Calico, etc.)
- **Service Discovery**: DNS-based service resolution

---

## Phase 7: Production Considerations

### Security Implementation
```yaml
# Pod Security Context
securityContext:
  runAsNonRoot: true
  runAsUser: 1000
  fsGroup: 2000
  capabilities:
    drop:
    - ALL
```

### Resource Quotas
```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: weather-api-quota
spec:
  hard:
    requests.cpu: "4"
    requests.memory: 8Gi
    limits.cpu: "8"
    limits.memory: 16Gi
    pods: "10"
```

### Horizontal Pod Autoscaler (HPA)
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: weather-api-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: weather-api
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

## Phase 8: Troubleshooting Common Issues

### Pod Startup Issues
```bash
# Check pod status
kubectl get pods -n weather-api -o wide

# Check events
kubectl get events -n weather-api --sort-by='.lastTimestamp'

# Describe problematic pod
kubectl describe pod <pod-name> -n weather-api
```

### Common Problems and Solutions:

1. **ImagePullBackOff**: 
   - Check image name and tag
   - Verify image registry access
   - Check imagePullPolicy

2. **CrashLoopBackOff**:
   - Check application logs
   - Verify resource limits
   - Check liveness probe configuration

3. **Service Unreachable**:
   - Verify service selector matches pod labels
   - Check endpoint list: `kubectl get endpoints -n weather-api`
   - Verify port mappings

4. **Ingress Not Working**:
   - Check ingress controller is running
   - Verify DNS resolution
   - Check ingress resource rules

---

## Summary: Complete Deployment Flow

```mermaid
graph TD
    A[Build Image] → B[Create Namespace]
    B → C[Create ConfigMap]
    C → D[Create Service]
    D → E[Create Deployment]
    E → F[Pods Start]
    F → G[Health Checks Pass]
    G → H[Service Endpoints Updated]
    H → I[Create Ingress]
    I → J[External Access Ready]
    
    K[Continuous Monitoring] → F
    L[Auto-scaling] → E
    M[Rolling Updates] → E
```

This comprehensive guide provides everything students need to understand Kubernetes deployment from basics to advanced concepts, including the underlying architecture and what happens at each step of the process. 