# Weather API CI/CD Quick Start Guide

## 🎯 What You Have Now

✅ **Working Spring Boot Weather API** - Deployed and accessible at http://localhost:8081  
✅ **Complete CI/CD Pipeline** - Ready to use with Jenkins  
✅ **Automated Testing** - Unit tests that pass  
✅ **Health Monitoring** - Built-in health checks  
✅ **Environment Configuration** - Dev and Staging environments  

## 🚀 Quick Test Your API

Your weather API is currently running! Test these endpoints:

```bash
# Get weather for London
curl http://localhost:8081/api/weather/London

# Get weather for New York
curl "http://localhost:8081/api/weather/New York"

# Check application health
curl http://localhost:8081/actuator/health

# Get application info
curl http://localhost:8081/actuator/info
```

## 🔧 Jenkins CI/CD Setup

### Step 1: Configure Jenkins Tools
1. Open Jenkins: http://localhost:8080
2. Go to **Manage Jenkins** → **Global Tool Configuration**
3. Configure **JDK 11**: `/Library/Java/JavaVirtualMachines/zulu-11.jdk/Contents/Home`
4. Configure **Maven 3.9.9**: `/opt/homebrew/Cellar/maven/3.9.9/libexec`

### Step 2: Create Pipeline Job
1. **New Item** → Name: `weather-api-cicd` → **Pipeline**
2. **Pipeline** section:
   - **Definition**: `Pipeline script from SCM`
   - **SCM**: `Git`
   - **Repository URL**: Your git repository
   - **Script Path**: `Jenkinsfile.cicd`

### Step 3: Run Pipeline
1. Click **Build Now**
2. Watch the pipeline stages:
   - ✅ Checkout
   - ✅ Build
   - ✅ Unit Tests
   - ✅ Code Quality & Security
   - ✅ Package
   - ✅ Deploy to Dev
   - ✅ Integration Tests
   - ✅ Deploy to Staging (for develop/main branches)

## 📊 Pipeline Features

### Environments
- **Dev**: Port 8081 - Deploys from any branch
- **Staging**: Port 8082 - Deploys from develop/main branches
- **Production**: Manual approval required

### Automated Testing
- Unit tests with TestNG
- Integration tests against deployed app
- Health checks
- API endpoint validation

### Monitoring
- Application health: `/actuator/health`
- Application metrics: `/actuator/metrics`
- Application info: `/actuator/info`

## 🛠️ Manual Commands

```bash
# Build and test locally
mvn clean package

# Deploy to dev environment
./scripts/deploy-dev.sh

# Check if app is running
lsof -i :8081

# View application logs
tail -f /tmp/weather-api-dev/app.log

# Stop application
kill $(cat /tmp/weather-api-dev/app.pid)
```

## 🌐 API Endpoints

| Endpoint | Method | Description | Example |
|----------|--------|-------------|---------|
| `/api/weather/{city}` | GET | Get weather for city | `/api/weather/London` |
| `/actuator/health` | GET | Application health | Returns UP/DOWN status |
| `/actuator/info` | GET | Application info | Build and version info |
| `/actuator/metrics` | GET | Application metrics | Performance metrics |

## 📁 Project Structure

```
weather-api/
├── Jenkinsfile.cicd              # Complete CI/CD pipeline
├── Jenkinsfile.local             # Simple local pipeline
├── scripts/deploy-dev.sh         # Local deployment script
├── src/main/resources/
│   ├── application.properties    # Default config
│   ├── application-dev.properties    # Dev environment
│   └── application-staging.properties # Staging environment
├── CI_CD_SETUP_GUIDE.md         # Detailed setup guide
└── QUICK_START_GUIDE.md         # This file
```

## 🎉 Success! Your CI/CD Pipeline is Ready

Your Weather API now has:
- ✅ Automated builds and testing
- ✅ Multi-environment deployment
- ✅ Health monitoring
- ✅ Real weather data integration
- ✅ Jenkins pipeline automation

**Next Steps:**
1. Set up Jenkins with the configuration above
2. Run your first pipeline build
3. Access your deployed API at http://localhost:8081
4. Monitor builds and deployments through Jenkins dashboard

**Need Help?** Check the detailed `CI_CD_SETUP_GUIDE.md` for troubleshooting and advanced configuration. 