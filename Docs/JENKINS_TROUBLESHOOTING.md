# 🔧 Jenkins Pipeline Troubleshooting Guide

## 🚨 Error: workflow-cps PluginClassLoader

This error typically indicates a problem with Jenkins Pipeline execution. Here's how to fix it step by step.

## 🔍 Step 1: Check Required Plugins

### Essential Plugins for Your Pipeline:
1. **Pipeline: Stage View** - For pipeline visualization
2. **Pipeline: Groovy** - For pipeline script execution
3. **HTML Publisher** - For JaCoCo coverage reports
4. **JUnit** - For test result publishing
5. **Git** - For source code checkout
6. **Maven Integration** - For Maven builds

### How to Install Missing Plugins:
1. Go to **Manage Jenkins** → **Manage Plugins**
2. Click **Available** tab
3. Search for each plugin above
4. Check the box and click **Install without restart**
5. Restart Jenkins when done

## 🛠️ Step 2: Verify Tool Configuration

### Check Java Configuration:
1. **Manage Jenkins** → **Global Tool Configuration**
2. **JDK** section:
   - Name: `JDK 11`
   - JAVA_HOME: `/Library/Java/JavaVirtualMachines/zulu-11.jdk/Contents/Home`
   - Or find your Java path: `echo $JAVA_HOME` or `/usr/libexec/java_home -v 11`

### Check Maven Configuration:
1. **Manage Jenkins** → **Global Tool Configuration**
2. **Maven** section:
   - Name: `Maven 3.9.9`
   - MAVEN_HOME: `/opt/homebrew/Cellar/maven/3.9.9/libexec`
   - Or find your Maven path: `mvn -version` then `which mvn`

## 🧪 Step 3: Test with Debug Pipeline

### Use the Debug Jenkinsfile:
1. Create a new pipeline job: **"Debug-Weather-API"**
2. Use **Pipeline script from SCM**
3. **Script Path**: `Jenkinsfile.debug`
4. Run the pipeline to identify the specific issue

### Expected Debug Output:
```
✅ Jenkins Version: 2.x.x
✅ Build Number: 1
✅ Workspace: /var/jenkins_home/workspace/Debug-Weather-API
✅ Java is available
✅ Maven is available
✅ Git checkout successful
✅ Maven version check successful
```

## 🔧 Step 4: Common Fixes

### Fix 1: Remove Tool Declarations (Temporary)
If tools are causing issues, try this simplified Jenkinsfile:

```groovy
pipeline {
    agent any
    
    // Remove this section temporarily
    // tools {
    //     maven 'Maven 3.9.9'
    //     jdk 'JDK 11'
    // }
    
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
    }
}
```

### Fix 2: Use Absolute Paths
If tool configuration fails, use absolute paths:

```groovy
stage('Build') {
    steps {
        sh '/opt/homebrew/bin/mvn clean compile'
    }
}
```

### Fix 3: Simplify publishHTML
Replace the publishHTML step with a simpler version:

```groovy
// Instead of publishHTML, use this:
archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
```

## 🚨 Step 5: Emergency Simple Pipeline

If all else fails, use this ultra-simple pipeline:

```groovy
pipeline {
    agent any
    
    stages {
        stage('Test') {
            steps {
                echo 'Hello from Jenkins!'
                sh 'echo "Current directory: $(pwd)"'
                sh 'echo "Java version:"'
                sh 'java -version'
                sh 'echo "Maven version:"'
                sh 'mvn -version'
            }
        }
    }
}
```

## 🔍 Step 6: Check Jenkins Logs

### View Detailed Logs:
1. Go to your failed build
2. Click **Console Output**
3. Look for the **first error** (not just the stack trace)
4. Common first errors:
   - `Tool 'Maven 3.9.9' not found`
   - `Tool 'JDK 11' not found`
   - `No such DSL method 'publishHTML'`

### Check Jenkins System Logs:
1. **Manage Jenkins** → **System Log**
2. Look for errors around the time of your build

## 🎯 Step 7: Progressive Testing

### Test in This Order:
1. **Basic Pipeline** - Just echo statements
2. **Tool Check** - Add java/maven version checks
3. **Git Checkout** - Add source code checkout
4. **Simple Build** - Add `mvn compile`
5. **Add Tests** - Add `mvn test`
6. **Add Coverage** - Add JaCoCo reporting

### Stop at the First Failure:
- If step 1 fails → Plugin issue
- If step 2 fails → Tool configuration issue
- If step 3 fails → Git/SCM issue
- If step 4 fails → Maven/build issue
- If step 5 fails → Test configuration issue
- If step 6 fails → Plugin/reporting issue

## 🆘 Quick Fixes Summary

### Most Common Solutions:
1. **Install HTML Publisher Plugin**
2. **Fix Maven tool configuration**
3. **Fix JDK tool configuration**
4. **Remove publishHTML temporarily**
5. **Use absolute paths for tools**

### Emergency Workaround:
Remove these sections from your Jenkinsfile temporarily:
- `tools { ... }`
- `publishHTML([...])`
- `junit '**/target/surefire-reports/*.xml'`

## 🎉 Success Indicators

### Your pipeline is fixed when you see:
- ✅ All stages complete with green checkmarks
- ✅ No red error messages in console output
- ✅ Build artifacts are created
- ✅ Test results are published (if tests exist)

## 📞 Still Need Help?

### Provide This Information:
1. **Jenkins Version**: Found in bottom-right corner
2. **Console Output**: Copy the first 50 lines of error
3. **Plugin List**: Manage Jenkins → Manage Plugins → Installed
4. **Tool Configuration**: Screenshots of Global Tool Configuration

### Test Command:
Run this in your terminal to verify tools work outside Jenkins:
```bash
cd /path/to/your/weather-api
java -version
mvn -version
mvn clean compile
```

If this works but Jenkins fails, it's definitely a Jenkins configuration issue! 