package fr.syuko.createfactoryplanner.data.constants;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class OverrideLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final String FILE = "overrides.json";

    private static Path directory = Path.of("config", "createfactoryplanner");

    private static Overrides loaded;

    private static String lastProblem;

    private OverrideLoader() {
    }

    public static void readFrom(Path configDirectory) {
        directory = configDirectory;
        reload();
    }

    public static Overrides current() {
        if (loaded == null) {
            reload();
        }
        return loaded;
    }

    public static Overrides reload() {
        Path file = directory.resolve(FILE);
        lastProblem = null;
        if (!Files.isRegularFile(file)) {
            loaded = Overrides.NONE;
            return loaded;
        }
        try {
            Overrides parsed = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), Overrides.class);
            loaded = parsed == null
                     ? Overrides.NONE
                     : parsed;
        } catch (IOException | RuntimeException unreadable) {
            lastProblem = unreadable.getMessage();
            loaded = Overrides.NONE;
        }
        return loaded;
    }

    public static void invalidate() {
        loaded = null;
    }

    public static String lastProblem() {
        return lastProblem;
    }

    public static Path file() {
        return directory.resolve(FILE);
    }
}