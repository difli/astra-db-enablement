-- Lab 1 worked answers. Run in the Astra CQL console.
-- Replace default_keyspace if yours differs.

USE default_keyspace;

-- Part A: identity (one table per query)

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
   AND comment = 'Identity sessions; TTL is applied';

CREATE TABLE IF NOT EXISTS users_by_email (
  email text PRIMARY KEY,
  user_id uuid
);

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

SELECT * FROM users_by_id WHERE user_id = 11111111-1111-1111-1111-111111111111;
SELECT * FROM users_by_email WHERE email = 'alex@example.com';
SELECT * FROM entitlements_by_user WHERE user_id = 11111111-1111-1111-1111-111111111111;
SELECT * FROM sessions_by_id WHERE session_id = 22222222-2222-2222-2222-222222222222;

-- Anti-pattern: email is not the partition key of users_by_id.
-- SELECT * FROM users_by_id WHERE email = 'alex@example.com';  -- fails
-- ALLOW FILTERING may work here; do not use it as the hot path.

-- Part B: event inbox

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

-- Ignored WITH: table is created; compaction / gc_grace_seconds / caching are not applied.
CREATE TABLE IF NOT EXISTS inbox_with_cassandra_habits (
  consumer_id text,
  event_id uuid,
  PRIMARY KEY (consumer_id, event_id)
) WITH compaction = {'class': 'LeveledCompactionStrategy'}
   AND gc_grace_seconds = 86400
   AND caching = {'keys': 'ALL'};

-- Expected to fail: CQL cannot create keyspaces on Astra DB.
-- CREATE KEYSPACE IF NOT EXISTS workshop_ks
--   WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
