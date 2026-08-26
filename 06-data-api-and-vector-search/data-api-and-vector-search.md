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

- Not a replacement for `WHERE user_id = ...`. Identity stays on tables.
- Not exact match.
- Not multi-region **vector-query HA** today (module 04). Extra regions can replicate documents; they do not add another vector PCU.
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

`createCollection` fails if `knowledge` already exists. The sample app catches that and calls `getCollection`, then `deleteAll` so Lab 2 Part B can be re-run without `DOCUMENT_ALREADY_EXISTS`. It inserts four help-center articles; the full texts live in [`datasets/knowledge-notes.json`](../datasets/knowledge-notes.json).

You can also put vectors on **tables**. Collections are the faster teaching surface for document-shaped search.

## Platform constraints

From [module 04](../04-astra-specific-behavior/astra-specific-behavior.md) and [plan PCU groups](https://docs.datastax.com/en/astra-db-serverless/administration/plan-pcu.html):

- Vector PCU groups are **one unit** (no autoscaling, no burst).
- Plan Knowledge Search **query** capacity as single-region today. Extra regions can replicate data; each vector region is still one PCU.
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
- Why adding a region is not extra vector-query capacity

Walk the [decision tree](../01-why-astra-db/why-astra-db.md) once more. Then keep [reference architectures](../reference-architectures/reference-architectures.md) and the [architecture review checklist](../ARCHITECTURE-REVIEW-CHECKLIST.md) for later.

## Optional next reading

- [Quickstart for collections](https://docs.datastax.com/en/astra-db-serverless/get-started/quickstart.html)
- [Quickstart for tables](https://docs.datastax.com/en/astra-db-serverless/get-started/quickstart-tables.html)
- [Plan PCU groups](https://docs.datastax.com/en/astra-db-serverless/administration/plan-pcu.html)
