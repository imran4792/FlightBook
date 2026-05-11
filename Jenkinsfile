pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        EC2_USER = 'ec2-user'
        EC2_IP   = '3.6.130.208'
        DOMAIN   = 'https://flightbook.mooo.com'
        APP      = 'flightbooking-1.0.jar'
        KEY_PATH = 'C:\\ProgramData\\Jenkins\\.ssh\\mykey.pem'
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
                bat """
                echo ===== COPYING JAR TO EC2 =====
                scp -o StrictHostKeyChecking=no -i %KEY_PATH% %WORKSPACE%\\target\\%APP% %EC2_USER%@%EC2_IP%:/home/%EC2_USER%/

                echo ===== STOPPING OLD APP =====
                ssh -o StrictHostKeyChecking=no -i %KEY_PATH% %EC2_USER%@%EC2_IP% "pkill -f %APP% || true"

                echo ===== STARTING NEW APP =====
                ssh -o StrictHostKeyChecking=no -i %KEY_PATH% %EC2_USER%@%EC2_IP% "nohup java -jar /home/%EC2_USER%/%APP% > app.log 2>&1 &"
                """
            }
        }

        stage('Check App (Domain)') {
            steps {
                bat """
                echo ===== WAITING FOR APP START =====
                timeout /t 15

                echo ===== CHECKING DOMAIN =====
                curl -k %DOMAIN%
                """
            }
        }
    }

    post {
        success {
            echo '✅ Deployment successful! App is live at https://flightbook.mooo.com'
        }
        failure {
            echo '❌ Deployment failed! Check logs.'
        }
    }
}