# 06 — Data API and vector search

A correct mental model, not a RAG course. This finishes **[Lab 2](../labs/lab-2-data-api.md)** (part B: Knowledge Search).

Sources: [Vector search](https://docs.datastax.com/en/astra-db-serverless/databases/vector-search.html), [Vector concepts](https://docs.datastax.com/en/astra-db-serverless/get-started/vector-concepts.html), [Data API](https://docs.datastax.com/en/astra-db-serverless/api-reference/dataapiclient.html).

## What vector search is

1. Store embeddings (float arrays) with each document or row.
2. Embed the query with the **same model**.
3. Search for **similar** vectors.
4. Optionally **filter** on metadata together with the vector sort.

Astra DB uses **approximate nearest neighbour (ANN)**, not exact KNN. ANN may miss the mathematically perfect neighbour. That is expected.

## What vector search is not

- Not a replacement for `WHERE user_id = …`. Identity stays on tables.
- Not exact match.
- Not multi-region HA today (module 04).
- Vectors are not human-readable. Model choice matters; **same model** for insert and query is mandatory.

Metadata filters narrow the set (for example `topic = "identity"`). Use them.

## Knowledge Search

Use case 3 — embed documents, retrieve by similarity **and** metadata. Sketch: [reference architectures](../reference-architectures/reference-architectures.md).

| Field | Role |
|---|---|
| `_id` | Document id |
| `text` | Searchable content |
| `topic` | Metadata filter |
| `$vector` | Embedding |

The lab uses **small pre-computed vectors** (dimension 5) so it runs in any Serverless (vector) region without a vectorize provider. Production uses one real embedding model (or Astra **vectorize**, where the region supports it).

```java
Collection<Document> knowledge =
    database.createCollection(
        "knowledge",
        new CollectionDefinition().vector(5, SimilarityMetric.COSINE));

knowledge.insertOne(
    new Document()
        .id("k1")
        .append("text", "Password reset requires the active session id")
        .append("topic", "identity")
        .vector(new float[] {0.12f, 0.88f, 0.05f, 0.10f, 0.40f}));

Filter filter = Filters.eq("topic", "identity");
CollectionFindOptions options =
    new CollectionFindOptions()
        .sort(Sort.vector(new float[] {0.11f, 0.90f, 0.04f, 0.12f, 0.38f}))
        .limit(3);

knowledge.find(filter, options);
```

You can also put vectors on **tables**. Collections are the faster teaching surface for document-shaped search.

## Platform constraints

From [module 04](../04-astra-specific-behavior/astra-specific-behavior.md) and [plan PCU groups](https://docs.datastax.com/en/astra-db-serverless/administration/plan-pcu.html):

- Vector PCU groups are **one unit** (no autoscaling, no burst).
- Knowledge Search is **single-region** today.
- Prefer **Cache optimized** for vector working sets.
- Collection index rules are in [database limits](https://docs.datastax.com/en/astra-db-serverless/databases/database-limits.html); selective indexing is set at **create** time.

## Lab

Complete **[Lab 2 Part B](../labs/lab-2-data-api.md)**.

## You are done when

You can explain, without slides:

- Why identity is tables and Knowledge Search is a collection
- Why ANN + metadata beats “embed everything and hope”
- Why Astra ignored your `WITH compaction` in Lab 1
- How the Java client authenticates
- Why vector HA is not “add another region like Cassandra”

Walk the [decision tree](../01-why-astra-db/why-astra-db.md) once more. Then keep [reference architectures](../reference-architectures/reference-architectures.md) for later.

## Optional next reading

- [Quickstart for collections](https://docs.datastax.com/en/astra-db-serverless/get-started/quickstart.html)
- [Quickstart for tables](https://docs.datastax.com/en/astra-db-serverless/get-started/quickstart-tables.html)
- [Plan PCU groups](https://docs.datastax.com/en/astra-db-serverless/administration/plan-pcu.html)
