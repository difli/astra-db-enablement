package com.datastax.enablement;

import com.datastax.astra.client.databases.Database;

/** Runs Lab 2 parts A and B in one process. */
public final class WorkshopApp {

  public static void main(String[] args) {
    Database database = Connect.database();
    IdentityApp.upsertAndRead(database);
    KnowledgeSearchApp.search(database);
  }
}
