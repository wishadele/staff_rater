## Overview
**Rate CS Teaching Staff** is a CRUD web app built with Java 17, Spring Boot, Thymeleaf and PostgreSQL. Users can list, view, add, edit and delete ratings for teaching staff (TAs, professors, instructors and staff), with each entry scored 1-10 for clarity, niceness and knowledgeability. The index page shows each person's role and an overall score, which is the average of the three. Forms are validated on the server, with a message under each bad field: name required, valid email, scores between 1 and 10, and a comment of at most 500 characters. Each email can only be used once, because an entry represents one staff member. Deleting asks for confirmation first.

For design, an abstract `StaffMemberProfile` class with a `displayTitle()` method is implemented by separate TA, professor, instructor and staff classes. The pages call it without caring which role they have, so a TA shows as "TA Sam" and a professor as "Prof. Sam". Data is stored in PostgreSQL, with credentials supplied through environment variables, and a test suite covers validation, the web layer and persistence. The app is deployed on Render with a Render Postgres database.

## How to run locally

1. Install Java 17, Maven and PostgreSQL.
2. In pgAdmin, create a database named `staffrating`.
3. In the terminal, set your local Postgres password (PowerShell):
   `$env:DB_PASSWORD = "yourpassword"`
4. Run `mvn spring-boot:run`
5. Open http://localhost:8080

Defaults: `jdbc:postgresql://localhost:5432/staffrating`, user `postgres`. Override with the `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` environment variables. The table is created automatically.

## How to deploy (Render)

1. On Render, create a PostgreSQL database.
2. Create a Web Service from this GitHub repo. Set the runtime to **Docker** and the region to match the database.
3. Under Environment, add (copy values from the database's Connections section, and never commit them):
   - `DB_URL` = `jdbc:postgresql://<internal-hostname>:5432/<database-name>`
   - `DB_USERNAME` = the database username
   - `DB_PASSWORD` = the database password
4. Deploy. Every push to `main` redeploys automatically.