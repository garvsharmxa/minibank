#!/bin/bash
SERVICES=("authService" "AccountService" "CustomerService" "CardService" "TransactionService" "KycService" "ApiGateway" "ServiceRegistry")
for SERVICE in "${SERVICES[@]}"
do
    cat << 'DOCKERFILE' > "$SERVICE/Dockerfile"
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
DOCKERFILE
done
