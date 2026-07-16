# ── Build stage ───────────────────────────────────────────────────────────────
FROM maven:3.9-amazoncorretto-17 AS build
WORKDIR /build

# Camada de dependências separada: só invalida quando o pom.xml muda
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
# Testes e gates de qualidade rodam no pre-commit e no CI, não no build da imagem
RUN mvn -q package -DskipTests -Dspotless.check.skip=true -Djacoco.skip=true

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM amazoncorretto:17-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY --from=build /build/target/bank-simulator-*.jar app.jar

USER app
EXPOSE 5000
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
