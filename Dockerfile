FROM gradle:7.3.3-jdk11 AS build

COPY --chown=gradle:gradle . /home/gradle/tla-frontend
WORKDIR /home/gradle/tla-frontend
RUN gradle installAssets bootJar --no-daemon


FROM openjdk:11-jre-slim

RUN mkdir /app; apt-get update && apt-get install -y fontconfig libfreetype6
WORKDIR /app

COPY --from=build /home/gradle/tla-frontend/build/libs/*.jar /app/tla-web-frontend.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/tla-web-frontend.jar"]
