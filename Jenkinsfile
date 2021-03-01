pipeline {
    agent any

    stages {
        stage('Clean Build') {
              steps {
                  sh './gradlew clean'
              }
        }

        stage('Run Tests') {
              steps {
                  echo 'Running Tests'
                       script {
                            VARIANT = getBuildType()
                            sh "./gradlew test${VARIANT}UnitTest"
                       }
              }
        }
        
        stage('Build Debug') {
              steps {
                  sh './gradlew assembleDebug'
              }
            }
      
        stage('Compile') {
            steps {
                  archiveArtifacts artifacts: '**/*.apk', fingerprint: true, onlyIfSuccessful: true
            }
        }
    }
}