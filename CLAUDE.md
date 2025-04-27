# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands
- Run application: `./mvnw docker:start spring-boot:run`
- Run tests: `./mvnw test`
- Run single test: `./mvnw test -Dtest=TestClassName`
- Run integration tests: `./mvnw verify`
- Update dependency lock: `./mvnw dependency-lock:lock`

## Code Style Guidelines
- Language: Kotlin with SpringBoot
- Testing: Kotest with FreeSpec style and SpringMockK
- Imports: Organize by package; no wildcard imports
- Formatting: 4-space indentation; no trailing whitespace
- Types: Use nullable types with `?` for optional values
- Naming: camelCase for variables/functions, PascalCase for classes
- Error handling: Use Result pattern or exceptions with meaningful messages
- Collections: Prefer immutable collections and functional operations
