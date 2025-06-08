pipeline {
    agent any

    // Configure tools (Maven from Global Tool Configuration)
    tools {
        maven 'Maven-3.9.10'  // Your configured Maven name
    }
    
    // Simple environment variables for students
    environment {
        APP_NAME = 'weather-api'
        IMAGE_NAME = 'weather-api'
        CONTAINER_NAME = 'weather-api-app'
        PORT = '8081'
    }
    
    stages {
        
        // Stage 1: Welcome & Info
        stage('📋 Pipeline Start') {
            steps {
                echo "🚀 Welcome to CI/CD Pipeline!"
                echo "=============================="
                echo "📦 App Name: ${APP_NAME}"
                echo "🐳 Image: ${IMAGE_NAME}"
                echo "🌐 Port: ${PORT}"
                echo "🏷️ Build: #${BUILD_NUMBER}"
                echo "=============================="
            }
        }
        
        // Stage 2: Get Code from Git
        stage('📥 Get Source Code') {
            steps {
                echo "📥 Getting source code from Git..."
                
                // Jenkins automatically checks out code here
                script {
                    sh 'echo "✅ Code downloaded!"'
                    sh 'echo "📁 Files in project:"'
                    sh 'ls -la'
                }
            }
        }
        
        // Stage 3: Test the Application
        stage('🧪 Test Application') {
            steps {
                echo "🧪 Running tests..."
                
                script {
                    sh '''
                        echo "🔍 Using Maven from Jenkins Global Tool Configuration..."
                        mvn --version
                        
                        echo "🧪 Running unit tests..."
                        mvn clean test
                        echo "✅ Tests passed!"
                    '''
                }
            }
            post {
                always {
                    // Correct Jenkins syntax for test results
                    script {
                        if (fileExists('target/surefire-reports/*.xml')) {
                            junit 'target/surefire-reports/*.xml'
                            echo "📊 Test results published!"
                        } else {
                            echo "📊 No test results found (this is OK for simple projects)"
                        }
                    }
                }
            }
        }
        
        // Stage 4: Build Docker Image
        stage('🐳 Build Docker Image') {
            steps {
                echo "🐳 Building Docker image..."
                
                script {
                    sh '''
                        echo "📦 Building image: ${IMAGE_NAME}"
                        
                        # Build Docker image (Jenkins provides source code)
                        docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} .
                        docker tag ${IMAGE_NAME}:${BUILD_NUMBER} ${IMAGE_NAME}:latest
                        
                        echo "✅ Docker image built!"
                        docker images | grep ${IMAGE_NAME}
                    '''
                }
            }
        }
        
        // Stage 5: Manual Check (Students can review)
        stage('👀 Manual Check') {
            steps {
                script {
                    echo "⏸️ STUDENT CHECKPOINT"
                    echo "===================="
                    echo "👀 Take a moment to review:"
                    echo "   ✅ Did tests pass?"
                    echo "   ✅ Is Docker image built?"
                    echo "   ✅ Ready to deploy?"
                    echo "===================="
                    
                    // Simple approval
                    def proceed = input(
                        message: '🚀 Deploy the Weather API?',
                        parameters: [
                            choice(
                                name: 'ACTION',
                                choices: ['✅ Yes, Deploy!', '❌ No, Stop'],
                                description: 'What do you want to do?'
                            )
                        ]
                    )
                    
                    if (proceed == '❌ No, Stop') {
                        error('🛑 Deployment stopped by student')
                    }
                    
                    echo "✅ Student approved deployment!"
                }
            }
        }
        
        // Stage 6: Deploy Application
        stage('🚀 Deploy Application') {
            steps {
                echo "🚀 Deploying Weather API..."
                
                script {
                    sh '''
                        echo "🛑 Stopping old application..."
                        docker stop ${CONTAINER_NAME} || true
                        docker rm ${CONTAINER_NAME} || true
                        
                        echo "🚀 Starting new application..."
                        docker run -d \\
                            --name ${CONTAINER_NAME} \\
                            -p ${PORT}:${PORT} \\
                            --restart unless-stopped \\
                            ${IMAGE_NAME}:latest
                        
                        echo "✅ Application deployed!"
                    '''
                }
            }
        }
        
        // Stage 7: Check if it Works
        stage('🔍 Health Check') {
            steps {
                echo "🔍 Checking if application is working..."
                
                script {
                    sh '''
                        echo "⏳ Waiting for app to start..."
                        sleep 20
                        
                        echo "🔍 Checking container..."
                        docker ps | grep ${CONTAINER_NAME}
                        
                        echo "🩺 Testing health endpoint..."
                        for i in {1..6}; do
                            echo "Attempt $i/6..."
                            if curl -s http://localhost:${PORT}/actuator/health; then
                                echo "✅ Application is healthy!"
                                break
                            else
                                echo "⏳ Still starting... waiting 10 seconds"
                                sleep 10
                            fi
                        done
                    '''
                }
            }
        }
    }
    
    // What happens at the end
    post {
        success {
            echo ""
            echo "🎉 SUCCESS! Weather API is deployed!"
            echo "===================================="
            echo "🌐 Open in browser: http://localhost:${PORT}"
            echo "🩺 Health check: http://localhost:${PORT}/actuator/health"
            echo "📊 View logs: docker logs ${CONTAINER_NAME}"
            echo "===================================="
        }
        
        failure {
            echo ""
            echo "❌ OOPS! Something went wrong!"
            echo "============================="
            echo "🔍 Check the red error messages above"
            echo "🧹 Cleaning up..."
            
            script {
                sh '''
                    docker stop ${CONTAINER_NAME} || true
                    docker rm ${CONTAINER_NAME} || true
                '''
            }
        }
        
        always {
            echo "🧹 Cleaning up workspace..."
            // Use built-in Jenkins method (no plugin required)
            script {
                try {
                    deleteDir()
                    echo "✅ Workspace cleaned"
                } catch (Exception e) {
                    echo "⚠️ Workspace cleanup skipped (not critical)"
                }
            }
        }
    }
} 