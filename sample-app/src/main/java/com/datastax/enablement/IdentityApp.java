package com.datastax.enablement;

import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.definition.rows.Row;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Lab 2 part A: write and read an identity profile by partition key.
 *
 * <p>Requires table {@code users_by_id} from Lab 1.
 */
public final class IdentityApp {

  static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  public static void main(String[] args) {
    Database database = Connect.database();
    upsertAndRead(database);
  }

  static void upsertAndRead(Database database) {
    Table<Row> users = database.getTable("users_by_id");

    users.insertOne(
        new Row()
            .add("user_id", USER_ID)
            .addText("email", "alex@example.com")
            .addText("display_name", "Alex")
            .addText("status", "active")
            .add("updated_at", Instant.now()));

    System.out.println("Wrote users_by_id partition " + USER_ID);

    Optional<Row> found = users.findOne(Filters.eq("user_id", USER_ID));
    if (found.isEmpty()) {
      throw new IllegalStateException(
          "Read missed. Confirm Lab 1 created users_by_id in this keyspace.");
    }
    System.out.println("Read profile: " + found.get());
  }
}
