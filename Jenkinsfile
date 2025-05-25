pipeline {
    agent any
    
    tools {
        maven 'Maven 3.8.6'
        jdk 'JDK 11'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        
        stage('Deploy to Dev') {
            when {
                branch 'develop'
            }
            steps {
                sh '''
                    # Copy JAR to dev server
                    scp -o StrictHostKeyChecking=no target/*.jar user@dev-server:/opt/weather-api/
                    
                    # Restart service
                    ssh -o StrictHostKeyChecking=no user@dev-server 'sudo systemctl restart weather-api'
                '''
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                // Require manual approval
                input message: 'Deploy to production?', ok: 'Approve'
                
                sh '''
                    # Copy JAR to production server
                    scp -o StrictHostKeyChecking=no target/*.jar user@prod-server:/opt/weather-api/
                    
                    # Restart service
                    ssh -o StrictHostKeyChecking=no user@prod-server 'sudo systemctl restart weather-api'
                '''
            }
        }
    }
    
    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
            // Send notification
            mail to: 'team@example.com', subject: 'Build failed', body: 'Pipeline failed'
        }
    }
} 