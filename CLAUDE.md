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

## Deployment
- Deploy is done with `./mvnw deploy` (runs `appengine:deploy appengine:deployCron`).
- Secrets must be set as env vars. `application.properties` is the single source of truth:
  its `${UPPER_SNAKE}` placeholders define the required secrets. At deploy time
  `scripts/generate-appengine-env.sh` generates `app.yaml`'s `env_variables` from them and
  fails fast if any is unset. In CI all GitHub secrets are exported to env automatically.
- Adding a secret: reference it in `application.properties` and add the GitHub secret — no
  `app.yaml`, workflow, or `pom.xml` change needed.
- IMPORTANT: Never run the deploy yourself — only the user deploys.

## Code Style Guidelines
- Language: Kotlin with SpringBoot
- Testing: Kotest with FreeSpec style and SpringMockK
- Imports: Organize by package; no wildcard imports
- Formatting: 4-space indentation; no trailing whitespace
- Error handling: Use Result pattern or exceptions with meaningful messages
