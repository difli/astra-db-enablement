# 05 — Java development

One application. No Spring Boot. The goal is Astra DB, not a framework course.

This module starts **[Lab 2 — Develop with the Data API](../labs/lab-2-data-api.md)** (part A: connect, write, read).

## Which Java library?

| Library | When |
|---|---|
| **`astra-db-java`** (Data API client) | **This workshop.** Endpoint + token. Tables and collections. Vector search. |
| Cassandra Java driver | CQL over mTLS using a **Secure Connect Bundle**. Username is the literal string `token`, password is the application token. |

Use the Data API client unless you are maintaining an existing CQL driver codebase.

Requirements: **Java 17+** (21 recommended), Maven 3.9+.

Source: [Get started with the Data API](https://docs.datastax.com/en/astra-db-serverless/api-reference/dataapiclient.html).

## Authenticate

```java
String endpoint = System.getenv("API_ENDPOINT");
String token = System.getenv("APPLICATION_TOKEN");

DataAPIClient client = new DataAPIClient(token);
Database database = client.getDatabase(endpoint);
```

That is the whole connection story for Lab 2. No contact points, no SCB unzip, no `cassandra.yaml`.

Never hard-code tokens. Never commit `.env` files.

## What the sample app demonstrates

The project in [`sample-app/sample-app.md`](../sample-app/sample-app.md) is the only Java application in this repository.

| Class | Shows |
|---|---|
| `Connect` | Client + database from environment variables |
| `IdentityApp` | Table insert and find by partition key (Enterprise Identity) |
| `KnowledgeSearchApp` | Collection insert and ANN search with a metadata filter |
| `WorkshopApp` | Runs identity then Knowledge Search |

Operations you must be able to point to in the code:

1. **Connect**
2. **Authenticate** (token)
3. **Write** (`insertOne`)
4. **Read** (`findOne` / `find`)
5. **Query** (filter on a table; vector sort + filter on a collection)

## Tables through the Data API

You can use the Data API against tables you created in **Lab 1 with CQL**. That is deliberate: CQL and the Data API share the same table.

```java
Table<Row> users = database.getTable("users_by_id");

users.insertOne(
    new Row()
        .addText("user_id", userId)
        .addText("email", "alex@example.com")
        .addText("display_name", "Alex")
        .addText("status", "active"));

Optional<Row> found = users.findOne(Filters.eq("user_id", userId));
```

The Data API always uses `LOCAL_QUORUM`. You do not set consistency in the client.

Some CQL features are awkward or unsupported through the Data API (for example some frozen types, TTL as a write-time API concern). If you need those, use CQL. For identity CRUD, the Data API is enough.

## Driver path (awareness only)

If you connect with the Cassandra Java driver:

```java
CqlSession session = CqlSession.builder()
    .withCloudSecureConnectBundle(Paths.get("secure-connect-bundle.zip"))
    .withAuthCredentials("token", System.getenv("APPLICATION_TOKEN"))
    .withKeyspace(System.getenv("KEYSPACE_NAME"))
    .build();
```

Keep the SCB as a **zip**. Treat it like a secret. You will not build this path in the lab.

Source: [Java driver](https://docs.datastax.com/en/astra-db-serverless/drivers/java-driver.html).

## Application habits that matter on Astra

- **Timeouts and retries with backoff** on rate limits (especially after hibernation or a traffic jump)
- **Idempotent writes** (inbox primary key) rather than LWT on a hot partition
- **Dual-write both identity tables** if you add `users_by_email`
- **Warm** on-demand databases before a launch, or use PCUs in production

## Lab

Open [`sample-app`](../sample-app/sample-app.md) and complete **[Lab 2 Part A](../labs/lab-2-data-api.md)**.

## Next

[06 — Data API and vector search](../06-data-api-and-vector-search/data-api-and-vector-search.md)
