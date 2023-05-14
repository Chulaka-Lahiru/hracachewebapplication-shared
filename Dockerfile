FROM openjdk:8
ARG JAR_FILE=hra-cache-web-app-0.0.1-SNAPSHOT.jar
COPY ./target/${JAR_FILE} /deployments/app.jar
#COPY ./src/main/resources/application.yml /deployments
#COPY ./src/main/resources/logback-spring.xml /deployments
#WORKDIR /deployments
#CMD ["java", "-jar", "app.jar"]
#WORKDIR /..
RUN mvn clean install
RUN java -jar ./deployments/app.jar
ENTRYPOINT ["java", "-jar", "/deployments/app.jar"]

##REDIS
#"c:\Program Files\Redis\redis-server.exe"

##COMMANDS
#mvn clean install
#java -jar ./target/hra-cache-web-app-0.0.1-SNAPSHOT.jar