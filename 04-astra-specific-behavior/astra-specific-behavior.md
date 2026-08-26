# 04 — Astra-specific behaviour and limitations

Cassandra-experienced teams need this module most. Astra DB will **accept** statements that look like admin CQL and then **quietly not do** what you asked.

After this module, finish **[Lab 1 — Develop with CQL](../labs/lab-1-cql.md) Part B**.

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

## Lab

Complete **[Lab 1 Part B](../labs/lab-1-cql.md)** — Event Inbox, TTL, ignored `WITH`, failed `CREATE KEYSPACE`.

## Next

[05 — Java development](../05-java-development/java-development.md)
