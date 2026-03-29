# --- First Stage: Build ---
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

RUN apk add --no-cache maven


COPY pom.xml .
RUN mvn dependency:go-offline -B


COPY src ./src
RUN mvn clean package -DskipTests

# --- Second Stage: Runtime ---
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app


RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring


COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]