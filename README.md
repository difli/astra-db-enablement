# Astra DB Enablement

Workshop in a Box for developers and architects who need to become productive with **Astra DB Serverless**.

This is not Apache Cassandra administrator training. It is not a multi-day certification. It is a focused, hands-on path you can complete in about **two hours**.

## What you will be able to do

After this workshop you can answer:

1. What is Astra DB?
2. Why would I use it?
3. When should I use it?
4. When should I **not** use it?
5. How is it different from self-managed Cassandra?
6. How do I model data correctly?
7. How do I connect a Java application?
8. Which limits and constraints actually matter?
9. What are the most common adoption mistakes?
10. How do I get started immediately?

The [decision tree](01-why-astra-db/why-astra-db.md) is how you answer 2–5 in the room. Take-home (not on the clock): [reference architectures](reference-architectures/reference-architectures.md), the [architecture review checklist](ARCHITECTURE-REVIEW-CHECKLIST.md), and the [Oracle to Astra DB assessment](ORACLE-TO-ASTRA-ASSESSMENT.md) (modernization opportunity discovery).

## Who this is for

Solution architects, developers, data engineers, technical consultants, solution engineers, and customer technical teams. Little or no Astra DB experience is assumed. Cassandra experience helps for data modelling, but is not required.

## How the workshop is organised

| Module | What it is |
|---|---|
| [00 Get started](00-get-started/get-started.md) | Pre-work: account, database, token |
| [01 Why Astra DB](01-why-astra-db/why-astra-db.md) | Decision tree: fit, non-fit, vs self-managed Cassandra |
| [02 Astra fundamentals](02-astra-fundamentals/astra-fundamentals.md) | Databases, keyspaces, tables vs collections, tokens |
| [03 Data modelling](03-data-modeling/data-modeling.md) | Query-first design. Starts **Lab 1 (CQL)** |
| [04 Astra-specific behaviour](04-astra-specific-behavior/astra-specific-behavior.md) | Ignored CQL, behavioural limits, slim PCU. Finishes **Lab 1** |
| [05 Java development](05-java-development/java-development.md) | Connect, read, write, query. Starts **Lab 2 (Data API)** |
| [06 Data API and vector search](06-data-api-and-vector-search/data-api-and-vector-search.md) | Knowledge Search: ANN + filters. Finishes **Lab 2** |

There are **two labs**, one per primary API. Hands-on is on the module pages: CQL in [03](03-data-modeling/data-modeling.md)–[04](04-astra-specific-behavior/astra-specific-behavior.md), Java Data API in [05](05-java-development/java-development.md)–[06](06-data-api-and-vector-search/data-api-and-vector-search.md).

Suggested delivery time is in [FACILITATOR.md](FACILITATOR.md). Module pages do not carry a stopwatch. Numeric quotas are not copied into the modules; use [database limits](https://docs.datastax.com/en/astra-db-serverless/databases/database-limits.html).

## Use cases

Three customer-neutral patterns. Sketches: [reference architectures](reference-architectures/reference-architectures.md). Review questions: [architecture review checklist](ARCHITECTURE-REVIEW-CHECKLIST.md).

| Pattern | Used to teach |
|---|---|
| Enterprise Identity Platform | Fast lookups, query-first tables, Java CRUD |
| Event Inbox | Deduplication, TTL, bucketing, tombstones, Astra DDL limits |
| Knowledge Search | Collections, embeddings, ANN vector search, metadata filters |

No customer names. No industry lock-in.

## What you need

- An Astra account (free plan is enough for the labs)
- A **Serverless (vector)** database
- This repository cloned locally (required for Lab 2)
- Java 17 or later (21 recommended)
- Apache Maven 3.9 or later
- A browser

You do **not** need Docker, a Cassandra cluster, Spring Boot, or `nodetool`.

## Primary API strategy

- **Data API** first for application development (`astra-db-java`)
- **CQL** where it teaches modelling and Astra-specific DDL behaviour
- **Java** only — one sample application, no Spring Boot

This repository does not cover Langflow, GraphQL, or the deprecated Document API.

## Source of truth

Astra DB Serverless documentation wins whenever it conflicts with Cassandra folklore. Mapping: [SOURCES.md](SOURCES.md).

## Start here

1. Complete [00 Get started](00-get-started/get-started.md)
2. Work through modules 01–06 in order (labs run on those pages)
3. Take-home: [reference architectures](reference-architectures/reference-architectures.md) and the [architecture review checklist](ARCHITECTURE-REVIEW-CHECKLIST.md)

If you are delivering this as a session, read [FACILITATOR.md](FACILITATOR.md) first.
