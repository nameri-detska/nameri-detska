FROM quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-25 AS build
WORKDIR /code
COPY pom.xml .
COPY .formatter .formatter
COPY backend/pom.xml backend/pom.xml
RUN mvn -f backend/pom.xml dependency:go-offline -B
COPY backend/src backend/src
RUN mvn -f backend/pom.xml package -Pnative -DskipTests -B

FROM quay.io/quarkus/ubi9-quarkus-micro-image:2.0
WORKDIR /work/
COPY --from=build /code/backend/target/*-runner /work/application
RUN chmod 775 /work /work/application
EXPOSE 8080
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]
