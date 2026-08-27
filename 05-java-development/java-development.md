# 05 — Java development

One application. No Spring Boot. The goal is Astra DB, not a framework course.

Read this page first (client vs driver, connect, tables and collections). You run the sample app at the end of the module.

## Which Java library?

| Library | When |
|---|---|
| **[`astra-db-java`](https://docs.datastax.com/en/astra-db-serverless/api-reference/dataapiclient.html)** (Data API client) | **This workshop.** Endpoint + token. Tables and collections. Vector search. |
| [Cassandra Java driver](https://docs.datastax.com/en/astra-db-serverless/drivers/java-driver.html) | CQL over mTLS using a **Secure Connect Bundle**. Username is the literal string `token`, password is the application token. |

Use the Data API client unless you are maintaining an existing CQL driver codebase.

Requirements: **Java 17+** (21 recommended), Maven 3.9+.

Source: [Get started with the Data API](https://docs.datastax.com/en/astra-db-serverless/api-reference/dataapiclient.html).

## Authenticate

From [`Connect.java`](../sample-app/src/main/java/com/datastax/enablement/Connect.java):

```java
String endpoint = System.getenv("API_ENDPOINT");
String token = System.getenv("APPLICATION_TOKEN");
String keyspace =
    System.getenv().getOrDefault("KEYSPACE_NAME", "default_keyspace");

DataAPIClient client = new DataAPIClient(token);
Database database = client.getDatabase(endpoint, keyspace);
```

No contact points, no SCB unzip, no `cassandra.yaml`. Pass the keyspace so this lab hits the same container as the CQL tables from module 03.

Never hard-code tokens. Never commit `.env` files.

## What the sample app demonstrates

The project in [`sample-app/sample-app.md`](../sample-app/sample-app.md) is the only Java application in this repository.

| Class | Shows |
|---|---|
| [`Connect`](../sample-app/src/main/java/com/datastax/enablement/Connect.java) | Client + database from endpoint, token, and keyspace |
| [`IdentityApp`](../sample-app/src/main/java/com/datastax/enablement/IdentityApp.java) | Table insert and find by partition key (Enterprise Identity) |
| [`KnowledgeSearchApp`](../sample-app/src/main/java/com/datastax/enablement/KnowledgeSearchApp.java) | Collection insert and ANN search with a metadata filter |
| [`WorkshopApp`](../sample-app/src/main/java/com/datastax/enablement/WorkshopApp.java) | Runs identity then Knowledge Search |

Operations you must be able to point to in the code:

1. **Connect**
2. **Authenticate** (token)
3. **Write** (`insertOne`)
4. **Read** (`findOne` / `find`)
5. **Query** (filter on a table; vector sort + filter on a collection)

## Tables through the Data API

You can use the Data API against tables you created in **CQL in module 03**. That is deliberate: CQL and the Data API share the same table. From [`IdentityApp.java`](../sample-app/src/main/java/com/datastax/enablement/IdentityApp.java):

```java
UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
Table<Row> users = database.getTable("users_by_id");

users.insertOne(
    new Row()
        .add("user_id", userId)
        .addText("email", "alex@example.com")
        .addText("display_name", "Alex")
        .addText("status", "active")
        .add("updated_at", Instant.now()));

Optional<Row> found = users.findOne(Filters.eq("user_id", userId));
```

`user_id` is a CQL `uuid`. Use `.add(...)` with a `UUID`, not `.addText`. The sample app writes this table only; dual-write `users_by_email` in production (the lab below does not demonstrate that second write).

The Data API always uses `LOCAL_QUORUM`. You do not set consistency in the client.

Some CQL features are awkward or unsupported through the Data API (for example some frozen types, TTL as a write-time API concern). If you need those, use CQL. For identity CRUD, the Data API is enough.

## Collections through the Data API

Same client, same `Database`. A **collection** stores documents (dynamic fields), not CQL rows. You do **not** run this until [module 06](../06-data-api-and-vector-search/data-api-and-vector-search.md). From [`KnowledgeSearchApp.java`](../sample-app/src/main/java/com/datastax/enablement/KnowledgeSearchApp.java):

```java
Collection<Document> knowledge = database.getCollection("knowledge");

knowledge.insertOne(
    new Document()
        .id("k1")
        .append("topic", "identity")
        .append("text", "Reset a work password from the identity portal.")
        .vector(new float[] {0.12f, 0.88f, 0.05f, 0.10f, 0.40f}));
```

`getTable` vs `getCollection` is the fork: identity/inbox stay **tables**; Knowledge Search is a **collection**.

## Driver path (awareness only)

If you connect with the Cassandra Java driver (not in this sample app — see the [Java driver docs](https://docs.datastax.com/en/astra-db-serverless/drivers/java-driver.html)):

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
- **Dual-write `users_by_email`** when a profile changes (the lab below writes `users_by_id` only)
- **Warm** on-demand databases before a launch, or use PCUs in production

## Lab — Connect, write, read (`users_by_id`)

The code is above and in [`sample-app`](../sample-app/sample-app.md). You run it with Maven; you do not paste it into the CQL console.

Prerequisites: `users_by_id` from [module 03](../03-data-modeling/data-modeling.md); Java 17+ and Maven 3.9+; environment variables from [00 Get started](../00-get-started/get-started.md) in **this** terminal (quick `source .env`, or the explicit `export` / PowerShell commands).

`test -f pom.xml || cd sample-app` means: if you are not already in the folder that contains `pom.xml`, enter `sample-app`.

```bash
test -f pom.xml || cd sample-app
mvn -q compile
mvn -q exec:java -Dexec.mainClass="com.datastax.enablement.IdentityApp"
```

Expected (SLF4J “No SLF4J providers” lines are noise, not a failure):

```text
Connected to https://…apps.astra.datastax.com keyspace=default_keyspace
Wrote users_by_id partition 11111111-1111-1111-1111-111111111111
Read profile: {"user_id":"11111111-…","email":"alex@example.com","display_name":"Alex",…}
```

If it fails:

| Symptom | Check |
|---|---|
| `API_ENDPOINT` / `APPLICATION_TOKEN` must be defined | Load vars in **this** terminal (module 00: `source .env` or explicit `export` / PowerShell). Java does not read `.env` |
| 401 / unauthorized | Token copied fully, `AstraCS:` prefix, no extra quotes or spaces |
| table not found | Module 03 identity tables; keyspace name; CQL names are case-sensitive if created in the portal |
| `findOne` empty | Same keyspace as module 03; `user_id` matches the uuid you inserted |
| timeout / hibernated | Open the database in the portal until **Active** |
| Unsupported class version / compiler release | Java 17+ (`java -version`) |

This app writes `users_by_id` only. Module 03 dual-wrote `users_by_email` in CQL. A production identity service must write both tables on every profile change; this lab does not demonstrate that second write.

**Deliverable:** You have a Java process that authenticates to Astra DB and performs a partition-key read.

## Next

[06 — Data API and vector search](../06-data-api-and-vector-search/data-api-and-vector-search.md)
