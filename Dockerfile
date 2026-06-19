FROM maven:3.9-eclipse-temurin-25-alpine AS build-env

WORKDIR /app
COPY . .

ARG MAVEN_OPTS

RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:25-jre-alpine AS runtime

ENV TZ=Asia/Tehran

WORKDIR /usr/local/app/

COPY --from=build-env /app/target/*.jar /usr/local/app/

RUN mv /usr/local/app/ecommerce-*.jar /usr/local/app/ecom.jar

EXPOSE 8080

CMD ["sh", "-c", "java ${JVM_OPTS} -jar /usr/local/app/ecom.jar"]