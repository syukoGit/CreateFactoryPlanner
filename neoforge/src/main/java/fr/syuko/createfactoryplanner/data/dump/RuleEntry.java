package fr.syuko.createfactoryplanner.data.dump;

import java.util.List;

public record RuleEntry(String label, String machine, List<String> recipeTypes, String configFlag, boolean enabled,
                        int harvestedCount) {
}