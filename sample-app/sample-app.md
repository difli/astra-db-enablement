# Java sample application

One Maven project. Data API only (`astra-db-java`). No Spring Boot.

## What it shows

| Class | Lab |
|---|---|
| `Connect` | Endpoint + token + keyspace |
| `IdentityApp` | Lab 2 part A — table write/read |
| `KnowledgeSearchApp` | Lab 2 part B — collection vector search |
| `WorkshopApp` | Both |

## Run

Java 17+ and Maven 3.9+. The apps read **process environment variables**. Copying `.env.example` to `.env` does not load them; export the values in this terminal (see [00 Get started](../00-get-started/get-started.md)). The block below works from the repository root or from `sample-app`.

```bash
test -f pom.xml || cd sample-app
export API_ENDPOINT="https://DATABASE_ID-REGION.apps.astra.datastax.com"
export APPLICATION_TOKEN="AstraCS:..."
export KEYSPACE_NAME="default_keyspace"

mvn -q compile
mvn -q exec:java -Dexec.mainClass="com.datastax.enablement.IdentityApp"
mvn -q exec:java -Dexec.mainClass="com.datastax.enablement.KnowledgeSearchApp"
```

Create `users_by_id` in [Lab 1](../labs/lab-1-cql.md) before `IdentityApp`.
