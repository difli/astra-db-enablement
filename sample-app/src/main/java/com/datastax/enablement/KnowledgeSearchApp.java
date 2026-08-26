package com.datastax.enablement;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.databases.Database;

/**
 * Lab 2 part B: Knowledge Search with ANN and a metadata filter.
 *
 * <p>Vectors are synthetic 5-d embeddings so the lab runs in any Serverless
 * (vector) region without vectorize. {@code deleteAll} makes re-runs safe.
 */
public final class KnowledgeSearchApp {

  static final String COLLECTION = "knowledge";

  public static void main(String[] args) {
    Database database = Connect.database();
    search(database);
  }

  static void search(Database database) {
    CollectionDefinition definition =
        new CollectionDefinition().vector(5, SimilarityMetric.COSINE);
    Collection<Document> knowledge;
    try {
      knowledge = database.createCollection(COLLECTION, definition);
    } catch (RuntimeException alreadyExists) {
      knowledge = database.getCollection(COLLECTION);
    }

    knowledge.deleteAll();

    knowledge.insertOne(
        article(
            "k1",
            "identity",
            "Reset a work password from the identity portal. We email a "
                + "one-time link to the address on your employee profile. Other "
                + "signed-in sessions stay open until they expire, unless you "
                + "choose Sign out everywhere.",
            new float[] {0.12f, 0.88f, 0.05f, 0.10f, 0.40f}));
    knowledge.insertOne(
        article(
            "k2",
            "identity",
            "Find an employee by work email in the company directory. Search "
                + "is keyed by email. Do not download the full employee list to "
                + "look up one person.",
            new float[] {0.10f, 0.85f, 0.08f, 0.12f, 0.38f}));
    knowledge.insertOne(
        article(
            "k3",
            "finance",
            "Invoice webhooks can arrive more than once. Use the event id to "
                + "ignore duplicates. Process invoice.paid only the first time "
                + "you see that id.",
            new float[] {0.70f, 0.10f, 0.15f, 0.60f, 0.05f}));
    knowledge.insertOne(
        article(
            "k4",
            "platform",
            "Help-center semantic search runs in a single region. Extra "
                + "database regions can replicate documents; they do not add "
                + "another vector-search capacity unit.",
            new float[] {0.20f, 0.25f, 0.80f, 0.15f, 0.10f}));

    System.out.println("Inserted Knowledge Search documents into '" + COLLECTION + "'");

    Filter topic = Filters.eq("topic", "identity");
    CollectionFindOptions options =
        new CollectionFindOptions()
            .sort(Sort.vector(new float[] {0.11f, 0.90f, 0.04f, 0.12f, 0.38f}))
            .limit(3);

    System.out.println("ANN neighbours for topic=identity:");
    knowledge.find(topic, options).forEach(document -> System.out.println("  " + document));
  }

  private static Document article(String id, String topic, String text, float[] vector) {
    return new Document()
        .id(id)
        .append("topic", topic)
        .append("text", text)
        .vector(vector);
  }
}
