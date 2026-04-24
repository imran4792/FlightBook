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

        stage('Build') {
            steps {
                bat 'mvn clean install'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn test'
            }
        }

        stage('Package') {
            steps {
                bat 'mvn package'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Stopping old container (if running)...'
                bat 'docker stop flightbooking-container || exit 0'
                bat 'docker rm flightbooking-container || exit 0'

                echo 'Running new container...'
                bat 'docker run -d -p 8080:8080 --name flightbooking-container flightbooking-app'
            }
        }
    }
}