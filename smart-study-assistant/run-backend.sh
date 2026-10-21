#!/usr/bin/env bash
# Starts the Spring Boot backend (needs JDK 17+ and Maven, or the ./mvnw wrapper if you add one).
cd "$(dirname "$0")"
if [ -x ./mvnw ]; then ./mvnw spring-boot:run; else mvn spring-boot:run; fi
