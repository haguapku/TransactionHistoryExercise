class Constants {

    static final String MASTER_BRANCH = 'master'

    static final String QA_BUILD = 'Debug'
    static final String RELEASE_BUILD = 'Release'

    static final String INTERNAL_TRACK = 'internal'
    static final String RELEASE_TRACK = 'release'
}

def getBuildType() {
    switch (env.BRANCH_NAME) {
        case Constants.MASTER_BRANCH:
            return Constants.RELEASE_BUILD
        default:
            return Constants.QA_BUILD
    }
}

def getTrackType() {
    switch (env.BRANCH_NAME) {
        case Constants.MASTER_BRANCH:
            return Constants.RELEASE_TRACK
        default:
            return Constants.INTERNAL_TRACK
    }
}

def isDeployCandidate() {
    return ("${env.BRANCH_NAME}" =~ /(develop|master)/)
}

pipeline {
    agent any

    options {
        // Stop the build early in case of compile or test failures
        skipStagesAfterUnstable()
    }

    stages {
            stage('Clean Build') {
                  steps {
                      sh './gradlew clean'
                  }
            }

    stage('Compile') {
          steps {
            // Compile the app and its dependencies
            sh './gradlew compileDevDebugSources'
          }
        }

    stage('Unit test') {
          steps {
            // Compile and run the unit tests for the app and its dependencies
            sh './gradlew testDevDebugUnitTest'

            // Analyse the test results and update the build result as appropriate
            junit '**/TEST-*.xml'
          }
        }

    stage('Build APK') {
          steps {
            // Finish building and packaging the APK
            sh './gradlew assembleDebug'

            // Archive the APKs so that they can be downloaded from Jenkins
            archiveArtifacts artifacts: '**/*.apk', fingerprint: true, onlyIfSuccessful: true
          }
        }

    stage('Static analysis') {
              steps {
                // Run Lint and analyse the results
                sh './gradlew lintDebug'
                androidLint pattern: '**/lint-results-*.xml'
              }
            }
    }
}