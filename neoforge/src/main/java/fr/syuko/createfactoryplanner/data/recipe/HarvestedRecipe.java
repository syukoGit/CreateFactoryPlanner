package fr.syuko.createfactoryplanner.data.recipe;

import java.util.List;

public record HarvestedRecipe(List<String> rules, List<String> machines, RawRecipe recipe) {
}
