# Facilitator guide

Internal delivery notes for a ~2 hour Astra DB enablement session. Do not print this as participant slides. Learners should use the module pages and labs.

## Stance

- Astra DB first. Not Cassandra operations.
- Data modelling is the largest technical block. Protect it if you run long.
- Two labs only: CQL, then Data API.
- The [decision tree](01-why-astra-db/why-astra-db.md) is module 01. [Reference architectures](reference-architectures/reference-architectures.md) and [ARCHITECTURE-REVIEW-CHECKLIST.md](ARCHITECTURE-REVIEW-CHECKLIST.md) are take-home, not a seventh module.
- Limitations before production habits form. Module 04 is behavioural (ignored `WITH`, vector = 1 PCU), not a quota spreadsheet.
- Customer-neutral. No customer names.

## Suggested clock

Use this as a facilitator sheet. Do not stamp these minutes on learner pages.

| Clock | Duration | What |
|---|---|---|
| Before the room | 15 min | Pre-work: [00-get-started](00-get-started/get-started.md) |
| 0:00 | 15 min | [01 Why Astra DB](01-why-astra-db/why-astra-db.md) — walk the tree |
| 0:15 | 20 min | [02 Fundamentals](02-astra-fundamentals/astra-fundamentals.md) |
| 0:35 | 30 min | [03 Data modelling](03-data-modeling/data-modeling.md) + Lab 1 part A |
| 1:05 | 15 min | [04 Astra-specific behaviour](04-astra-specific-behavior/astra-specific-behavior.md) + Lab 1 part B |
| 1:20 | 20 min | [05 Java development](05-java-development/java-development.md) + Lab 2 part A |
| 1:40 | 10 min | [06 Data API and vector search](06-data-api-and-vector-search/data-api-and-vector-search.md) + Lab 2 part B |
| 1:50 | 10 min | Buffer / tree recap / Q&A |

Hands-on is roughly half the session.

## If you run long

Cut in this order:

1. Q&A buffer
2. Extra discussion on the decision tree (still walk it once)
3. Vector lab polish in module 06 (keep ANN + metadata filter)

Do **not** cut data modelling or Lab 1 part A. Do **not** spend the buffer on PCU billing.

## Pre-work you must enforce

Ask participants to finish 00 **before** they sit down. The two-hour clock cannot absorb account creation.

Confirm each person has:

- A Serverless **(vector)** database in **Active** status
- An API endpoint
- An application token
- The CQL console opening in the Astra Portal
- This repository cloned locally (Lab 2)

Free-plan databases can hibernate. If a database is asleep, wake it before Lab 1. Current free-plan rules: [database limits](https://docs.datastax.com/en/astra-db-serverless/databases/database-limits.html).

## Talking points that people get wrong

- Astra **creates the table** even when it **ignores** `compaction`, `gc_grace_seconds`, and most other `WITH` properties. A warning is not a failure.
- Data API reads and writes use `LOCAL_QUORUM`. CQL reads support all consistency levels. CQL writes support all consistency levels except `ONE`, `ANY`, and `LOCAL_ONE`.
- Replication is not yours to change. Cite [database limits](https://docs.datastax.com/en/astra-db-serverless/databases/database-limits.html), do not recite a quota table.
- Vector search is **ANN**, not exact KNN.
- PCU **types**: Small, Medium, General purpose, Cache optimized. Cache optimized for vector. Specs live in the [PCU docs](https://docs.datastax.com/en/astra-db-serverless/administration/provisioned-capacity-units.html).
- **Serverless (vector) PCU groups are exactly one unit.** No autoscaling. No burst. Extra regions can replicate Knowledge Search documents; they do not add vector-query capacity.

## Lab facilitation

**Lab 1 (CQL)** — CQL console in the portal. No local cluster.

- Part A: identity tables, insert, select by partition key
- Part B: inbox + TTL, then the ignored `WITH` demonstration and `CREATE KEYSPACE` failure

**Lab 2 (Data API)** — the Java sample app.

- Part A: connect, insert, find on `users_by_id`
- Part B: `knowledge` collection + vector find with a metadata filter

Worked answers live in [solutions/](solutions/).

## Room setup

- One shared screen for the ignored-`WITH` warning. It is the moment people remember.
- Maven and Java 17+ on laptops **before** module 05.
- Environment variables from 00: `API_ENDPOINT`, `APPLICATION_TOKEN`, `KEYSPACE_NAME`.

## Out of scope (politely shut down)

Cluster deployment, gossip, snitches, repairs, compaction tuning, `nodetool`, JMX, Spring Boot, Langflow, GraphQL, the deprecated Document API, PCU billing deep-dives, RAG product design, and customer-specific architectures.
