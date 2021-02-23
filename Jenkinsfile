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
            archiveArtifacts artifacts: '**/*.apk', fingerprint: true, onlyIfSuccessful: true            
        }
    }
}