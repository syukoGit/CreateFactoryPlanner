package fr.syuko.createfactoryplanner.data.recipe;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.fluids.potion.PotionMixingRecipes;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.foundation.data.recipe.LogStrippingFakeRecipes;
import com.simibubi.create.infrastructure.config.AllConfigs;

import fr.syuko.createfactoryplanner.core.model.MachineId;
import fr.syuko.createfactoryplanner.data.machine.Machines;

import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class RecipeBindings {

    private static final String CRAFTING = "minecraft:crafting";

    private static final String UNCONDITIONAL = "";

    private RecipeBindings() {
    }

    public static List<HarvestRule> rules() {
        return List.of(HarvestRule.ofType("milling_millstone",
                                          "create:milling",
                                          Machines.MILLSTONE,
                                          AllRecipeTypes.MILLING::getType),
                       HarvestRule.ofType("milling_crushing_wheels",
                                          "create:milling",
                                          Machines.CRUSHING_WHEELS,
                                          AllRecipeTypes.MILLING::getType),
                       HarvestRule.ofType("crushing",
                                          "create:crushing",
                                          Machines.CRUSHING_WHEELS,
                                          AllRecipeTypes.CRUSHING::getType),
                       HarvestRule.ofType("pressing",
                                          "create:pressing",
                                          Machines.MECHANICAL_PRESS,
                                          AllRecipeTypes.PRESSING::getType),
                       HarvestRule.ofType("compacting",
                                          "create:compacting",
                                          Machines.MECHANICAL_PRESS,
                                          AllRecipeTypes.COMPACTING::getType),
                       HarvestRule.ofType("mixing",
                                          "create:mixing",
                                          Machines.MECHANICAL_MIXER,
                                          AllRecipeTypes.MIXING::getType),
                       HarvestRule.ofType("cutting",
                                          "create:cutting",
                                          Machines.MECHANICAL_SAW,
                                          AllRecipeTypes.CUTTING::getType),
                       HarvestRule.ofType("splashing",
                                          "create:splashing",
                                          Machines.ENCASED_FAN,
                                          AllRecipeTypes.SPLASHING::getType),
                       HarvestRule.ofType("haunting",
                                          "create:haunting",
                                          Machines.ENCASED_FAN,
                                          AllRecipeTypes.HAUNTING::getType),
                       HarvestRule.ofType("deploying",
                                          "create:deploying",
                                          Machines.DEPLOYER,
                                          AllRecipeTypes.DEPLOYING::getType),
                       HarvestRule.ofAdaptedType("sandpaper_polishing_deployer",
                                                 "create:sandpaper_polishing",
                                                 Machines.DEPLOYER,
                                                 AllRecipeTypes.SANDPAPER_POLISHING::getType,
                                                 DeployerApplicationRecipe::convert),
                       HarvestRule.ofAdaptedType("item_application_deployer",
                                                 "create:item_application",
                                                 Machines.DEPLOYER,
                                                 AllRecipeTypes.ITEM_APPLICATION::getType,
                                                 ManualApplicationRecipe::asDeploying),
                       HarvestRule.ofType("filling", "create:filling", Machines.SPOUT, AllRecipeTypes.FILLING::getType),
                       HarvestRule.ofType("emptying",
                                          "create:emptying",
                                          Machines.ITEM_DRAIN,
                                          AllRecipeTypes.EMPTYING::getType),
                       HarvestRule.ofType("mechanical_crafting",
                                          "create:mechanical_crafting",
                                          Machines.MECHANICAL_CRAFTER,
                                          AllRecipeTypes.MECHANICAL_CRAFTING::getType),
                       HarvestRule.ofType("sequenced_assembly",
                                          "create:sequenced_assembly",
                                          Machines.SEQUENCED_ASSEMBLY,
                                          AllRecipeTypes.SEQUENCED_ASSEMBLY::getType),
                       HarvestRule.ofType("fan_smelting",
                                          "minecraft:smelting",
                                          Machines.ENCASED_FAN,
                                          () -> RecipeType.SMELTING),
                       HarvestRule.ofType("fan_blasting",
                                          "minecraft:blasting",
                                          Machines.ENCASED_FAN,
                                          () -> RecipeType.BLASTING),
                       HarvestRule.ofType("fan_smoking",
                                          "minecraft:smoking",
                                          Machines.ENCASED_FAN,
                                          () -> RecipeType.SMOKING),
                       HarvestRule.ofGatedType("stonecutting_saw",
                                               "minecraft:stonecutting",
                                               Machines.MECHANICAL_SAW,
                                               "allowStonecuttingOnSaw",
                                               () -> AllConfigs.server().recipes.allowStonecuttingOnSaw.get(),
                                               () -> RecipeType.STONECUTTING),
                       HarvestRule.ofFilteredType("shaped_crafter",
                                                  CRAFTING,
                                                  Machines.MECHANICAL_CRAFTER,
                                                  "allowRegularCraftingInCrafter",
                                                  () -> AllConfigs.server().recipes.allowRegularCraftingInCrafter.get(),
                                                  () -> RecipeType.CRAFTING,
                                                  recipe -> recipe instanceof ShapedRecipe),
                       HarvestRule.ofFilteredType("single_ingredient_crafter",
                                                  CRAFTING,
                                                  Machines.MECHANICAL_CRAFTER,
                                                  "allowRegularCraftingInCrafter",
                                                  () -> AllConfigs.server().recipes.allowRegularCraftingInCrafter.get(),
                                                  () -> RecipeType.CRAFTING,
                                                  RecipeBindings::isSingleIngredientShapeless),
                       HarvestRule.ofFilteredType("shapeless_mixer",
                                                  CRAFTING,
                                                  Machines.MECHANICAL_MIXER,
                                                  "allowShapelessInMixer",
                                                  () -> AllConfigs.server().recipes.allowShapelessInMixer.get(),
                                                  () -> RecipeType.CRAFTING,
                                                  RecipeBindings::isShapelessMixable),
                       HarvestRule.ofFilteredType("compressible_press",
                                                  CRAFTING,
                                                  Machines.MECHANICAL_PRESS,
                                                  "allowShapedSquareInPress",
                                                  () -> AllConfigs.server().recipes.allowShapedSquareInPress.get(),
                                                  () -> RecipeType.CRAFTING,
                                                  RecipeBindings::isCompressible),
                       HarvestRule.ofSynthetic("log_stripping_deployer",
                                               Machines.DEPLOYER,
                                               UNCONDITIONAL,
                                               () -> true,
                                               context -> List.copyOf(LogStrippingFakeRecipes.createRecipes())),
                       HarvestRule.ofSynthetic("potion_mixing",
                                               Machines.MECHANICAL_MIXER,
                                               "allowBrewingInMixer",
                                               () -> AllConfigs.server().recipes.allowBrewingInMixer.get(),
                                               context -> List.copyOf(PotionMixingRecipes.createRecipes(context.level()))));
    }

    public static List<String> tableProblems() {
        List<HarvestRule> rules = rules();
        List<String> problems = new ArrayList<>();

        duplicatesOf(rules.stream().map(HarvestRule::label).toList()).forEach(label -> problems.add(
                "duplicated rule label: " + label));

        rules.stream()
             .map(HarvestRule::machine)
             .filter(machine -> !Machines.all().contains(machine))
             .map(MachineId::value)
             .distinct()
             .forEach(machine -> problems.add("machine outside the catalogue: " + machine));

        return List.copyOf(problems);
    }

    private static List<String> duplicatesOf(List<String> values) {
        Set<String> seen = new HashSet<>();
        return values.stream().filter(value -> !seen.add(value)).distinct().toList();
    }

    public static Set<String> boundRecipeTypes() {
        return rules().stream().flatMap(rule -> rule.recipeTypes().stream()).collect(Collectors.toUnmodifiableSet());
    }

    private static boolean isSingleIngredientShapeless(Recipe<?> recipe) {
        return recipe instanceof CraftingRecipe && !(recipe instanceof ShapedRecipe) && recipe.getIngredients()
                                                                                              .size() == 1;
    }

    private static boolean isShapelessMixable(Recipe<?> recipe) {
        return recipe instanceof CraftingRecipe && !(recipe instanceof ShapedRecipe) && recipe.getIngredients()
                                                                                              .size() > 1 && !MechanicalPressBlockEntity.canCompress(
                recipe);
    }

    private static boolean isCompressible(Recipe<?> recipe) {
        return recipe instanceof CraftingRecipe && !(recipe instanceof MechanicalCraftingRecipe) && MechanicalPressBlockEntity.canCompress(
                recipe);
    }
}