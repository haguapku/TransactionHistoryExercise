pipeline {
    agent any

    stages {
        stage('Clean Build') {
              steps {
                  sh 'gradle clean'
              }
        }
        
        stage('Build Debug') {
              steps {
                  sh 'gradle assembleDebug'
              }
            }
      
        stage('Compile') {
            steps {
                  archiveArtifacts artifacts: '**/*.apk', fingerprint: true, onlyIfSuccessful: true
            }
        }
    }
}