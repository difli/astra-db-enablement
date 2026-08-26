# Java sample application

One Maven project. Data API only (`astra-db-java`). No Spring Boot.

## What it shows

| Class | Lab |
|---|---|
| `Connect` | Endpoint + token |
| `IdentityApp` | Lab 2 part A — table write/read |
| `KnowledgeSearchApp` | Lab 2 part B — collection vector search |
| `WorkshopApp` | Both |

## Run

Java 17+ and Maven 3.9+.

```bash
export API_ENDPOINT="https://YOUR_DB_ID-YOUR_REGION.apps.astra.datastax.com"
export APPLICATION_TOKEN="AstraCS:..."
export KEYSPACE_NAME="default_keyspace"

mvn -q compile
mvn -q exec:java -Dexec.mainClass="com.datastax.enablement.IdentityApp"
mvn -q exec:java -Dexec.mainClass="com.datastax.enablement.KnowledgeSearchApp"
```

Create `users_by_id` in [Lab 1](../labs/lab-1-cql.md) before `IdentityApp`.
