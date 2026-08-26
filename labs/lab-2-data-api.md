# Lab 2 — Develop with the Data API

- **API:** Java Data API client (`astra-db-java`)
- **Use cases:** Enterprise Identity, Knowledge Search
- **When:** Part A during module 05. Part B during module 06.

## Prerequisites

- Lab 1 Part A tables exist (`users_by_id` at minimum)
- Java 17+ and Maven 3.9+
- Environment variables from [00 Get started](../00-get-started/get-started.md) in **this** terminal (PowerShell equivalents are in 00):

```bash
export API_ENDPOINT="https://DATABASE_ID-REGION.apps.astra.datastax.com"
export APPLICATION_TOKEN="AstraCS:..."
export KEYSPACE_NAME="default_keyspace"
```

---

## Part A — Connect, write, read (module 05)

```bash
test -f pom.xml || cd sample-app
mvn -q compile
mvn -q exec:java -Dexec.mainClass="com.datastax.enablement.IdentityApp"
```

Expected: a line that you connected, a line that you inserted a user, a line that you read that user back by `user_id`.

If it fails:

| Symptom | Check |
|---|---|
| `API_ENDPOINT` / `APPLICATION_TOKEN` must be defined | Exports in **this** terminal (a `.env` file is not loaded) |
| 401 / unauthorized | Token copied fully, `AstraCS:` prefix, no extra quotes or spaces |
| table not found | Lab 1 Part A; keyspace name; CQL names are case-sensitive if created in the portal |
| timeout / hibernated | Open the database in the portal until **Active** |
| Unsupported class version / compiler release | Java 17+ (`java -version`) |

Read `IdentityApp.java`. Point to: client construction (`getDatabase` with the keyspace), `insertOne`, `findOne`.

This app writes `users_by_id` only. Lab 1 dual-wrote `users_by_email` in CQL. A production identity service must write both tables on every profile change; Part A does not demonstrate that second write.

**Part A deliverable:** You have a Java process that authenticates to Astra DB and performs a partition-key read.

---

## Part B — Knowledge Search (module 06)

```bash
test -f pom.xml || cd sample-app
mvn -q exec:java -Dexec.mainClass="com.datastax.enablement.KnowledgeSearchApp"
```

The app:

1. Creates (or gets) collection `knowledge` with vector dimension **5** and cosine similarity, then `deleteAll` so a re-run is safe
2. Inserts a few help-center articles with a `topic` field and a toy embedding
3. Runs ANN search **with** `topic = "identity"`
4. Prints neighbours

The vectors are **not** from a production embedding model. They exist so the lab does not depend on a vectorize region. In production, use one model for insert and query.

**Part B deliverable:** You can explain why the filter is there (vector search is similarity, not an ID lookup) and why extra regions do not give Knowledge Search extra vector-query capacity (module 04).

### Run both halves

```bash
test -f pom.xml || cd sample-app
mvn -q exec:java -Dexec.mainClass="com.datastax.enablement.WorkshopApp"
```

---

## What you are not building

- Spring Boot
- A second CQL driver application
- Langflow, GraphQL, or the deprecated Document API
- Multi-region vector failover
- A RAG product

## Next

Skim [solutions/lab-2-notes.md](../solutions/lab-2-notes.md) if a step failed. Keep [reference architectures](../reference-architectures/reference-architectures.md) and the [architecture review checklist](../ARCHITECTURE-REVIEW-CHECKLIST.md) for later. You are finished with the workshop path.
