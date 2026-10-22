FROM eclipse-temurin:25-jdk-alpine AS deps
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q

FROM deps AS test
COPY src/ src/
CMD ["./mvnw", "test", "-B"]

FROM deps AS build
COPY src/main/ src/main/
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
