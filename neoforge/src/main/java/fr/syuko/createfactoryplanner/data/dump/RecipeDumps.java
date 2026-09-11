package fr.syuko.createfactoryplanner.data.dump;

import fr.syuko.createfactoryplanner.CreateFactoryPlanner;
import fr.syuko.createfactoryplanner.data.recipe.HarvestedRecipe;
import fr.syuko.createfactoryplanner.data.recipe.RecipeBindings;
import fr.syuko.createfactoryplanner.data.recipe.RecipeSource;
import fr.syuko.createfactoryplanner.data.recipe.RecipeTypeEntry;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class RecipeDumps {

    private static final String UNSUPPORTED = "unsupported";

    private static final String CREATE = "create";

    private RecipeDumps() {
    }

    public static RecipeDump of(RecipeSource source) {
        List<RecipeTypeEntry> types = source.knownTypes();
        List<HarvestedRecipe> recipes = source.allRecipes();
        List<RuleEntry> rules = rulesOf(recipes);
        return new RecipeDump(metaOf(types, recipes), types, rules, recipes, problemsOf(types, rules, recipes));
    }

    private static RecipeDump.DumpMeta metaOf(List<RecipeTypeEntry> types, List<HarvestedRecipe> recipes) {
        return new RecipeDump.DumpMeta(Instant.now().toString(),
                                       versionOf(CreateFactoryPlanner.MODID),
                                       versionOf(CREATE),
                                       types.size(),
                                       recipes.size(),
                                       loadedMods());
    }

    private static List<RuleEntry> rulesOf(List<HarvestedRecipe> recipes) {
        Map<String, Long> harvested = recipes.stream()
                                             .flatMap(recipe -> recipe.rules().stream())
                                             .collect(Collectors.groupingBy(Function.identity(),
                                                                            Collectors.counting()));
        return RecipeBindings.rules()
                             .stream()
                             .map(rule -> new RuleEntry(rule.label(),
                                                        rule.machine().value(),
                                                        rule.recipeTypes(),
                                                        rule.configFlag(),
                                                        rule.enabled(),
                                                        harvested.getOrDefault(rule.label(), 0L).intValue()))
                             .toList();
    }

    private static List<String> problemsOf(List<RecipeTypeEntry> types,
                                           List<RuleEntry> rules,
                                           List<HarvestedRecipe> recipes) {
        List<String> problems = new ArrayList<>(RecipeBindings.tableProblems());

        types.stream()
             .filter(type -> !type.bound())
             .filter(type -> type.recipeCount() > 0)
             .forEach(type -> problems.add("unbound recipe type: " + type.id() + " (" + type.recipeCount() + " recipes)"));

        rules.stream()
             .filter(RuleEntry::enabled)
             .filter(rule -> rule.harvestedCount() == 0)
             .forEach(rule -> problems.add("rule matched no recipe: " + rule.label()));

        Map<String, String> machineByRule = rules.stream()
                                                 .collect(Collectors.toMap(RuleEntry::label, RuleEntry::machine));
        recipes.stream()
               .filter(recipe -> hasCompetingRules(recipe, machineByRule))
               .forEach(recipe -> problems.add("recipe bound twice to the same machine: " + recipe.recipe().id()));

        recipes.stream()
               .filter(recipe -> UNSUPPORTED.equals(recipe.recipe().reader()))
               .flatMap(recipe -> recipe.recipe().notes().stream())
               .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
               .forEach((note, count) -> problems.add(note + ": " + count + " recipes"));

        problems.sort(String::compareTo);
        return List.copyOf(problems);
    }

    private static boolean hasCompetingRules(HarvestedRecipe recipe, Map<String, String> machineByRule) {
        return recipe.rules().stream().map(machineByRule::get).distinct().count() < recipe.rules().size();
    }

    private static List<String> loadedMods() {
        return ModList.get().getMods().stream().map(mod -> mod.getModId() + "@" + mod.getVersion()).sorted().toList();
    }

    private static String versionOf(String modId) {
        return ModList.get()
                      .getModContainerById(modId)
                      .map(ModContainer::getModInfo)
                      .map(info -> info.getVersion().toString())
                      .orElse("absent");
    }
}