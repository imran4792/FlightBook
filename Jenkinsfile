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
                echo 'Stopping old app on port 8080 (if running)...'
                bat '''
                for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080') do (
                    taskkill /F /PID %%a
                )
                '''
                echo 'Starting new app...'
                bat 'start /B java -jar target\\flightbooking-1.0.jar'
            }
        }
    }
}