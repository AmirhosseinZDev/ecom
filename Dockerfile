FROM maven:3-eclipse-temurin-26-alpine AS build-env

WORKDIR /app
COPY . .

ARG MAVEN_OPTS

RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:25-jre-alpine AS runtime

ENV TZ=Asia/Tehran

WORKDIR /usr/local/app/

COPY --from=build-env /app/ecommerce-api/target/*.jar /usr/local/app/

RUN mv /usr/local/app/ecommerce-api-*.jar /usr/local/app/ecom.jar

EXPOSE 8080

CMD ["sh", "-c", "java ${JVM_OPTS} -jar /usr/local/app/ecom.jar"]