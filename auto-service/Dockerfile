## Stage 1: Build mit Maven im Container
FROM maven:3.9.5-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY auto-service/pom.xml .
COPY auto-service/src ./src

# Maven Build durchführen
RUN mvn package -DskipTests

## Stage 2: Runtime-Image erstellen
FROM registry.access.redhat.com/ubi8/openjdk-17:1.16

ENV LANGUAGE='en_US:en'

# Kopiere die fertige Anwendung aus dem Build-Stage
COPY --from=build --chown=185 /app/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /app/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /app/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /app/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]