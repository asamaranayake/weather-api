#!/bin/bash

# Weather API Development Deployment Script

set -e

# Configuration
APP_NAME="weather-api"
DEV_PORT="8081"
DEV_DEPLOY_DIR="/tmp/weather-api-dev"
JAR_FILE="target/weather-api-1.0-SNAPSHOT.jar"

echo "🚀 Starting Weather API Development Deployment..."

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    echo "Please run 'mvn package' first"
    exit 1
fi

# Stop existing application
echo "🛑 Stopping existing application on port $DEV_PORT..."
lsof -ti:$DEV_PORT | xargs kill -9 2>/dev/null || echo "No existing process found"
sleep 2

# Create deployment directory
echo "📁 Creating deployment directory..."
mkdir -p "$DEV_DEPLOY_DIR"

# Copy application files
echo "📦 Copying application files..."
cp "$JAR_FILE" "$DEV_DEPLOY_DIR/"
cp src/main/resources/application*.properties "$DEV_DEPLOY_DIR/" 2>/dev/null || true

# Start application
echo "▶️  Starting application..."
cd "$DEV_DEPLOY_DIR"
nohup java -jar -Dserver.port=$DEV_PORT -Dspring.profiles.active=dev *.jar > app.log 2>&1 &
APP_PID=$!
echo $APP_PID > app.pid

echo "⏳ Waiting for application to start..."
sleep 10

# Health check
for i in {1..30}; do
    if curl -f http://localhost:$DEV_PORT/actuator/health 2>/dev/null; then
        echo "✅ Application is running successfully!"
        echo "🌐 Application URL: http://localhost:$DEV_PORT"
        echo "🌤️  Test API: curl http://localhost:$DEV_PORT/api/weather/London"
        echo "❤️  Health Check: curl http://localhost:$DEV_PORT/actuator/health"
        echo "📊 Application Info: curl http://localhost:$DEV_PORT/actuator/info"
        echo "📝 Application logs: tail -f $DEV_DEPLOY_DIR/app.log"
        echo "🛑 Stop application: kill $APP_PID"
        exit 0
    fi
    echo "Attempt $i: Application not ready yet..."
    sleep 2
done

echo "❌ Application failed to start properly"
echo "📝 Check logs: cat $DEV_DEPLOY_DIR/app.log"
exit 1 