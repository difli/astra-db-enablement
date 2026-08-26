# Lab 2 — notes

The Java in `sample-app/` **is** the solution. This file is failure triage only.

## Part A

`IdentityApp` must:

1. Build `DataAPIClient` with `APPLICATION_TOKEN`
2. `getDatabase(API_ENDPOINT)`
3. `getTable("users_by_id")`
4. `insertOne` a row whose partition key is `user_id`
5. `findOne(Filters.eq("user_id", …))`

If `findOne` is empty, you inserted into a different keyspace than Lab 1, or the column names do not match the CQL table (portal-created names are case-sensitive).

## Part B

`KnowledgeSearchApp` must:

1. `createCollection("knowledge", new CollectionDefinition().vector(5, COSINE))` (or get the existing collection)
2. Insert documents with `topic` + `.vector(float[5])`
3. `find` with `Filters.eq("topic", "identity")` and `Sort.vector(...)`

If collection create fails with an index/limit error, the database already has too many collections — see [database limits](https://docs.datastax.com/en/astra-db-serverless/databases/database-limits.html). Drop an unused collection in the portal, or reuse `knowledge`.

If vector search returns unrelated topics, the filter was omitted. ANN without metadata is a grab bag.

## Architecture reminder

Do not add a second region to “make vector HA”. Knowledge Search is single-PCU and not multi-region today. See module 04 and [reference architectures](../reference-architectures/reference-architectures.md).
