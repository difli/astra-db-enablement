# Lab 1 — Develop with CQL

**API:** CQL console in the Astra Portal  
**Use cases:** Enterprise Identity, Event Inbox  
**When:** Part A during module 03. Part B during module 04.

Open your Serverless (vector) database → **CQL console**. Select your keyspace (often `default_keyspace`).

```sql
USE default_keyspace;
```

If your keyspace name differs, use that name everywhere below.

---

## Part A — Enterprise Identity (module 03)

### A1. Create query-first tables

```sql
CREATE TABLE IF NOT EXISTS users_by_id (
  user_id uuid PRIMARY KEY,
  email text,
  display_name text,
  status text,
  updated_at timestamp
);

CREATE TABLE IF NOT EXISTS entitlements_by_user (
  user_id uuid,
  entitlement text,
  granted_at timestamp,
  PRIMARY KEY (user_id, entitlement)
);

CREATE TABLE IF NOT EXISTS sessions_by_id (
  session_id uuid PRIMARY KEY,
  user_id uuid,
  created_at timestamp,
  last_seen timestamp
) WITH default_time_to_live = 86400
   AND comment = 'Identity sessions; TTL is one of the table properties Astra actually applies';

CREATE TABLE IF NOT EXISTS users_by_email (
  email text PRIMARY KEY,
  user_id uuid
);
```

`DESCRIBE TABLE users_by_id;` — name the partition key. There is no clustering column. That is correct for Q1 (get profile by id).

### A2. Write and read one user

```sql
INSERT INTO users_by_id (user_id, email, display_name, status, updated_at)
VALUES (11111111-1111-1111-1111-111111111111, 'alex@example.com', 'Alex', 'active', toTimestamp(now()));

INSERT INTO users_by_email (email, user_id)
VALUES ('alex@example.com', 11111111-1111-1111-1111-111111111111);

INSERT INTO entitlements_by_user (user_id, entitlement, granted_at)
VALUES (11111111-1111-1111-1111-111111111111, 'invoice.read', toTimestamp(now()));

INSERT INTO entitlements_by_user (user_id, entitlement, granted_at)
VALUES (11111111-1111-1111-1111-111111111111, 'invoice.write', toTimestamp(now()));

INSERT INTO sessions_by_id (session_id, user_id, created_at, last_seen)
VALUES (22222222-2222-2222-2222-222222222222, 11111111-1111-1111-1111-111111111111, toTimestamp(now()), toTimestamp(now()));
```

Queries — each is a **single partition**:

```sql
SELECT * FROM users_by_id WHERE user_id = 11111111-1111-1111-1111-111111111111;

SELECT * FROM users_by_email WHERE email = 'alex@example.com';

SELECT * FROM entitlements_by_user WHERE user_id = 11111111-1111-1111-1111-111111111111;

SELECT * FROM sessions_by_id WHERE session_id = 22222222-2222-2222-2222-222222222222;
```

### A3. Feel the anti-pattern (do not ship this)

```sql
SELECT * FROM users_by_id WHERE email = 'alex@example.com';
```

This fails without `ALLOW FILTERING` because `email` is not the partition key. Now try:

```sql
SELECT * FROM users_by_id WHERE email = 'alex@example.com' ALLOW FILTERING;
```

It may return a row on this tiny table. That does **not** make it a production path. The production path is `users_by_email`.

**Part A deliverable:** You can explain why identity is four tables, not one.

---

## Part B — Event Inbox and Astra DDL (module 04)

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

You should see **two** rows (two `event_id`s), latest first. The duplicate insert did not create a third row.

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

Read the **warning**. The table is created. Compaction, `gc_grace_seconds`, and caching were **not** applied.

`DESCRIBE TABLE inbox_with_cassandra_habits;` — you will not see your LCS settings as an applied choice you control. UCS is the platform strategy.

### B3. Keyspaces are not CQL

```sql
CREATE KEYSPACE IF NOT EXISTS workshop_ks
  WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
```

This **fails**. Create keyspaces in the Astra Portal or DevOps API. Replication factor is not yours to set (RF=3, `NetworkTopologyStrategy`).

**Part B deliverable:** Inbox is idempotent and bucketed; you have seen a warning for ignored table properties; you have seen `CREATE KEYSPACE` reject.

---

## Stretch (only if time)

```sql
SELECT * FROM inbox_by_consumer WHERE payload = 'invoice.paid' ALLOW FILTERING;
```

Why is this the wrong primary access pattern even when it works?

---

## Next

Return to [module 04](../04-astra-specific-behavior/astra-specific-behavior.md) if you still need to finish reading, then go to [05 Java development](../05-java-development/java-development.md).

Compare with [solutions/lab-1-cql.cql](../solutions/lab-1-cql.cql).
