FROM openjdk:8-jdk-slim
ENV PORT 8000 
EXPOSE 8000
COPY build/libs/*.jar /opt/app.jar
WORKDIR /opt
CMD ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]
