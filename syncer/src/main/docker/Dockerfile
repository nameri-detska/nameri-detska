FROM maven:3-amazoncorretto-25 AS build
WORKDIR /app
COPY pom.xml .
COPY .formatter .formatter
COPY syncer/pom.xml syncer/pom.xml
RUN mvn -f syncer/pom.xml dependency:go-offline -B
COPY syncer/src syncer/src
COPY syncer/certs syncer/certs
RUN mvn -f syncer/pom.xml package -DskipTests -B

FROM sapmachine:25-jre-alpine
WORKDIR /app

COPY --from=build /app/syncer/target/*-runner.jar /app/syncer.jar
COPY --from=build /app/syncer/certs /app/certs

ENTRYPOINT ["java", "-jar", "/app/syncer.jar"]
