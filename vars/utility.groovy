def createDockerfile(){
    if (fileExists('build.gradle')){
        println('Identified as Gradle prject...')
        def dockerfile = libraryResource 'Dockerfile.gradle'
        if (fileExists('Dockerfile')){
            println('\tDockerfile exists not creating...')
        }
        else{
            writeFile file: 'Dockerfile', text: dockerfile
        }
    }
}

def createSonarFile(){
    if (fileExists('sonar.gradle')){
        println('Identified as Gradle prject...')
        def sonar = libraryResource 'sonar.gradle'
        if (fileExists('sonar.properties')){
            println('\tsonar.properties exists not creating...')
        }
        else{
            writeFile file: 'sonar.properties', text: sonar
        }
    }
}