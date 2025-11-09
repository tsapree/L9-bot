# pre-fetch dependencies
FROM maven:3.8.5-openjdk-17 AS DEPENDENCIES
WORKDIR /opt/app
COPY l9-bot-app/pom.xml l9-bot-app/pom.xml
COPY l9-lib/pom.xml l9-lib/pom.xml
COPY pom.xml .
RUN mvn -B -e org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline # -DexcludeArtifactIds=l9-lib

# build the jar
FROM maven:3.8.5-openjdk-17 AS BUILDER
WORKDIR /opt/app
COPY --from=DEPENDENCIES /root/.m2 /root/.m2
COPY --from=DEPENDENCIES /opt/app/ /opt/app
COPY l9-bot-app/src /opt/app/l9-bot-app/src
COPY l9-lib/src /opt/app/l9-lib/src

RUN mvn -B -e clean install -DskipTests # -o

# prepeare runtime env
FROM eclipse-temurin:17-jre-alpine

RUN mkdir -p /l9bot/tmp/cache

WORKDIR /opt/app
COPY --from=BUILDER /opt/app/l9-bot-app/target/*.jar l9bot.jar
COPY docker-entrypoint.sh /docker-entrypoint.sh

EXPOSE 8080

RUN chmod +x /docker-entrypoint.sh
ENTRYPOINT ["/bin/sh", "-c", "/docker-entrypoint.sh"]