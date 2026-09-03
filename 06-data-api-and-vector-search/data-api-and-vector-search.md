# 06 — Data API and vector search

This module builds the mental model for vector search on Astra DB — what it is, what it is not, and how to use it correctly from Java. It is not a RAG course; the goal is to understand the mechanics well enough to use them without surprises.

**By the end you should be able to:**
- Explain what ANN vector search does and where it fits alongside CQL tables
- Describe when to use a collection vs a table with a vector column
- Run Knowledge Search in Java: insert documents with embeddings, query by similarity, and narrow results with a metadata filter

Sources: [Vector search](https://docs.datastax.com/en/astra-db-serverless/databases/vector-search.html), [Vector concepts](https://docs.datastax.com/en/astra-db-serverless/get-started/vector-concepts.html), [Data API](https://docs.datastax.com/en/astra-db-serverless/api-reference/dataapiclient.html).

## What vector search is

At its core, vector search is a four-step workflow:

1. **Store** — each document or row is saved with an embedding: a float array that represents its meaning numerically.
2. **Embed the query** — at query time, convert the user's input into a vector using the **same model** used at insert time.
3. **Search for similar vectors** — Astra finds the stored vectors closest to the query vector.
4. **Filter optionally** — narrow the candidate set with a metadata filter (for example `topic = "identity"`) before or alongside the vector sort.

Vector search at scale uses **approximate nearest neighbour (ANN)** — this is true of all vector databases, not just Astra. ANN trades a small chance of missing the mathematically perfect neighbour for the speed needed to search millions of vectors in milliseconds. The results are highly relevant in practice.

## What vector search is not

- **Not a replacement for `WHERE user_id = ?`** — structured identity and inbox lookups stay on CQL tables with a partition key.
- **Not keyword or exact-value lookup** — ANN finds the most *similar* results, not documents that contain a specific word or match a specific value. Use CQL `WHERE` for exact lookups; use vector search for similarity.
- **Not multi-region vector-query HA** — extra regions replicate documents but do not add vector-query capacity; each region has a one-PCU ceiling (see [module 04](../04-astra-specific-behavior/astra-specific-behavior.md)).
- **Not model-agnostic** — vectors are not human-readable; the embedding model is baked into the numbers. You must use the **same model** for insert and query or results are meaningless.

## Knowledge Search

Use case 3 — embed documents, retrieve by similarity **and** metadata. Read through the code and field table below first; the lab at the end of this page asks you to run it and explain what you see. Sketch: [Astra DB Architect Guide](../ASTRA-DB-ARCHITECT-GUIDE.md#33-knowledge-search).

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

In this lab you run `KnowledgeSearchApp` — the class shown in the **Knowledge Search** section above. It creates a collection called `knowledge`, inserts four short help-centre articles each with a pre-computed embedding, then queries by vector similarity filtered to `topic=identity`. You should get back the two most relevant identity articles.

The app uses the same Maven project and environment variables as [module 05](../05-java-development/java-development.md) — no extra setup needed.

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

What you are seeing:
- Four documents (`k1`–`k4`) were inserted — two tagged `topic=identity`, two tagged with other topics.
- The metadata filter `topic=identity` drops the non-identity documents before ANN runs.
- `limit(3)` is the maximum; two results is correct because only two documents match the filter.
- `k1` comes first because its embedding is the closest to the query vector.

If it fails:

| Symptom | Check |
|---|---|
| Collection create / index limit | Database already has too many collections — [database limits](https://docs.datastax.com/en/astra-db-serverless/databases/database-limits.html). Drop an unused collection or reuse `knowledge` |
| Results from the wrong topic | The metadata filter was not applied — ANN without a filter searches the whole collection |
| `knowledge` not found | Environment variables not loaded — run `set -a && source .env && set +a` first |

**Deliverables:**
- You can explain why the metadata filter is there (narrow by topic before similarity search, not after)
- You can explain why two results come back instead of three
- You can explain why extra regions do not add vector-query capacity

### Run both halves

[`WorkshopApp.java`](../sample-app/src/main/java/com/datastax/enablement/WorkshopApp.java) ties the whole workshop together in a single run. Both calls go through the **same Data API client** — there is no driver switch, no second connection, no separate credentials:

- **`IdentityApp.upsertAndRead`** — writes and reads a row in the `users_by_id` **table** (CQL schema, typed columns, partition-key lookup). This is the structured data path.
- **`KnowledgeSearchApp.search`** — creates the `knowledge` **collection**, inserts documents with embeddings, and queries by vector similarity. This is the unstructured/search path.

Both use the same `Database` object created once from your endpoint, token, and keyspace. The only difference is `getTable(...)` vs `getCollection(...)`.

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
