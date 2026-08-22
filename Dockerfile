FROM eclipse-temurin:25-jre

WORKDIR /app

RUN useradd --system --uid 1001 --no-create-home bloomly

COPY target/bloomly-0.0.1.jar app.jar

USER bloomly

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
