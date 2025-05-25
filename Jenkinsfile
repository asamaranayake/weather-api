pipeline {
    agent any
    
    tools {
        maven 'Maven 3.9.9'
        jdk 'JDK 11'
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/asamaranayake/weather-api.git'
            }
        }
        
        stage('Build') {
            steps {
                echo '🔨 Building the application...'
                sh 'mvn clean compile'
            }
        }
        
        stage('Test') {
            steps{
                echo '🧪 Running tests...'
                sh 'mvn test'
            }
            post {
                always {
                    // Archive test results
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Code Coverage') {
            steps {
                echo '📊 Generating code coverage report...'
                sh 'mvn jacoco:report'
                
                // Publish JaCoCo coverage report
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/site/jacoco',
                    reportFiles: 'index.html',
                    reportName: 'JaCoCo Coverage Report'
                ])
                
                // Check coverage thresholds
                sh 'mvn jacoco:check'
            }
        }
        
        stage('Package') {
            steps {
                echo '📦 Packaging the application...'
                sh 'mvn package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }
    
    post {
        success {
            echo '''
            🎉 SUCCESS! Pipeline completed with code coverage!
            
            ✅ Code checked out
            ✅ Application built
            ✅ Tests passed
            ✅ Code coverage generated
            ✅ Application packaged
            
            📊 Check the "JaCoCo Coverage Report" for detailed coverage metrics!
            '''
        }
        failure {
            echo '''
            ❌ Pipeline failed!
            
            Possible issues:
            • Build compilation errors
            • Test failures
            • Code coverage below threshold (50%)
            • Packaging issues
            
            Check the logs above for details! 🔍
            '''
        }
        always {
            echo '🧹 Cleaning up...'
        }
    }
}