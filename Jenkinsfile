pipeline {
    agent any

    stages {
        stage('Clean Build') {
              steps {
                  sh './gradlew clean'
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