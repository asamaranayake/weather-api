# Kubernetes Weather API - Quick Reference Card

## 📁 Project Structure
```
weather-api/
├── k8s/                          # Kubernetes manifests
│   ├── namespace.yaml            # Logical isolation
│   ├── configmap.yaml            # Configuration data
│   ├── deployment.yaml           # Application workload
│   ├── service.yaml              # Network endpoint
│   ├── ingress.yaml              # External access
│   ├── kustomization.yaml        # Resource management
│   └── README.md                 # Detailed documentation
├── deploy.sh                     # Automated deployment script
├── STEP_BY_STEP_EXECUTION.md     # Complete execution guide
├── QUICK_REFERENCE.md            # This file
├── Dockerfile                    # Container definition
└── pom.xml                       # Maven configuration
```

## 🚀 Deployment Commands

### One-Line Deployment
```bash
./deploy.sh
```

### Manual Step-by-Step
```bash
# 1. Build image
docker build -t weather-api:latest .

# 2. Deploy all resources
kubectl apply -k k8s/

# 3. Wait for readiness
kubectl wait --for=condition=available --timeout=300s deployment/weather-api -n weather-api

# 4. Check status
kubectl get all -n weather-api
```

## 🔍 Monitoring Commands

```bash
# Overall status
kubectl get all -n weather-api

# Pod details
kubectl describe pods -n weather-api

# Logs (follow)
kubectl logs -f deployment/weather-api -n weather-api

# Resource usage
kubectl top pods -n weather-api

# Events
kubectl get events -n weather-api --sort-by='.lastTimestamp'

# Service endpoints
kubectl get endpoints -n weather-api
```

## 🌐 Access Methods

### Local Port Forward
```bash
kubectl port-forward service/weather-api-service 8080:80 -n weather-api
# Access: http://localhost:8080
```

### Ingress (requires ingress controller)
```bash
# Add to /etc/hosts
echo "127.0.0.1 weather-api.local" >> /etc/hosts
# Access: http://weather-api.local
```

### Direct Pod Access (debugging)
```bash
kubectl port-forward pod/<pod-name> 8081:8081 -n weather-api
```

## ⚡ Scaling Commands

```bash
# Scale up/down
kubectl scale deployment weather-api --replicas=5 -n weather-api

# Auto-scaling (HPA)
kubectl autoscale deployment weather-api --cpu-percent=70 --min=3 --max=10 -n weather-api
```

## 🔄 Update Commands

```bash
# Rolling update
kubectl set image deployment/weather-api weather-api=weather-api:v2.0 -n weather-api

# Check rollout status
kubectl rollout status deployment/weather-api -n weather-api

# Rollback
kubectl rollout undo deployment/weather-api -n weather-api

# Rollout history
kubectl rollout history deployment/weather-api -n weather-api
```

## 🧹 Cleanup Commands

```bash
# Delete all resources
kubectl delete -k k8s/

# Or delete namespace (removes everything)
kubectl delete namespace weather-api
```

## 📊 Key Concepts Summary

### Resource Hierarchy
```
Namespace
├── ConfigMap (configuration)
├── Service (networking)
├── Ingress (external access)
└── Deployment
    └── ReplicaSet
        └── Pod(s)
            └── Container(s)
```

### Network Flow
```
Client → Ingress → Service → Pod(s)
```

### Health Checks
- **Liveness**: Container alive? (restart if fails)
- **Readiness**: Container ready for traffic? (load balance if ready)

### Resource Management
- **Requests**: Guaranteed resources
- **Limits**: Maximum allowed resources

## 🏷️ Essential Labels & Selectors

```yaml
# Deployment template labels (must match)
metadata:
  labels:
    app: weather-api

# Service selector (must match pod labels)
spec:
  selector:
    app: weather-api
```

## 🔧 Troubleshooting Quick Fixes

### Pod Issues
```bash
# Pod stuck in Pending
kubectl describe pod <pod-name> -n weather-api
# Check: resource constraints, node capacity, image pull

# Pod CrashLoopBackOff
kubectl logs <pod-name> -n weather-api --previous
# Check: application errors, resource limits, health check config

# ImagePullBackOff
kubectl describe pod <pod-name> -n weather-api
# Check: image name, tag, registry access
```

### Service Issues
```bash
# Service not reachable
kubectl get endpoints -n weather-api
# Check: selector matches pod labels, pod readiness

# DNS not working
kubectl run debug --image=busybox --rm -it --restart=Never -- nslookup weather-api-service.weather-api.svc.cluster.local
```

### Ingress Issues
```bash
# Check ingress controller
kubectl get pods -n ingress-nginx  # or your ingress namespace

# Check ingress resource
kubectl describe ingress weather-api-ingress -n weather-api
```

## 📝 Important Files Explained

| File | Purpose | Key Concepts |
|------|---------|--------------|
| `namespace.yaml` | Isolation | Resource boundaries, DNS subdomain |
| `configmap.yaml` | Configuration | External config, environment variables |
| `deployment.yaml` | App Management | Replicas, health checks, resources |
| `service.yaml` | Networking | Load balancing, service discovery |
| `ingress.yaml` | External Access | Host/path routing, SSL termination |
| `kustomization.yaml` | Resource Mgmt | Multi-resource deployment |

## 🎯 Teaching Points for Students

1. **Start Simple**: Deploy → Service → Access
2. **Labels Matter**: Selectors must match labels
3. **Health is Key**: Liveness vs Readiness probes
4. **Resources**: Always set requests/limits
5. **Namespaces**: Isolate environments
6. **Troubleshoot**: Use describe, logs, events
7. **Scale**: Start small, scale as needed
8. **Update**: Rolling updates for zero downtime

## 🔗 Health Check Endpoints

- **Health**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Info**: `/actuator/info`

## 📋 Prerequisites Checklist

- [ ] Docker installed and running
- [ ] kubectl installed and configured
- [ ] Kubernetes cluster accessible
- [ ] Ingress controller (for external access)
- [ ] Sufficient cluster resources

## 🚨 Production Readiness

- [ ] Use specific image tags (not `latest`)
- [ ] Set resource requests/limits
- [ ] Configure health checks
- [ ] Implement security policies
- [ ] Set up monitoring/logging
- [ ] Plan backup/disaster recovery
- [ ] Configure auto-scaling
- [ ] Implement CI/CD pipeline 