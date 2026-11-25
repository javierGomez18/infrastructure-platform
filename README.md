# 📘 Infrastructure Platform

![Status](https://img.shields.io/badge/status-active-brightgreen)
![Architecture](https://img.shields.io/badge/architecture-microservices-blue)
![Observability](https://img.shields.io/badge/observability-enabled-orange)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

The **Infrastructure Platform** is the foundational layer that provides all cross‑cutting services, shared components, and operational capabilities for the entire microservices ecosystem.  
It centralizes observability, auditing, metrics, shared libraries, and optional shared databases, ensuring consistency, maintainability, and professional architecture across all services.

This repository acts as the backbone of the platform.

---

## 🚀 Features

### ✔ Advanced Auditing Service (SQLite-based)
Lightweight, fast, and ideal for local development.  
All microservices can send audit events without increasing their own resource usage.

### ✔ Metrics & Prometheus Integration
A dedicated service for exposing platform-wide metrics and future observability integrations.

### ✔ Shared Libraries
Reusable components such as filters, wrappers, logging configuration, header validation, MDC utilities, and other cross-cutting modules.

### ✔ Optional Shared Database Layer
Centralized models, migrations, and repositories for microservices that rely on common data structures.

### ✔ Documentation & Architecture Diagrams
Clear documentation for platform structure, observability, and microservice interactions.

---

## 📦 Repository Structure

```plaintext
infrastructure-platform/
│
├── audit-service/              # SQLite-based auditing microservice
│   ├── src/main/java/...
│   ├── src/main/resources/...
│   └── audit.db
│
├── metrics-service/            # Prometheus metrics exporter
│   ├── src/main/java/...
│   └── src/main/resources/...
│
├── shared-database/            # Shared DB layer
│   ├── migrations/
│   ├── models/
│   ├── repositories/
│   └── config/
│
├── shared-libraries/           # Reusable components for all micros
│   ├── filters/
│   ├── utils/
│   ├── logging/
│   └── security/
│
└── docs/
    ├── architecture/
    ├── observability/
    └── readme/
