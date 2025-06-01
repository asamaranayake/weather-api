pipeline {
  agent any     // Run on any available agent (Jenkins node)

  tools {
    maven 'Maven 3.9.9'  // Name of Maven tool in Jenkins
    jdk 'JDK 11'         // Name of JDK in Jenkins config
  }

  environment {
    JAR_NAME = 'weather-api-1.0-SNAPSHOT.jar'
  }

  stages {
    stage('Checkout') {
       steps {
                git branch: 'main', url: 'https://github.com/asamaranayake/weather-api.git'
            }
        }

    stage('Build') {
      steps {
        sh 'mvn clean package -DskipTests'
      }
    }

    stage('Test') {
      steps {
        sh 'mvn test'
      }
    }

    stage('Deploy') {
      steps {
        echo "Killing old version if running..."
        sh 'pkill -f $JAR_NAME || true'

        echo "Starting app..."
        sh 'nohup java -jar $WORKSPACE/target/weather-api-0.0.1-SNAPSHOT.jar > $WORKSPACE/app.log 2>&1 &'

        echo "Running App .... "
        sh 'mvn spring-boot:run'
      }
    }
  }

  post {
    success {
      echo 'Pipeline completed successfully!'
    }
    failure {
      echo 'Pipeline failed. Check logs.'
    }
  }
}