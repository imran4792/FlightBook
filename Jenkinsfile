pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        EC2_IP = '13.206.121.36'
        APP_NAME = 'flightbooking-1.0.jar'
    }

    stages {

        stage('Clean Workspace') {
            steps {
                deleteDir()
            }
        }

        stage('Checkout Code') {
            steps {
                git branch: 'main',
                    credentialsId: 'github-creds',
                    url: 'https://github.com/imran4792/FlightBook.git'
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean package'
            }
        }

        stage('Deploy to EC2') {
            steps {
                withCredentials([sshUserPrivateKey(credentialsId: 'ec2-key', keyFileVariable: 'KEY')]) {

                    bat """
                    scp -o StrictHostKeyChecking=no -i %KEY% target\\%APP_NAME% ubuntu@%EC2_IP%:/home/ubuntu/

                    ssh -o StrictHostKeyChecking=no -i %KEY% ubuntu@%EC2_IP% "pkill -f %APP_NAME% || true"

                    ssh -o StrictHostKeyChecking=no -i %KEY% ubuntu@%EC2_IP% "nohup java -jar /home/ubuntu/%APP_NAME% > app.log 2>&1 &"
                    """
                }
            }
        }
    }
}