// 🎯 Simple Jenkins Pipeline for Kids to Learn CI/CD
// This file tells Jenkins how to build and test our Weather App!

pipeline {
    agent any  // Run on any available computer
    
    // 🛠️ Tools we need (like having the right LEGO pieces!)
    tools {
        maven 'Maven 3.9.9'  // Tool to build our app
        jdk 'JDK 11'         // Java to run our app
    }
    
    // 📋 Step-by-step instructions for Jenkins
    stages {
        
        // Step 1: Get the code 📥
        stage('Get Code') {
            steps {
                echo '📥 Getting our code from the computer...'
                checkout scm  // Download the latest code
            }
        }
        
        // Step 2: Build the app 🔨
        stage('Build App') {
            steps {
                echo '🔨 Building our Weather App...'
                sh 'mvn clean compile'  // Turn code into an app
            }
        }
        
        // Step 3: Test the app 🧪
        stage('Test App') {
            steps {
                echo '🧪 Testing our app to make sure it works...'
                sh 'mvn test'  // Run tests to check everything is OK
            }
        }
        
        // Step 4: Package the app 📦
        stage('Package App') {
            steps {
                echo '📦 Wrapping up our app in a nice package...'
                sh 'mvn package -DskipTests'  // Create a .jar file
                
                // Save our app package for later
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        
        // Step 5: Deploy the app 🚀
        stage('Deploy App') {
            steps {
                echo '🚀 Starting our Weather App...'
                script {
                    // Stop any old version running
                    sh 'lsof -ti:8081 | xargs kill -9 || true'
                    sh 'sleep 3'
                    
                    // Start our new app
                    sh '''
                        cd target
                        nohup java -jar *.jar > app.log 2>&1 &
                        echo "App is starting..."
                        sleep 10
                    '''
                }
            }
        }
        
        // Step 6: Check if app is working 🩺
        stage('Health Check') {
            steps {
                echo '🩺 Checking if our app is healthy...'
                script {
                    // Try to talk to our app
                    def result = sh(
                        script: 'curl -f http://localhost:8081/actuator/health || echo "App not ready"',
                        returnStdout: true
                    ).trim()
                    
                    if (result.contains('"status":"UP"')) {
                        echo '✅ App is healthy and running!'
                        echo '🌤️  Try it: curl http://localhost:8081/api/weather/London'
                    } else {
                        echo '⚠️  App might need more time to start'
                    }
                }
            }
        }
    }
    
    // 📢 What to do when we're done
    post {
        success {
            echo '''
            🎉 SUCCESS! Our Weather App is ready!
            
            ✅ Code downloaded
            ✅ App built
            ✅ Tests passed
            ✅ App packaged
            ✅ App deployed
            ✅ Health check done
            
            🌐 Your app is running at: http://localhost:8081
            🌤️  Test it: curl http://localhost:8081/api/weather/London
            '''
        }
        
        failure {
            echo '''
            😞 OOPS! Something went wrong.
            
            Don't worry - this is how we learn!
            Check the logs to see what happened.
            '''
        }
        
        always {
            echo '🧹 Cleaning up...'
            // Clean up test results but keep them for Jenkins to show
            junit '**/target/surefire-reports/*.xml'
        }
    }
} 