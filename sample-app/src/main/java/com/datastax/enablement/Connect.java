package com.datastax.enablement;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.databases.Database;

/**
 * Builds a Data API {@link Database} from environment variables.
 *
 * <p>Required: {@code API_ENDPOINT}, {@code APPLICATION_TOKEN}.
 * Optional: {@code KEYSPACE_NAME} (defaults to {@code default_keyspace}).
 */
public final class Connect {

  private Connect() {}

  public static Database database() {
    String endpoint = env("API_ENDPOINT");
    String token = env("APPLICATION_TOKEN");
    String keyspace =
        System.getenv().getOrDefault("KEYSPACE_NAME", "default_keyspace");

    DataAPIClient client = new DataAPIClient(token);
    Database database = client.getDatabase(endpoint, keyspace);
    System.out.println("Connected to " + endpoint + " keyspace=" + keyspace);
    return database;
  }

  private static String env(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(
          "Set environment variable " + name + " (see 00-get-started)");
    }
    return value;
  }
}
