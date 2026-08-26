# Lab 2 — notes

The Java in `sample-app/` **is** the solution. This file is failure triage only.

## Part A

`IdentityApp` must:

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
users.findOne(Filters.eq("user_id", userId));
```

If `findOne` is empty, you inserted into a different keyspace than Lab 1, or the column names do not match the CQL table (portal-created names are case-sensitive).

## Part B

`KnowledgeSearchApp` must:

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
Filter topic = Filters.eq("topic", "identity");
CollectionFindOptions options =
    new CollectionFindOptions()
        .sort(Sort.vector(new float[] {0.11f, 0.90f, 0.04f, 0.12f, 0.38f}))
        .limit(3);
knowledge.find(topic, options);
```

If collection create fails with an index/limit error, the database already has too many collections — see [database limits](https://docs.datastax.com/en/astra-db-serverless/databases/database-limits.html). Drop an unused collection in the portal, or reuse `knowledge`.

If vector search returns unrelated topics, the filter was omitted. ANN without metadata is a grab bag.

## Architecture reminder

Do not add a second region to “make vector HA”. Extra regions can replicate documents; each vector PCU group is still one unit. See module 04 and [reference architectures](../reference-architectures/reference-architectures.md).
