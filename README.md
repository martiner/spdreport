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
spring.security.oauth2.client.registration.pagerduty.client-secret=
spring.mail.password=
```

### Run

```shell
./mvnw docker:start spring-boot:run
```

This will start
* the application on port `8080` http://localhost:8080
* the Datastore emulator on port `8484` http://localhost:8484
* the Datastore viewer on port `8000` http://localhost:8000

### Manual Deploy

Environment variables with secrets must be set (otherwise the deploy fails fast).
The required secrets are the `${UPPER_SNAKE}` placeholders in `application.properties`; at
deploy time they are generated into `app.yaml`. In CI they come from GitHub secrets of the
same name.

```shell
export ..._SECRET=...
./mvnw deploy
```
