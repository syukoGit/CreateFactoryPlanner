package fr.syuko.createfactoryplanner.core.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class CatalogJson {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
                                                      .disableHtmlEscaping()
                                                      .serializeNulls()
                                                      .create();

    private CatalogJson() {
    }

    public static String write(Catalog catalog) {
        return GSON.toJson(catalog);
    }

    public static Catalog read(String json) {
        Catalog catalog;
        try {
            catalog = GSON.fromJson(json, Catalog.class);
        } catch (RuntimeException malformed) {
            throw new IllegalArgumentException("a catalog must be readable: " + rootCause(malformed).getMessage(),
                                               malformed);
        }
        if (catalog == null) {
            throw new IllegalArgumentException("a catalog cannot be empty");
        }
        return catalog;
    }

    private static Throwable rootCause(Throwable thrown) {
        Throwable cause = thrown;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }
}