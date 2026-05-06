pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        EC2_IP = '13.206.121.36'
        APP = 'flightbooking-1.0.jar'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean package'
            }
        }

        stage('Deploy') {
            steps {
                sshagent(['ec2-key']) {
                    bat """
                    scp -o StrictHostKeyChecking=no target\\%APP% ec2-user@%EC2_IP%:/home/ec2-user/

                    ssh -o StrictHostKeyChecking=no ec2-user@%EC2_IP% "pkill -f %APP% || true"

                    ssh -o StrictHostKeyChecking=no ec2-user@%EC2_IP% "nohup java -jar /home/ec2-user/%APP% > app.log 2>&1 &"
                    """
                }
            }
        }
    }
}