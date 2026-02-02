pipeline {
    agent any

    triggers {
        githubPush()
    }

    environment {
        DOCKER_REPO = "nguyenminh1301/per"
        DEPLOY_DIR = "/home/per"
        DOTENV = credentials('per-dotenv')
    }

    stages {
        stage('Extract Version') {
            steps {
                script {
                    def pom = readMavenPom file: 'pom.xml'
                    env.APP_VERSION = pom.version
                    echo "App version detection: ${env.APP_VERSION}"
                }
            }
        }

        stage('Docker Build & Tag') {
            steps {
                echo "Building an image with the tag: ${env.APP_VERSION} and latest..."
                sh "docker build . -t ${DOCKER_REPO}:${env.APP_VERSION} -t ${DOCKER_REPO}:latest"
            }
        }

        stage('Docker Push') {
            steps {
                echo "Pushing images to Docker Hub..."
                sh "docker push ${DOCKER_REPO}:${env.APP_VERSION}"
                sh "docker push ${DOCKER_REPO}:latest"
            }
        }

        stage('Deploy to VPS') {
            steps {
                dir("${env.DEPLOY_DIR}") {
                    echo "Deploying at ${env.DEPLOY_DIR}..."

                    sh "cp ${DOTENV} .env"

                    sh "docker compose down -v"

                    sh "docker rmi ${DOCKER_REPO}:latest || true"

                    sh "docker compose up -d"
                }
            }
        }
    }

    post {
        success {
            echo "Workflow success!"
        }
        failure {
            echo "Workflow fail. Check logs."
        }
    }
}