![build](https://github.com/JKatzwinkel/tla-web/workflows/build/badge.svg)
![deploy](https://github.com/JKatzwinkel/tla-web/workflows/deploy/badge.svg)
![LINE](https://img.shields.io/badge/line--coverage-91.82%25-brightgreen.svg)
![METHOD](https://img.shields.io/badge/method--coverage-85.69%25-brightgreen.svg)

TLA web frontend.

Copyright (C) 2019-2021 Berlin-Brandenburgische Akademie der Wissenschaften


## Usage

This Thesaurus Linguae Aegyptiae (TLA) web frontend requires an instance of the
corresponding [TLA backend application](https://github.com/thesaurus-linguae-aegyptiae/tla-es)
in order to work. You can either retrieve the TLA backend from github and run it yourself,
or you can utilize the [Docker Compose Setup](docker-compose.yml) shipped with this TLA frontend
source code repository, which contains services for population and execution of the TLA backend.


### Docker Compose Services

The Docker Compose setup configuration file [`docker-compose.yml`](docker-compose.yml) coming with
this TLA frontend source code repository defines 4 services, the first 3 of which constitute the
TLA backend:

- `es`: An Elasticsearch node.
- `populate`: A temporary instance of the TLA backend application, used to retrieve, unpack, and index the contents of a TLA corpus data file.
- `backend`: The TLA backend application instance to which the TLA frontend application will actually connect.

All of these services come as individual Docker containers running processes that can be characterized as follows.

The TLA backend relies on an Elasticsearch node running in the background for querying and retrieving raw TLA corpus data.
Elasticsearch is a search engine with built-in support for various natural languages, which is necessary for efficient
searches inside the TLA's text corpus und vocabularies. The Elasticsearch node, provided by the `es` service
defined in the Docker Compose setup inside this repository, need to be filled with TLA corpus data. This is the purpose
of the `populate` service. It downloads the required TLA data and starts up a temporary instance of the TLA backend
application, which in turn creates and configures the various Elasticsearch indices needed, stores the downloaded
TLA data in them, and then shuts itself down again after completion of these tasks. The `backend` service, a second
instance of the TLA backend application, then takes over and waits for the TLA frontend application (or any HTTP client
for that matter) to connect.

The 4th service defined in [`docker-compose.yml`](docker-compose.yml) provides a container running the actual TLA
frontend application itself:

- `frontend`: An instance of the TLA frontend application implemented by this very source code repository.

> Whether you make use of this containerized execution of the TLA frontend application is up to you. The alternative
> mode of execution using Gradle is being discussed further below.


#### Running the Docker Compose setup

As the `populate` service is needed to be able retrieve a TLA corpus data file in order for the
`backend` service to actually work, an assignment to the environment variable `SAMPLE_URL` is mandatory.
The value assigned to the environment variable `SAMPLE_URL` should be a URL pointing to a `.tar.gz` file
containing preferably recent TLA corpus data serialized to JSON files using a version of the TLA format
compatible to both the version of the TLA backend application, and the
[TLA model DTO library](https://github.com/thesaurus-linguae-aegyptiae/tla-common) used by this TLA frontend
application for communication with the TLA backend.

An example for the assignment of the `SAMPLE_URL` environment variable with an appropriate data source for the
TLA frontend version at hand would be:

```bash
export SAMPLE_URL=http://aaew64.bbaw.de/resources/tla-data/tla-sample-20210115-1000t.tar.gz
```

Alternatively, you can create a file named `.env` within the same local folder that contains this
`README.md`, and insert the following line:

```ini
SAMPLE_URL=http://aaew64.bbaw.de/resources/tla-data/tla-sample-20210115-1000t.tar.gz
```

Having set the `SAMPLE_URL` variable one way or another, you are ready to use Docker Compose for running either
all of the containers defined (including the TLA frontend), or all those required to get a TLA backend up and running
on your local machine.

For building and executing the entire stack, *including the TLA frontend*, run this command in your terminal:

```bash
docker-compose up --build --force-recreate -d
```

For building and executing only the TLA backend stack, run the following. You will learn how to run the TLA frontend
seperately and outside of the containerized setup further down below.

```bash
docker-compose up --build --force-recreate -d backend populate
```

It will take some time for Docker Compose to build and start the services required, and some additional time for
the `populate` service (running inside the `tla-ingest` container) to download, unpack, and store the TLA data
located at the URL specified via the `SAMPLE_URL` environment variable. In the meantime, you can check on the
`tla-ingest` container running the `populate` service with the following command:

```bash
docker-compose ps
```

If it takes the `tla-ingest` container longer than you anticipated to exit, you can monitor its progress by checking
the amount of data already uploaded into the Elasticsearch container by opening the `_cat/indices` endpoint of
Elasticsearch's HTTP API in a web browser:

    http://localhost:9200/_cat/indices

Once the populating phase of the backend setup is complete, you can check whether the TLA backend is available by
sending a request (for example by using your browser) to its thesaurus entry details endpoint:

    http://localhost:8090/ths/get/7pupjz

If the response contains a JSON representation of the TLA thesaurus term `"Pianchi/Pije Usermaatre"` instead of
an error message indicating a 404 HTTP status code or worse, the TLA backend is running and has been properly
populated with data.


### Run TLA frontend app using Gradle

- Requires Java 11

Provided a properly running TLA backend application is available, the TLA frontend application can be executed
using the included [build file](build.gradle). This requires a Java JDK version 11 or higher.

This repository comes with Gradle wrappers for both Unix (`gradlew`) and Windows (`gradlew.bat`). Use the Gradle wrapper
appropriate for your platform in order to execute build tasks `install` defined in the build file and `bootRun`
from the Spring Boot Gradle plugin:

    ./gradlew install bootrun --refresh-dependencies

or 

    ./gradlew.bat install bootrun --refresh-dependencies

The `bootRun` task ist configured to start up the application in `dev` mode, which disables static asset and template caches.
This means that modifications to file in the `templates` and `static` subdirectories of `src/main/resources` are being
reflected in the application's web interface without reboot.


## Configuration

For config options and further methods of execution check out the [Dockerfile](Dockerfile),
the [Docker Compose file](docker-compose.yml), the [env var template file](.env.template), the
[Application Properties file](src/main/resources/application.yml), and the [build file](build.gradle).


<!--- vim: set ts=2 sw=2 tw=100 noet ft=markdown : -->
