FROM openjdk:8-jdk-alpine

EXPOSE 8080

ARG JAR_FILE=target/proyectoSoap-0.0.1-SNAPSHOT.jar

ADD ${JAR_FILE} proyectoSoap.jar

CMD ["java","-jar","/proyectoSoap.jar"]
