# ── Stage 1: dependency cache ─────────────────────────────────────────────────
# Copying pom.xml before source means Docker reuses this layer on code-only changes,
# avoiding a full re-download of ~100 MB of dependencies on every build.
FROM maven:3.9-eclipse-temurin-25-alpine AS deps
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress -B

# ── Stage 2: compile & package ────────────────────────────────────────────────
FROM deps AS build
COPY src ./src
RUN mvn package -DskipTests --no-transfer-progress -B

# ── Stage 3: runtime ──────────────────────────────────────────────────────────
# eclipse-temurin:25-jre-alpine is ~140 MB vs ~530 MB for the full JDK image.
FROM eclipse-temurin:25-jre-alpine AS runtime

# Dedicated non-root user; Spring Boot does not need root.
RUN addgroup -S app && adduser -S -G app app

WORKDIR /app

COPY --from=build /build/target/ecommerce-*.jar ecom.jar

USER app

EXPOSE 8080

# Spring Boot Actuator exposes /actuator/health (included via spring-boot-starter-actuator).
# start-period gives Flyway time to run migrations before the first probe.
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

# UseContainerSupport — honours cgroup CPU/memory limits (on by default since JDK 10,
# made explicit here for clarity).
# MaxRAMPercentage — cap heap at 75 % of the container memory limit; leaves headroom
# for Metaspace, thread stacks, and off-heap Caffeine/Netty buffers.
# security.egd — faster SecureRandom seeding in containers (no /dev/random blocking).
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "ecom.jar"]
