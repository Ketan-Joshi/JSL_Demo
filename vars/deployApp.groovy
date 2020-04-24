def call() {
    sh "docker ps -a"
    sh "docker run -tid -p \${HOST_PORT}:\${CONTAINER_PORT} --name \${APP_NAME} \${APP_NAME}:\${BUILD_NUMBER}"
    sh 'echo "App URL: http://localhost:\${HOST_PORT}"'
}