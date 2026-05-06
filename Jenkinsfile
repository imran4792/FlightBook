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

       stage('Deploy') {
    steps {
        withCredentials([sshUserPrivateKey(credentialsId: 'ec2-key', keyFileVariable: 'KEY')]) {

            bat """
            scp -o StrictHostKeyChecking=no -i %KEY% target\\flightbooking-1.0.jar ec2-user@13.206.121.36:/home/ec2-user/

            ssh -o StrictHostKeyChecking=no -i %KEY% ec2-user@13.206.121.36 "pkill -f flightbooking-1.0.jar || true"

            ssh -o StrictHostKeyChecking=no -i %KEY% ec2-user@13.206.121.36 "nohup java -jar /home/ec2-user/flightbooking-1.0.jar > app.log 2>&1 &"
            """
        }
    }
}
    }
}