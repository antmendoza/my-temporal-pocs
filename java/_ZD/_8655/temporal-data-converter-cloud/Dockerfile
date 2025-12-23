FROM openjdk:17
MAINTAINER antmendoza
EXPOSE 8888
COPY target/libs/*.jar /libs/
COPY target/app.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]