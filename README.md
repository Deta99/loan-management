# Loan Management System

A secure REST API for loan management with JWT authentication and role-based access control.

## Architecture
- Customer registers and logs in via JWT
- USER role can apply for loans and make payments
- ADMIN role can approve or reject loans
- Kafka event published when a loan is approved

## Tech Stack
Java 17 | Spring Boot 3 | Spring Security | JWT | PostgreSQL | Apache Kafka | Docker | GitLab CI/CD | JUnit 5 | Mockito | Swagger

## Running locally
```bash
docker compose up -d
./mvnw spring-boot:run
```

## API Documentation
http://localhost:8082/swagger-ui/index.html