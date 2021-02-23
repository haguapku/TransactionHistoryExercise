def err = null
try {
  
    node {
        
        stage('Clean Build') {
                sh './gradlew clean'
        }
        
        stage('Build release ') {

                    sh './gradlew assembleDevDebug'

        }
      
        stage('Compile') {
            archiveArtifacts artifacts: '**/*.apk', fingerprint: true, onlyIfSuccessful: true            
        }
    }
  
} catch (caughtError) { 
    
    err = caughtError
    currentBuild.result = "FAILURE"

} finally {
    
    if(currentBuild.result == "FAILURE"){
              sh "echo 'Build FAILURE'"
    }else{
         sh "echo 'Build SUCCESSFUL'"
    }
   
}