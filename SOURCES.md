# Sources

Every technical claim in this workshop is grounded in Astra DB Serverless documentation. When Cassandra training material and Astra documentation conflict, Astra documentation wins.

## Product and getting started

| Topic | Page |
|---|---|
| Product overview | https://docs.datastax.com/en/astra-db-serverless/index.html |
| About Astra DB Serverless | https://docs.datastax.com/en/astra-db-serverless/get-started/astra-db-introduction.html |
| Quickstart | https://docs.datastax.com/en/astra-db-serverless/get-started/quickstart.html |
| Vector concepts | https://docs.datastax.com/en/astra-db-serverless/get-started/vector-concepts.html |
| Create a database | https://docs.datastax.com/en/astra-db-serverless/databases/create-database.html |
| Collections and tables | https://docs.datastax.com/en/astra-db-serverless/databases/manage-collections.html |

## Connect and APIs

| Topic | Page |
|---|---|
| Connection methods | https://docs.datastax.com/en/astra-db-serverless/databases/connection-methods-comparison.html |
| Data API clients | https://docs.datastax.com/en/astra-db-serverless/api-reference/dataapiclient.html |
| Quickstart for tables | https://docs.datastax.com/en/astra-db-serverless/get-started/quickstart-tables.html |
| Find rows | https://docs.datastax.com/en/astra-db-serverless/api-reference/row-methods/find-many.html |
| Application tokens | https://docs.datastax.com/en/astra-db-serverless/administration/manage-application-tokens.html |
| Secure Connect Bundle | https://docs.datastax.com/en/astra-db-serverless/databases/secure-connect-bundle.html |
| Java driver (CQL path) | https://docs.datastax.com/en/astra-db-serverless/drivers/java-driver.html |

## CQL, limits, and platform behaviour

| Topic | Page |
|---|---|
| CQL for Astra DB (ignored table properties) | https://docs.datastax.com/en/astra-db-serverless/cql/develop-with-cql.html |
| Database limits | https://docs.datastax.com/en/astra-db-serverless/databases/database-limits.html |
| Multi-region | https://docs.datastax.com/en/astra-db-serverless/databases/manage-regions.html |
| Shared responsibility | https://docs.datastax.com/en/astra-db-serverless/shared-responsibility-model.html |

## Capacity (PCU)

| Topic | Page |
|---|---|
| Provisioned Capacity Units | https://docs.datastax.com/en/astra-db-serverless/administration/provisioned-capacity-units.html |
| Plan PCU groups (vector = 1 unit) | https://docs.datastax.com/en/astra-db-serverless/administration/plan-pcu.html |
| Create PCU groups | https://docs.datastax.com/en/astra-db-serverless/administration/create-pcu.html |
| PCU FAQs | https://docs.datastax.com/en/astra-db-serverless/pcu-faqs.html |

## Vector search

| Topic | Page |
|---|---|
| Find data with vector search | https://docs.datastax.com/en/astra-db-serverless/databases/vector-search.html |

## Modelling pedagogy adapted from

Query-first design, partition keys, clustering, denormalisation, bucketing, wide/hot partitions, TTL, tombstones, and anti-patterns are adapted from [michelderu/cassandra-fundamentals](https://github.com/michelderu/cassandra-fundamentals) (data-modelling track only). Architecture, gossip, repairs, and Docker cluster labs are not used.

## Explicitly not used

Langflow, GraphQL / Stargate GraphQL, deprecated Document API, customer-specific material, and Cassandra cluster-operations documentation.
