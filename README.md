# S PD Report

## Development

### Dependency locking

Dependencies are checked against `dependencies-lock.json` during `validate` phase.
Rewrite lock using:
```shell
./mvnw dependency-lock:lock
```

### Setup

Update `application.properties` with
```
spring.security.oauth2.client.registration.google.client-secret=
spring.mail.password=
```

### Run

```shell
./mvnw docker:start spring-boot:run
```
http://localhost:8080


### Deploy

```shell
./mvnw package appengine:deploy appengine:deployCron
```
