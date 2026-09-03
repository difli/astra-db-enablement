# 06 — Data API and vector search

A correct mental model, not a RAG course. At the end of this page you run Knowledge Search in Java.

Sources: [Vector search](https://docs.datastax.com/en/astra-db-serverless/databases/vector-search.html), [Vector concepts](https://docs.datastax.com/en/astra-db-serverless/get-started/vector-concepts.html), [Data API](https://docs.datastax.com/en/astra-db-serverless/api-reference/dataapiclient.html).

## What vector search is

1. Store embeddings (float arrays) with each document or row.
2. Embed the query with the **same model**.
3. Search for **similar** vectors.
4. Optionally **filter** on metadata together with the vector sort.

Astra DB uses **approximate nearest neighbour (ANN)**, not exact KNN. ANN may miss the mathematically perfect neighbour. That is expected.

## What vector search is not

- Not a replacement for `WHERE user_id = ...`. Identity stays on tables.
- Not exact match.
- Not multi-region **vector-query HA** today (module 04). Extra regions can replicate documents; they do not add another vector PCU.
- Vectors are not human-readable. Model choice matters; **same model** for insert and query is mandatory.

Metadata filters narrow the set (for example `topic = "identity"`). Use them.

## Knowledge Search

Use case 3 — embed documents, retrieve by similarity **and** metadata. Sketch: [Astra DB Architect Guide](../ASTRA-DB-ARCHITECT-GUIDE.md#33-knowledge-search).

| Field | Role |
|---|---|
| `_id` | Document id |
| `text` | Searchable content |
| `topic` | Metadata filter |
| `$vector` | Embedding |

The lab uses **small pre-computed vectors** (dimension 5) so it runs in any Serverless (vector) region without a vectorize provider. Production uses one real embedding model (or Astra **vectorize**, where the region supports it).

```java
CollectionDefinition definition =
    new CollectionDefinition().vector(5, SimilarityMetric.COSINE);
Collection<Document> knowledge;
try {
  knowledge = database.createCollection("knowledge", definition);
} catch (RuntimeException alreadyExists) {
  knowledge = database.getCollection("knowledge");
}

knowledge.deleteAll();

knowledge.insertOne(
    new Document()
        .id("k1")
        .append(
            "text",
            "Reset a work password from the identity portal. We email a "
                + "one-time link to the address on your employee profile. Other "
                + "signed-in sessions stay open until they expire, unless you "
                + "choose Sign out everywhere.")
        .append("topic", "identity")
        .vector(new float[] {0.12f, 0.88f, 0.05f, 0.10f, 0.40f}));

Filter filter = Filters.eq("topic", "identity");
CollectionFindOptions options =
    new CollectionFindOptions()
        .sort(Sort.vector(new float[] {0.11f, 0.90f, 0.04f, 0.12f, 0.38f}))
        .limit(3);

knowledge.find(filter, options);
```

`createCollection` fails if `knowledge` already exists. The sample app catches that and calls `getCollection`, then `deleteAll` so this lab can be re-run without `DOCUMENT_ALREADY_EXISTS`. It inserts four help-center articles (`k1`–`k4`).

You can also put vectors on **tables**. Collections are the faster teaching surface for document-shaped search.

## Platform constraints

From [module 04](../04-astra-specific-behavior/astra-specific-behavior.md) and [plan PCU groups](https://docs.datastax.com/en/astra-db-serverless/administration/plan-pcu.html):

- Vector PCU groups are **one unit** (no autoscaling, no burst).
- Plan Knowledge Search **query** capacity as single-region today. Extra regions can replicate data; each vector region is still one PCU.
- Prefer **Cache optimized** for vector working sets.
- Collection index rules are in [database limits](https://docs.datastax.com/en/astra-db-serverless/databases/database-limits.html); selective indexing is set at **create** time.

## Lab — Knowledge Search (collection + ANN)

The code is above and in [`KnowledgeSearchApp.java`](../sample-app/src/main/java/com/datastax/enablement/KnowledgeSearchApp.java). Same Maven project and environment variables as [module 05](../05-java-development/java-development.md).

```bash
test -f pom.xml || cd sample-app
mvn -q exec:java -Dexec.mainClass="com.datastax.enablement.KnowledgeSearchApp"
```

Expected (SLF4J noise is harmless):

```text
Connected to https://…apps.astra.datastax.com keyspace=default_keyspace
Inserted Knowledge Search documents into 'knowledge'
ANN neighbours for topic=identity:
  {"_id":"k1","topic":"identity","text":"Reset a work password…"}
  {"_id":"k2","topic":"identity","text":"Find an employee by work email…"}
```

Four documents were inserted (`k1`–`k4`). Only **k1** and **k2** have `topic=identity`, so the filter drops finance/platform. `limit(3)` is a ceiling; two rows is correct. k1 first is expected (the query vector is closest to k1).

If it fails:

| Symptom | Check |
|---|---|
| Collection create / index limit | Database already has too many collections — [database limits](https://docs.datastax.com/en/astra-db-serverless/databases/database-limits.html). Drop an unused collection, or reuse `knowledge` |
| Neighbours from the wrong topic | The metadata filter was omitted. ANN without a filter is a grab bag |

**Deliverable:** You can explain why the filter is there (vector search is similarity, not an ID lookup) and why extra regions do not give Knowledge Search extra vector-query capacity (module 04).

### Run both halves

[`WorkshopApp.java`](../sample-app/src/main/java/com/datastax/enablement/WorkshopApp.java) calls module 05 then this lab:

```java
IdentityApp.upsertAndRead(database);
KnowledgeSearchApp.search(database);
```

```bash
test -f pom.xml || cd sample-app
mvn -q exec:java -Dexec.mainClass="com.datastax.enablement.WorkshopApp"
```

Expected (SLF4J noise is harmless):

```text
Connected to https://…apps.astra.datastax.com keyspace=default_keyspace
Wrote users_by_id partition 11111111-1111-1111-1111-111111111111
Read profile: {"user_id":"11111111-…","email":"alex@example.com","display_name":"Alex",…}
Inserted Knowledge Search documents into 'knowledge'
ANN neighbours for topic=identity:
  {"_id":"k1","topic":"identity","text":"Reset a work password…"}
  {"_id":"k2","topic":"identity","text":"Find an employee by work email…"}
```

## Wrap-up

This is the end of the workshop. There is no seventh module. Recap here and walk the [decision tree](../01-why-astra-db/why-astra-db.md) once more.

You can now answer:

| Question | What to say |
|---|---|
| What is Astra DB? | Cassandra as a service. You own the model and the app; the platform owns nodes, compaction, and `cassandra.yaml`. |
| Why / when / when not? | Walk the [decision tree](../01-why-astra-db/why-astra-db.md). Known partition keys and Knowledge Search fit. Ad-hoc SQL and warehouses do not. |
| vs self-managed Cassandra? | Same data model (partitions, TTL, tombstones). No cluster admin. |
| How do I model? | One table per query. Identity is four tables and a dual-write. Inbox is bucket + `event_id` + TTL. |
| How do I connect Java? | Data API: endpoint + token + keyspace. No Secure Connect Bundle for this path. |
| Which constraints matter? | Unsupported `WITH` is a **warning**. `CREATE KEYSPACE` is an **error**. Vector PCU is **one unit**. Numbers: [database limits](https://docs.datastax.com/en/astra-db-serverless/databases/database-limits.html). |
| Common mistakes? | `ALLOW FILTERING` as the email lookup. Compaction in DDL. Extra regions as vector-query HA. |

Take-home (not on the clock): [Astra DB Architect Guide](../ASTRA-DB-ARCHITECT-GUIDE.md). If the source is Oracle: [Oracle to Astra DB assessment](../ORACLE-TO-ASTRA-ASSESSMENT.md).

## Optional next reading

- [Quickstart for collections](https://docs.datastax.com/en/astra-db-serverless/get-started/quickstart.html)
- [Quickstart for tables](https://docs.datastax.com/en/astra-db-serverless/get-started/quickstart-tables.html)
- [Plan PCU groups](https://docs.datastax.com/en/astra-db-serverless/administration/plan-pcu.html)
