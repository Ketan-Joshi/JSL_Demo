def call() {
    sh "docker build -t \${APP_NAME}:\${BUILD_NUMBER} ."
}
