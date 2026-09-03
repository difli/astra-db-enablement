# 00 — Get started

Clone this repository, then create your Astra account, database, and token. Later modules use them.

## Clone this repository

```bash
git clone https://github.com/difli/astra-db-enablement.git
cd astra-db-enablement
```

You need the repo on your machine for the Java lab. After you create the database, you will put the endpoint and token in `.env` here.

## Create an Astra account

1. Open the [Astra Portal](https://astra.datastax.com) and create an account, or sign in.
2. Free plan is enough for this workshop.

## Create a Serverless (vector) database

1. In the Astra Portal, create a database.
2. Choose **Serverless (vector)**. You need vector support for Lab 2.
3. Pick a cloud and region. On the **free plan**, **Serverless (vector)** is available only on **Amazon Web Services** in **`us-east-2`**. Paid orgs can use other vector regions — see [database regions](https://docs.datastax.com/en/astra-db-serverless/databases/regions.html).
4. Wait until the database goes from **Pending** to **Active**.
5. Keep the default keyspace name unless you have a reason to change it. Note the keyspace name. New Serverless (vector) databases typically start with `default_keyspace`.

You cannot create a keyspace with CQL on Astra DB. Keyspaces are created in the portal or with the DevOps API. That is an Astra-specific fact you will see again in module 04.

## Copy connection details

In the repository root, copy `.env.example` to `.env` and fill in `API_ENDPOINT`, `APPLICATION_TOKEN`, and `KEYSPACE_NAME`. `.env` is gitignored. Do not commit tokens. You load these variables in the Java lab ([module 05](../05-java-development/java-development.md)); Java does not read `.env` by itself.

From the database page, copy:

| Value | Used for |
|---|---|
| **API endpoint** | Lab 2 (Java Data API). Form: `https://DATABASE_ID-REGION.apps.astra.datastax.com` |
| **Application token** | Lab 2 (Java Data API). Starts with `AstraCS:`. Not required for the portal CQL console (Lab 1). |
| **Keyspace name** | Both labs (CQL and Java) |

Generate the token from the **Overview** tab, **Database Details**, **Generate token**. Store it like a password. The portal shows it once.

You do **not** need a Secure Connect Bundle for this workshop. The SCB is for the Cassandra Java driver and `cqlsh` outside the portal. Lab 2 is the Java lab: it uses the Data API client (`astra-db-java`) with endpoint + token.

## Confirm the CQL console

1. Open your database in the Astra Portal.
2. Click **CQL console**.
3. Wait for the `token@cqlsh>` prompt (this session uses your portal login, not the application token).

## Free-plan note

Inactive free-plan databases can hibernate. If your database is asleep when the workshop starts, open it in the portal and wait until it is **Active** again.

## What you will not do

- You will not run Docker Cassandra.
- You will not download `nodetool`.
- You will not tune `cassandra.yaml`.

Astra DB is a managed service. You connect. You model. You query.

## Next

[01 — Why Astra DB](../01-why-astra-db/why-astra-db.md)
