pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    stages {

        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                bat 'mvn clean install'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Stopping old app (if running)...'
                bat 'taskkill /F /IM java.exe || exit 0'

                echo 'Starting new app...'
                bat 'for %%i in (target\\*.jar) do start /B java -jar %%i'
            }
        }
    }
}