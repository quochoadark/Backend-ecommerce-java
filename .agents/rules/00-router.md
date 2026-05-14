# AI Context Router

This is the global router for the AI Agent (Antigravity/Cursor/Windsurf). 
You MUST follow these routing rules when analyzing requirements or writing code to ensure you load the correct Context-Specific Skills.

## 1. Global Rules (Always Applied)
The files in this directory (`.agents/rules/`) are global. You must ALWAYS abide by them.
- `architect.md` & `code-architect.md`: Global system design, architecture, and performance guidelines.
- `coding-style.md`: Code formatting, naming conventions, and modern Java features usage.
- `security.md`: Hard rules on security.
- `testing.md`: Hard rules on testing approaches.
- `patterns.md`: Design patterns to be used across the project.

## 2. Context-Specific Skills Routing
Depending on the task at hand, you MUST proactively read the specific guidelines in `.agents/skills/`:

- **Docker / Infrastructure**: 
  -> IF the task involves Docker, Docker Compose, or containerization: HÃY ĐỌC `.agents/skills/docker-patterns/SKILL.md`.
- **Database / Migrations**: 
  -> IF the task involves creating or modifying database entities, or Flyway/Liquibase migrations: HÃY ĐỌC `.agents/skills/database-migrations/`.
- **Java Coding Standards**: 
  -> IF writing or refactoring Java code: HÃY ĐỌC `.agents/skills/java-coding-standards/`.
- **Spring Boot Components**: 
  -> IF working on Spring Boot Controllers, Services, Repositories, or Security: HÃY ĐỌC các pattern trong `.agents/skills/springboot-patterns/`, `.agents/skills/springboot-security/`, `.agents/skills/springboot-tdd/`, hoặc `.agents/skills/springboot-verification/`.
- **Architecture & Domain Design**: 
  -> IF designing a new module or complex business logic: HÃY ĐỌC `.agents/skills/hexagonal-architecture/` và xem các quyết định trước đó ở `.agents/skills/architecture-decision-records/`.
