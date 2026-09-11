package fr.syuko.createfactoryplanner.core.machine;

import fr.syuko.createfactoryplanner.core.math.Rate;
import fr.syuko.createfactoryplanner.core.recipe.RecipeDto;

import java.util.List;

public interface ThroughputModel {

    Rate operationsPerTick(RecipeDto recipe, MachineSettings settings, MachineProfile profile);

    List<ParamDescriptor> parameters(MachineProfile profile);

    double stressUnits(MachineSettings settings, MachineProfile profile);
}
