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

        stage('Checkout Code') {
            steps {
                git 'https://github.com/your-username/your-repo.git'
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean package'
            }
        }


        stage('Clean Workspace') {
    steps {
        deleteDir()
    }
}

        stage('Deploy to EC2') {
            steps {
                withCredentials([sshUserPrivateKey(credentialsId: 'ec2-key', keyFileVariable: 'KEY')]) {

                    bat """
                    scp -o StrictHostKeyChecking=no -i %KEY% target\\%APP_NAME% ec2-user@%EC2_IP%:/home/ec2-user/

                    ssh -o StrictHostKeyChecking=no -i %KEY% ec2-user@%EC2_IP% "pkill -f %APP_NAME% || true"

                    ssh -o StrictHostKeyChecking=no -i %KEY% ec2-user@%EC2_IP% "nohup java -jar /home/ec2-user/%APP_NAME% > app.log 2>&1 &"
                    """
                }
            }
        }
    }
}