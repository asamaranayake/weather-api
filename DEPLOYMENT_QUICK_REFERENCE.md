# 🚀 Weather API Deployment - Quick Reference

## 📦 Deployment Order (Critical!)

```bash
# 1. Namespaces (Foundation)
kubectl apply -f k8s/namespace.yaml

# 2. ConfigMaps (Configuration)
kubectl apply -f k8s/configmap.yaml

# 3. Deployments (Applications)
kubectl apply -f k8s/deployment.yaml

# 4. Services (Internal Networking)
kubectl apply -f k8s/service.yaml

# 5. Ingress (External Access)
kubectl apply -f k8s/ingress.yaml
```

## 🌐 Access URLs

| Environment | URL | Description |
|-------------|-----|-------------|
| **Dev** | `http://localhost/dev/weather-api` | Development |
| **QA** | `http://localhost/qa/weather-api` | Quality Assurance |
| **Prod** | `http://localhost/prod/weather-api` | Production |

## 🔍 Essential Commands

### Status Check
```bash
# Check all resources
kubectl get all -n weather-api-dev
kubectl get all -n weather-api-qa
kubectl get all -n weather-api-prod

# Check ingress
kubectl get ingress -A
```

### Logs & Debugging
```bash
# Pod logs
kubectl logs -f -n weather-api-dev deployment/weather-api-dev

# Pod status
kubectl get pods -n weather-api-dev

# Events
kubectl get events -n weather-api-dev --sort-by='.lastTimestamp'
```

### Testing
```bash
# Health checks
curl http://localhost/dev/actuator/health
curl http://localhost/qa/actuator/health
curl http://localhost/prod/actuator/health

# API endpoints
curl http://localhost/dev/weather-api
curl http://localhost/qa/weather-api
curl http://localhost/prod/weather-api
```

## 🏗️ Architecture Summary

```
Client → Ingress → Service → Pod
  ↓        ↓        ↓       ↓
localhost  Path     Port    App
  :80     /dev/*   80→8081  :8081
```

## 📊 Environment Differences

| Aspect | Dev | QA | Prod |
|--------|-----|----|----- |
| **Replicas** | 1 | 2 | 3 |
| **Memory** | 256Mi-512Mi | 256Mi-512Mi | 512Mi-1Gi |
| **CPU** | 250m-500m | 250m-500m | 500m-1000m |
| **Logging** | DEBUG | INFO | WARN |
| **Profile** | dev | staging | prod |

## 🚨 Troubleshooting

### Pod Issues
```bash
kubectl describe pod -n weather-api-dev <pod-name>
kubectl logs -n weather-api-dev <pod-name>
```

### Service Issues
```bash
kubectl get endpoints -n weather-api-dev
kubectl port-forward -n weather-api-dev service/weather-api-service 8080:80
```

### Ingress Issues
```bash
kubectl describe ingress -n weather-api-dev
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller
```

## 🔧 Common Fixes

### Restart Deployment
```bash
kubectl rollout restart deployment/weather-api-dev -n weather-api-dev
```

### Scale Replicas
```bash
kubectl scale deployment/weather-api-dev --replicas=2 -n weather-api-dev
```

### Delete & Recreate
```bash
kubectl delete -f k8s/deployment.yaml
kubectl apply -f k8s/deployment.yaml
```

## 📝 YAML File Purposes

| File | Purpose | Dependencies |
|------|---------|-------------|
| `namespace.yaml` | Environment isolation | None |
| `configmap.yaml` | Application configuration | Namespaces |
| `deployment.yaml` | Pod management | Namespaces, ConfigMaps |
| `service.yaml` | Internal networking | Deployments |
| `ingress.yaml` | External access | Services |

---
*For detailed explanations, see `KUBERNETES_DEPLOYMENT_GUIDE.md`* 