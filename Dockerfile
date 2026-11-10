# Etapa 1: compilar el WAR con Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# Etapa 2: WildFly 39 (Jakarta EE 10, Faces 4.0) con JDK 21; la app queda en la raiz "/"
FROM quay.io/wildfly/wildfly:39.0.1.Final-2-jdk21
ENV TZ=America/Lima
# Detras del proxy HTTPS: respeta X-Forwarded-* para que las redirecciones salgan con https
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --commands="embed-server,/subsystem=undertow/server=default-server/http-listener=default:write-attribute(name=proxy-address-forwarding,value=true),stop-embedded-server" && rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history
COPY --from=build /app/target/app.war /opt/jboss/wildfly/standalone/deployments/app.war
EXPOSE 8080
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]
