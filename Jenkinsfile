pipeline {
    agent any

    triggers {
        githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: 'github-ssh-key',
                    url: 'git@github.com:NguyenMinh1301/Per.git'
            }
        }

        stage('Build') {
            steps {
                echo 'Building application...'
                // Wait
            }
        }

        stage('Test') {
            steps {
                echo 'Testing...'
                // Wait
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying to VPS...'
                // Wait
            }
        }
    }

    post {
        success {
            echo 'Build success!'
        }
        failure {
            echo 'Build fail. Check logs'
        }
    }
}