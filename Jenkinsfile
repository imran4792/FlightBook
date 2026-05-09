pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        EC2_USER = 'ec2-user'
        EC2_IP   = '3.6.130.208'
        APP      = 'flightbooking-1.0.jar'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Deploy to EC2') {
            steps {
                sshagent(['ec2-key']) {

                    // Copy JAR
                    bat 'scp -o StrictHostKeyChecking=no target\\%APP% %EC2_USER%@%EC2_IP%:/home/%EC2_USER%/'

                    // Stop old app
                    bat 'ssh -o StrictHostKeyChecking=no %EC2_USER%@%EC2_IP% "pkill -f %APP% || true"'

                    // Start new app
                    bat 'ssh -o StrictHostKeyChecking=no %EC2_USER%@%EC2_IP% "nohup java -jar /home/%EC2_USER%/%APP% > app.log 2>&1 &"'
                }
            }
        }

        stage('Check App') {
            steps {
                sshagent(['ec2-key']) {
                    bat 'ssh %EC2_USER%@%EC2_IP% "ps -ef | grep java"'
                }
            }
        }
    }

    post {
        success {
            echo '✅ Deployment successful!'
        }
        failure {
            echo '❌ Deployment failed! Check logs.'
        }
    }
}