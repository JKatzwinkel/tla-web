FROM gradle:9.2-jdk21 AS build

COPY --chown=gradle:gradle . /home/gradle/tla-frontend
WORKDIR /home/gradle/tla-frontend
RUN gradle installAssets bootJar --no-daemon


FROM openjdk:25-jdk-slim-bookworm

RUN apt-get update \
  && apt-get install -y --no-install-recommends fontconfig=2.14.1-4 \
  && apt-get clean && rm -r /var/lib/apt/lists/* \
  && mkdir /app

WORKDIR /app

COPY --from=build /home/gradle/tla-frontend/build/libs/*.jar /app/tla-web-frontend.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/tla-web-frontend.jar"]
