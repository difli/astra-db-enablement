# Java sample application

One Maven project. Data API only (`astra-db-java`). No Spring Boot.

## What it shows

| Class | Lab |
|---|---|
| `Connect` | Endpoint + token + keyspace |
| `IdentityApp` | Module 05 — table write/read |
| `KnowledgeSearchApp` | Module 06 — collection vector search |
| `WorkshopApp` | Both |

## Run

Java 17+ and Maven 3.9+. The apps read **process environment variables**. Load them in this terminal first (quick `set -a && source .env && set +a` from the repository root, or the explicit commands in [00 Get started](../00-get-started/get-started.md)), then:

```bash
test -f pom.xml || cd sample-app

mvn -q compile
mvn -q exec:java -Dexec.mainClass="com.datastax.enablement.IdentityApp"
mvn -q exec:java -Dexec.mainClass="com.datastax.enablement.KnowledgeSearchApp"
```

Create `users_by_id` in [module 03](../03-data-modeling/data-modeling.md) before `IdentityApp`.
