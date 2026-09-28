# Spring Boot + Redis + Nexus Employee Assignment

This workspace contains two Maven projects in one reactor:

- `employee-library`: reusable, framework-independent employee domain model, built as `com.example:employee-library:1.0.0`.
- `employee-redis-app`: REST API that consumes the library, stores employees in file-backed H2, and caches reads in Redis.

The app caches individual reads for 30 seconds using `@Cacheable`, refreshes an employee entry with `@CachePut` after create/update, and evicts stale entries with `@CacheEvict` after updates/deletes. The employee list cache is invalidated on every write. The TTL is configurable as `app.cache.employee-ttl` (ISO-8601 duration).

## Requirements

- JDK 17 or later
- Maven 3.8+
- Docker Desktop with Compose

## Connect to Nexus and start Redis

Use the existing Nexus instance at <http://localhost:8081>. Confirm a hosted Maven repository named `maven-releases` exists; if it does not, create a **maven2 (hosted)** repository with **Version policy: Release**, **Layout policy: Strict**, and **Deployment policy: Allow redeploy**. The repository URL must be `http://localhost:8081/repository/maven-releases/`.

Compose starts Redis only, so it will not start a second Nexus instance:

```powershell
docker compose up -d redis
docker compose ps
```

Create a local Maven settings file from the example, then load your Nexus deployment password into this PowerShell session without echoing it:

```powershell
Copy-Item settings-nexus.example.xml settings.xml
$securePassword = Read-Host 'Nexus deployment password' -AsSecureString
$env:NEXUS_PASSWORD = [Net.NetworkCredential]::new('', $securePassword).Password
```

`settings.xml` is ignored by Git. The Maven server id `nexus` matches the root POM's `distributionManagement` entry. Use an account permitted to deploy to `maven-releases`; do not commit credentials.

## Build and publish the library

The reactor build compiles/tests both projects and resolves the library directly as a reactor module:

```powershell
mvn clean verify
```

Publish the parent POM first so Maven clients can resolve the library's parent, then publish the reusable library:

```powershell
mvn -N deploy -s settings.xml
mvn -f employee-library/pom.xml deploy -s settings.xml
```

Confirm `com/example/employee/employee-platform/1.0.0/` and `com/example/employee/employee-library/1.0.0/` appear in the Nexus Browse view. The `employee-redis-app` POM points to that same Nexus repository for dependency resolution. If you change and republish a release, increment the library version unless redeploy is enabled.

To explicitly exercise dependency download from Nexus rather than the reactor/local Maven cache, remove the cached `com/example/employee/employee-library/1.0.0` directory under `%USERPROFILE%\.m2\repository`, then run:

```powershell
mvn -f employee-redis-app/pom.xml dependency:tree -s settings.xml
```

The tree should include `com.example:employee-library:jar:1.0.0` and Maven should retrieve it from `localhost:8081`.

## Run the application

```powershell
docker compose up -d redis
mvn -f employee-redis-app/pom.xml spring-boot:run -s settings.xml
```

The API listens at <http://localhost:8080>. H2 data persists under `employee-redis-app/data`; the H2 console is at <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:file:./data/employees`, user `sa`, blank password). Redis is exposed on port `6379`.

Use the included `employee-api.http` with the VS Code REST Client extension or import/copy the requests into Postman. Available endpoints:

| Method | Endpoint | Result |
| --- | --- | --- |
| `POST` | `/api/employees` | Create employee (`201` and `Location`) |
| `GET` | `/api/employees` | List employees |
| `GET` | `/api/employees/{id}` | Read employee (`404` if absent) |
| `PUT` | `/api/employees/{id}` | Update employee |
| `DELETE` | `/api/employees/{id}` | Delete employee (`204`) |

Example request body:

```json
{
  "id": null,
  "fullName": "Ada Lovelace",
  "email": "ada.lovelace@example.com",
  "department": "Engineering",
  "salary": 125000.00
}
```

`fullName`, `email`, and `department` are required; email must contain `@`, and salary must be zero or greater. Duplicate emails are rejected.

## Demonstrate caching and TTL

1. Create an employee and note its returned id.
2. Call `GET /api/employees/{id}` twice. The first call reads H2 and populates Redis; the second is served from cache.
3. Inspect keys and TTL with `docker exec employee-redis redis-cli --scan` and `docker exec employee-redis redis-cli TTL 'employee::<id>'`. Spring Cache keys are prefixed by cache name; the exact serialized key can vary by configuration. Repeat before expiry and observe a positive TTL decreasing. After approximately 30 seconds it expires and the next GET repopulates it.
4. Call PUT for that id and GET again to demonstrate `@CachePut`; the returned updated data is immediately in cache.
5. Call DELETE, then GET. The key has been evicted and the GET returns `404`.
6. Use `docker exec employee-redis redis-cli FLUSHDB` only when you want to reset the demo cache.

For evidence, capture Nexus Browse showing `employee-library:1.0.0`, the Maven dependency tree showing the library dependency, successful API responses for CRUD, and Redis CLI TTL/keys before and after expiry or eviction. Screenshots must be captured from the running local services; none are fabricated or included in source control.

## Configuration and cleanup

- Change Redis host/port with `SPRING_DATA_REDIS_HOST` and `SPRING_DATA_REDIS_PORT`.
- Change cache lifetime with `APP_CACHE_EMPLOYEE_TTL` (for example `PT1M`).
- Database uses H2 for a self-contained demo; replace the JDBC driver and datasource settings for MySQL/PostgreSQL.
- Stop the Redis container with `docker compose down`. This does not stop or delete the existing Nexus instance.