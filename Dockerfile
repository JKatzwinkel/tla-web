FROM gradle:8.6.0-jdk21 AS build

COPY --chown=gradle:gradle . /home/gradle/tla-frontend
WORKDIR /home/gradle/tla-frontend
RUN gradle installAssets bootJar --no-daemon


FROM openjdk:23-jdk-slim-bookworm

RUN mkdir /app; apt-get update && apt-get install -y fontconfig \
  && apt-get clean
WORKDIR /app

COPY --from=build /home/gradle/tla-frontend/build/libs/*.jar /app/tla-web-frontend.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/tla-web-frontend.jar"]
