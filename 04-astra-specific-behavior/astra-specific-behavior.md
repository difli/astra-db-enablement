# 04 — Astra-specific behaviour and limitations

Cassandra-experienced teams need this module most. Astra DB will **accept** statements that look like admin CQL and then **quietly not do** what you asked.

At the end of this page you create the inbox table and run the ignored-`WITH` and `CREATE KEYSPACE` demonstrations.

Quotas change. Treat [database limits](https://docs.datastax.com/en/astra-db-serverless/databases/database-limits.html) as the live list. This page teaches **behaviour**.

## Unsupported table properties are ignored

If you pass unsupported DDL properties, **the statement still runs**. Astra DB **ignores** them and returns a **warning**.

The optional table properties Astra applies are `default_time_to_live` and `comment`. **`compaction` and `gc_grace_seconds` are ignored.** So are caching, compression, `nodesync`, and most other Cassandra `WITH` clauses.

```sql
CREATE TABLE IF NOT EXISTS ignored_props (
  id uuid PRIMARY KEY
) WITH compaction = {'class': 'LeveledCompactionStrategy'}
  AND gc_grace_seconds = 86400;
```

On Astra this **creates the table**. It does **not** set LCS. It does **not** set `gc_grace_seconds`.

**This is the number one migration surprise.** Read warnings. Do not copy Cassandra `WITH` clauses into Astra and assume they stuck.

Compaction is platform-managed (UCS). Full warning list: [CQL for Astra DB — unsupported values are ignored](https://docs.datastax.com/en/astra-db-serverless/cql/develop-with-cql.html).

## CQL that does not exist here

| Area | Not supported on Astra CQL |
|---|---|
| Keyspaces | `CREATE` / `ALTER` / `DROP KEYSPACE` (`DESCRIBE KEYSPACE` works) |
| Materialized views | All MV commands |
| UDFs / UDAs | All function/aggregate commands |
| Roles / grants | Astra RBAC in the portal / DevOps API, not CQL `CREATE ROLE` |

Keyspaces are created in the **Astra Portal** or the **DevOps API**. UDTs are supported; functions are not.

## Consistency, lists, and `cassandra.yaml`

- Replication per region is platform-controlled. See [replicas and consistency](https://docs.datastax.com/en/astra-db-serverless/databases/database-limits.html#replicas-and-consistency).
- Data API reads and writes use **`LOCAL_QUORUM`**.
- CQL reads: all consistency levels.
- CQL writes: all levels **except** `ONE`, `ANY`, `LOCAL_ONE`.
- You cannot `UPDATE`/`DELETE` a list **by index** (no read-before-write list ops).
- You cannot edit `cassandra.yaml`.

Current numeric guardrails (tables per database, columns, cell size, indexes, rate limits, partition-size warnings, free-plan caps) live only in [database limits](https://docs.datastax.com/en/astra-db-serverless/databases/database-limits.html). Design against that page, not against a workshop copy.

Idle on-demand databases can feel cold on a sudden spike. Warm them before a launch, or use PCUs.

## Capacity: on-demand vs PCU

**On-demand** capacity follows usage and can scale down.

**Provisioned Capacity Units (PCUs)** are an Enterprise option: a **PCU group** is reserved compute for databases in **one cloud + region**. You do not need a PCU group for the labs.

Types (fixed for the life of the group): **Small**, **Medium**, **General purpose** (default), **Cache optimized**. Prefer **Cache optimized** for vector working sets. Specs, tenant types (shared vs dedicated), and billing (RCU/HCU) are in [Provisioned Capacity Units](https://docs.datastax.com/en/astra-db-serverless/administration/provisioned-capacity-units.html).

### Vector search: one PCU; plan queries as single-region

From [Plan PCU groups](https://docs.datastax.com/en/astra-db-serverless/administration/plan-pcu.html):

1. A Serverless **(vector)** PCU group is **exactly one unit**. No autoscaling. No burst.
2. You can add extra regions so **data** replicates, but **do not design Knowledge Search as multi-region vector-query HA today.** Each vector region still has a one-PCU ceiling. Treat vector search as **single-region** unless current [plan PCU](https://docs.datastax.com/en/astra-db-serverless/administration/plan-pcu.html) and [multi-region](https://docs.datastax.com/en/astra-db-serverless/databases/manage-regions.html) docs say otherwise.
3. Vector and non-vector databases **cannot share** a PCU group.

## Adoption mistakes this module exists to prevent

1. Shipping Cassandra DDL with compaction / `gc_grace_seconds` and believing it applied
2. Ignoring [database limits](https://docs.datastax.com/en/astra-db-serverless/databases/database-limits.html) until a create/index/rate call fails
3. Using `ALLOW FILTERING` or a secondary index as the identity lookup
4. Assuming vector search is multi-region HA like a CQL table
5. Calling `CREATE KEYSPACE` from a migration script

## Lab — Event Inbox and Astra DDL (CQL console)

Same CQL console and keyspace as [module 03](../03-data-modeling/data-modeling.md).

### B1. Inbox table: bucket + idempotent key + TTL

```sql
CREATE TABLE IF NOT EXISTS inbox_by_consumer (
  consumer_id text,
  bucket text,
  event_time timestamp,
  event_id uuid,
  payload text,
  PRIMARY KEY ((consumer_id, bucket), event_time, event_id)
) WITH CLUSTERING ORDER BY (event_time DESC, event_id ASC)
   AND default_time_to_live = 604800
   AND comment = 'Seven-day inbox; bucket by UTC day';
```

Insert the same logical event twice (retry). Same primary key → one row.

```sql
INSERT INTO inbox_by_consumer (consumer_id, bucket, event_time, event_id, payload)
VALUES ('billing-service', '2026-08-26', '2026-08-26 09:15:00+0000',
        33333333-3333-3333-3333-333333333333, 'invoice.paid');

INSERT INTO inbox_by_consumer (consumer_id, bucket, event_time, event_id, payload)
VALUES ('billing-service', '2026-08-26', '2026-08-26 09:15:00+0000',
        33333333-3333-3333-3333-333333333333, 'invoice.paid');

INSERT INTO inbox_by_consumer (consumer_id, bucket, event_time, event_id, payload)
VALUES ('billing-service', '2026-08-26', '2026-08-26 09:16:00+0000',
        44444444-4444-4444-4444-444444444444, 'invoice.failed');

SELECT event_time, event_id, payload
FROM inbox_by_consumer
WHERE consumer_id = 'billing-service' AND bucket = '2026-08-26';
```

Expected: **two** rows (two `event_id`s), `invoice.failed` first (`event_time DESC`). The duplicate `invoice.paid` insert did not create a third row.

### B2. The ignored `WITH` trap

```sql
CREATE TABLE IF NOT EXISTS inbox_with_cassandra_habits (
  consumer_id text,
  event_id uuid,
  PRIMARY KEY (consumer_id, event_id)
) WITH compaction = {'class': 'LeveledCompactionStrategy'}
   AND gc_grace_seconds = 86400
   AND caching = {'keys': 'ALL'};
```

Expected: the table is **created**, plus a **warning** that unsupported properties were ignored. This is not a failure.

```sql
DESCRIBE TABLE inbox_with_cassandra_habits;
```

Expected: `DESCRIBE` does **not** show LCS, `gc_grace_seconds`, or your caching clause as settings you control. UCS is the platform strategy.

### B3. Keyspaces are not CQL

```sql
CREATE KEYSPACE IF NOT EXISTS workshop_ks
  WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
```

Expected: an **error**, not a warning. The keyspace is not created. Create keyspaces in the Astra Portal or DevOps API. Replication is platform-controlled; see [replicas and consistency](https://docs.datastax.com/en/astra-db-serverless/databases/database-limits.html#replicas-and-consistency).

**Deliverable:** Inbox is idempotent and bucketed; you have seen a warning for ignored table properties; you have seen `CREATE KEYSPACE` reject.

### Stretch (only if time)

```sql
SELECT * FROM inbox_by_consumer WHERE payload = 'invoice.paid' ALLOW FILTERING;
```

Expected: the one `invoice.paid` row. Why is this the wrong primary access pattern even when it works?

## Next

[05 — Java development](../05-java-development/java-development.md)
