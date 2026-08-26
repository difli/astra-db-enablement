# Architecture review checklist

Take-home. Not a workshop module. Not on the two-hour clock.

Use after [the decision tree](01-why-astra-db/why-astra-db.md) and [reference architectures](reference-architectures/reference-architectures.md). Numeric quotas live on [database limits](https://docs.datastax.com/en/astra-db-serverless/databases/database-limits.html).

## Fit

- [ ] Every hot path is a known access pattern, not ad-hoc SQL or joins
- [ ] Each hot query can hit **one partition** (or a small, bounded set)
- [ ] You do not need to set RF, compaction, `cassandra.yaml`, UDFs, or materialized views
- [ ] You do not need write consistency `ONE`, `ANY`, or `LOCAL_ONE`
- [ ] Vector search, if any, is **ANN + metadata**, not exact KNN

## Enterprise Identity

- [ ] One table per query (`users_by_id`, `users_by_email`, `entitlements_by_user`, `sessions_by_id`)
- [ ] Application **dual-writes** `users_by_id` and `users_by_email`
- [ ] Sessions use table TTL; they are not unbounded
- [ ] Email lookup is not `ALLOW FILTERING` or a secondary index as the hot path
- [ ] Identity is **tables**, not a collection

## Event Inbox

- [ ] `event_id` is in the primary key so retries are idempotent
- [ ] Partition key includes a **time bucket** (`consumer_id`, `bucket`)
- [ ] Table TTL covers retention
- [ ] You are not depending on `compaction` or `gc_grace_seconds` (Astra ignores them)

## Knowledge Search

- [ ] Collection (or vector table) stores embeddings with metadata filters
- [ ] Insert and query use the **same** embedding model
- [ ] Reads are ANN **with** a metadata filter, not an id lookup
- [ ] Vector **query** capacity is planned as single-region (extra regions can replicate data; they do not add a vector PCU)
- [ ] Serverless (vector) PCU group is **one unit** if you use PCUs; prefer Cache optimized

## Platform

- [ ] Keyspaces are created in the portal or DevOps API, not with CQL `CREATE KEYSPACE`
- [ ] DDL warnings are treated as real (`WITH` properties that Astra ignores)
- [ ] Data API reads and writes are assumed `LOCAL_QUORUM`
- [ ] Design is checked against current [database limits](https://docs.datastax.com/en/astra-db-serverless/databases/database-limits.html), not a copied quota table
