# Etapa 1: compilar el WAR con Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# Etapa 2: Tomcat 10.1 (Jakarta EE 10), la app queda en la raiz "/"
FROM tomcat:10.1-jdk21-temurin
ENV TZ=America/Lima
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/semana6-1.0.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
