# Lab 2 — Develop with the Data API

**API:** Java Data API client (`astra-db-java`)  
**Use cases:** Enterprise Identity, Knowledge Search  
**When:** Part A during module 05. Part B during module 06.

## Prerequisites

- Lab 1 Part A tables exist (`users_by_id` at minimum)
- Java 17+ and Maven 3.9+
- Environment variables from [00 Get started](../00-get-started/get-started.md):

```bash
export API_ENDPOINT="https://YOUR_DB_ID-YOUR_REGION.apps.astra.datastax.com"
export APPLICATION_TOKEN="AstraCS:..."
export KEYSPACE_NAME="default_keyspace"
```

---

## Part A — Connect, write, read (module 05)

```bash
cd sample-app
mvn -q compile
mvn -q exec:java -Dexec.mainClass="com.datastax.enablement.IdentityApp"
```

Expected: a line that you connected, a line that you inserted a user, a line that you read that user back by `user_id`.

If it fails:

| Symptom | Check |
|---|---|
| `API_ENDPOINT` / `APPLICATION_TOKEN` must be defined | Exports in **this** terminal |
| 401 / unauthorized | Token copied fully, `AstraCS:` prefix |
| table not found | Lab 1 Part A; keyspace name; CQL names are case-sensitive if created in the portal |
| timeout / hibernated | Open the database in the portal until **Active** |

Read `IdentityApp.java`. Point to: client construction, `insertOne`, `findOne`.

**Part A deliverable:** You have a Java process that authenticates to Astra DB and performs a partition-key read.

---

## Part B — Knowledge Search (module 06)

```bash
cd sample-app
mvn -q exec:java -Dexec.mainClass="com.datastax.enablement.KnowledgeSearchApp"
```

The app:

1. Creates (or gets) collection `knowledge` with vector dimension **5** and cosine similarity
2. Inserts a few documents with a `topic` field and a toy embedding
3. Runs ANN search **with** `topic = "identity"`
4. Prints neighbours

The vectors are **not** from a production embedding model. They exist so the lab does not depend on a vectorize region. In production, use one model for insert and query.

**Part B deliverable:** You can explain why the filter is there (vector search is similarity, not an ID lookup) and why Knowledge Search is single-region / single-PCU in a real architecture (module 04).

### Run both halves

```bash
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

Skim [solutions/lab-2-notes.md](../solutions/lab-2-notes.md) if a step failed. Keep [reference architectures](../reference-architectures/reference-architectures.md) for later. You are finished with the workshop path.
