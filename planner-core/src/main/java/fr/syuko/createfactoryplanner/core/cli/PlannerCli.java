package fr.syuko.createfactoryplanner.core.cli;

import fr.syuko.createfactoryplanner.core.io.Catalog;
import fr.syuko.createfactoryplanner.core.io.CatalogJson;
import fr.syuko.createfactoryplanner.core.io.MachineEntry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PlannerCli {

    private static final String MACHINES = "machines";

    private static final String CATALOG_OPTION = "--catalog";

    private static final String UNKNOWN = "unknown";

    private PlannerCli() {
    }

    public static void main(String[] args) {
        try {
            System.out.print(run(List.of(args)));
        } catch (IllegalArgumentException | IOException failure) {
            System.err.println(failure.getMessage());
            System.exit(1);
        }
    }

    static String run(List<String> args) throws IOException {
        if (args.isEmpty()) {
            throw new IllegalArgumentException(usage());
        }
        String command = args.getFirst();
        if (!MACHINES.equals(command)) {
            throw new IllegalArgumentException("unknown command " + command + "\n" + usage());
        }
        return machines(CatalogJson.read(readCatalog(catalogPath(args))));
    }

    private static String readCatalog(Path catalog) throws IOException {
        if (!Files.isRegularFile(catalog)) {
            throw new IllegalArgumentException("no catalog at " + catalog.toAbsolutePath() + System.lineSeparator() + "run /cfp dump catalog in game first");
        }
        return Files.readString(catalog, StandardCharsets.UTF_8);
    }

    private static Path catalogPath(List<String> args) {
        int option = args.indexOf(CATALOG_OPTION);
        if (option < 0 || option + 1 >= args.size()) {
            throw new IllegalArgumentException(CATALOG_OPTION + " is required\n" + usage());
        }
        return Path.of(args.get(option + 1));
    }

    static String machines(Catalog catalog) {
        StringBuilder report = new StringBuilder();
        report.append(catalog.machines().size())
              .append(" machines read at ")
              .append(catalog.meta().readAt())
              .append(" from ")
              .append(catalog.meta().world() == null
                      ? UNKNOWN
                      : catalog.meta().world())
              .append(", Create ")
              .append(catalog.meta().createVersion() == null
                      ? UNKNOWN
                      : catalog.meta().createVersion())
              .append('\n');
        for (MachineEntry entry : catalog.machines()) {
            report.append(String.format(Locale.ROOT,
                                        "  %-20s rpm %3d..%-3d default %-3d %s%s%n",
                                        entry.id(),
                                        entry.minimumRpm(),
                                        entry.maximumRpm(),
                                        entry.defaultRpm(),
                                        stress(entry),
                                        scalars(entry)));
        }
        return report.toString();
    }

    private static String stress(MachineEntry entry) {
        return entry.readsItsStressImpact()
               ? String.format(Locale.ROOT, "su/rpm %.2f", entry.stressImpactPerRpm())
               : "su/rpm unread";
    }

    private static String scalars(MachineEntry entry) {
        if (entry.formulaScalars().isEmpty()) {
            return "";
        }
        StringBuilder rendered = new StringBuilder();
        for (Map.Entry<String, Long> scalar : entry.formulaScalars().entrySet()) {
            rendered.append(' ').append(scalar.getKey()).append('=').append(scalar.getValue());
        }
        return rendered.toString();
    }

    private static String usage() {
        return "usage: machines --catalog <file>";
    }
}