# S PD Report

## Development

### Dependency locking

Dependencies are checked against `dependencies-lock.json` during `validate` phase.
Rewrite lock using:
```shell
./mvnw dependency-lock:lock
```

### Setup

```shell
gcloud components install cloud-datastore-emulator
gcloud config set project spdreport
```

Put `spring.security.oauth2.client.registration.google.client-secret` into `application.properties`

### Run

```shell
gcloud beta emulators datastore start --host-port=localhost:8484
./mvnw spring-boot:run
```
http://localhost:8080


### Deploy

```shell
mvn package appengine:deploy
```
