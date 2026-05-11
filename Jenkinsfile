pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        EC2_USER = 'ec2-user'
        EC2_IP   = '3.6.130.208'
        APP      = 'flightbooking-1.0.jar'
        KEY_PATH = 'C:\\ProgramData\\Jenkins\\.ssh\\mykey.ppk'
        PUTTY    = '"C:\\Program Files\\PuTTY\\'
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
                ${PUTTY}pscp.exe" -batch -i %KEY_PATH% %WORKSPACE%\\target\\%APP% %EC2_USER%@%EC2_IP%:/home/%EC2_USER%/

                echo ===== STOPPING OLD APP =====
                ${PUTTY}plink.exe" -batch -i %KEY_PATH% %EC2_USER%@%EC2_IP% "pkill -f %APP% || true"

                echo ===== STARTING NEW APP =====
                ${PUTTY}plink.exe" -batch -i %KEY_PATH% %EC2_USER%@%EC2_IP% "nohup java -jar /home/%EC2_USER%/%APP% > app.log 2>&1 &"
                """
            }
        }

        stage('Check App') {
            steps {
                bat """
                echo ===== VERIFYING APP =====
                ${PUTTY}plink.exe" -batch -i %KEY_PATH% %EC2_USER%@%EC2_IP% "ps -ef | grep java"
                """
            }
        }
    }

    post {
        success {
            echo '✅ Deployment successful! App is live.'
        }
        failure {
            echo '❌ Deployment failed! Check logs.'
        }
    }
}