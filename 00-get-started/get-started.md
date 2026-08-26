# 00 — Get started

Do this **before** the workshop. The two-hour session assumes you already have a database.

This answers: *How do I get started immediately?*

## Create an Astra account

1. Open the [Astra signup](https://www.datastax.com/products/datastax-astra) page and create an account, or sign in.
2. Free plan is enough for this workshop.

## Create a Serverless (vector) database

1. In the Astra Portal, create a database.
2. Choose **Serverless (vector)**. You need vector support for Lab 2.
3. Pick a cloud region close to you. Availability of vector databases varies by region — see [database regions](https://docs.datastax.com/en/astra-db-serverless/databases/regions.html).
4. Keep the default keyspace name unless you have a reason to change it. Note the keyspace name. New Serverless (vector) databases typically start with `default_keyspace`.
5. Wait until the database status is **Active**.

You cannot create a keyspace with CQL on Astra DB. Keyspaces are created in the portal or with the DevOps API. That is an Astra-specific fact you will see again in module 04.

## Copy connection details

From the database page, copy:

| Value | Used for |
|---|---|
| **API endpoint** | Data API (Lab 2). Form: `https://DATABASE_ID-REGION.apps.astra.datastax.com` |
| **Application token** | Data API and CQL console. Starts with `AstraCS:` |
| **Keyspace name** | Both labs |

Generate the token from **Database details** / **Generate token**. Store it like a password. The portal shows it once.

You do **not** need a Secure Connect Bundle for this workshop. The SCB is for Cassandra drivers and `cqlsh` outside the portal. Lab 2 uses the Data API client (`astra-db-java`), which authenticates with endpoint + token.

## Confirm the CQL console

1. Open your database in the Astra Portal.
2. Click **CQL console**.
3. Wait for the `token@cqlsh>` prompt.

If the prompt appears, pre-work is done.

## Set environment variables for Lab 2

On macOS / Linux:

```bash
export API_ENDPOINT="https://YOUR_DB_ID-YOUR_REGION.apps.astra.datastax.com"
export APPLICATION_TOKEN="AstraCS:..."
export KEYSPACE_NAME="default_keyspace"
```

On Windows PowerShell:

```powershell
$env:API_ENDPOINT="https://YOUR_DB_ID-YOUR_REGION.apps.astra.datastax.com"
$env:APPLICATION_TOKEN="AstraCS:..."
$env:KEYSPACE_NAME="default_keyspace"
```

Do not commit tokens to git.

## Free-plan note

Inactive free-plan databases can hibernate. If your database is asleep when the workshop starts, open it in the portal and wait until it is **Active** again.

## What you will not do

- You will not run Docker Cassandra.
- You will not download `nodetool`.
- You will not tune `cassandra.yaml`.

Astra DB is a managed service. You connect. You model. You query.

## Next

[01 — Why Astra DB](../01-why-astra-db/why-astra-db.md)
