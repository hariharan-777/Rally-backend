# Auth service

Spring Boot service for account registration, password login, and signed JWT validation.

Run it with Java 21 and Maven 3.9+:

```powershell
mvn spring-boot:run
```

Set `JWT_SECRET` to a unique secret of at least 32 bytes in any non-development environment. The default H2 database is in memory; configure `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `DATABASE_DRIVER` for PostgreSQL or another JDBC database.

| Endpoint | Request | Result |
| --- | --- | --- |
| `POST /auth/register` | `{ "email": "member@example.com", "password": "password1" }` | `201` and an access token |
| `POST /auth/login` | `{ "email": "member@example.com", "password": "password1" }` | `200` and an access token |
| `POST /auth/validate` | `Authorization: Bearer <token>` | token validity, email, and role |

Passwords are BCrypt-hashed and emails are normalized to lowercase before storage and lookup.
