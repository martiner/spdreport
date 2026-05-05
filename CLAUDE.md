# CLAUDE.md

## Infrastructure
- Runs on **Google App Engine Standard**
- Data is stored in **Google Cloud Datastore** (accessed via Objectify)
- Never attempt to deploy the application — deployment is handled push-to-deploy

## Build Commands
- Run application: `./mvnw docker:start spring-boot:run` (starts Datastore emulator via Docker, then the app)
- Run unit tests (`*Test.kt`): `./mvnw test`
- Run single test: `./mvnw test -Dtest=TestClassName`
- Run integration tests (`*IT.kt`): `./mvnw verify` (also starts the Docker container with Datastore emulator)
- Update dependency lock after each dependency change: `./mvnw dependency-lock:lock`

## Code Style Guidelines
- Language: Kotlin with SpringBoot
- Testing: Kotest with FreeSpec style and SpringMockK
- Imports: Organize by package; no wildcard imports
- Formatting: 4-space indentation; no trailing whitespace
- Error handling: Use Result pattern or exceptions with meaningful messages
