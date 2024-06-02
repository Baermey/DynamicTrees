package com.ferreusveritas.dynamictrees.resources.loader;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.applier.Applier;
import com.ferreusveritas.dynamictrees.api.applier.ApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.api.applier.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.api.resource.loading.preparation.JsonRegistryResourceLoader;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.block.rooty.SoilHelper;
import com.ferreusveritas.dynamictrees.block.rooty.SoilProperties;
import com.ferreusveritas.dynamictrees.deserialisation.JsonHelper;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.family.MangroveFamily;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Harley O'Connor
 */
public final class FamilyResourceLoader extends JsonRegistryResourceLoader<Family> {

    private static final Logger LOGGER = LogManager.getLogger();

    public FamilyResourceLoader() {
        super(Family.REGISTRY, "families", ApplierRegistryEvent.FAMILY);
    }

    @Override
    public void registerAppliers() {
        this.commonAppliers
                .register("common_species", ResourceLocation.class, (family, registryName) -> {
                    registryName = TreeRegistry.processResLoc(registryName);
                    Species.REGISTRY.runOnNextLock(Species.REGISTRY.generateIfValidRunnable(registryName,
                            family::setupCommonSpecies, setCommonWarn(family, registryName)));
                })
                .register("common_leaves", LeavesProperties.class, Family::setCommonLeaves)
                .register("max_branch_radius", Integer.class, Family::setMaxBranchRadius);

        // Primitive logs are needed before gathering data.
        this.gatherDataAppliers
                .register("primitive_log", Block.class, Family::setPrimitiveLog)
                .register("primitive_stripped_log", Block.class, Family::setPrimitiveStrippedLog)
                .registerMapApplier("texture_overrides", ResourceLocation.class, Family::setTextureOverrides)
                .registerMapApplier("model_overrides", ResourceLocation.class, Family::setModelOverrides);

        this.setupAppliers
                .register("primitive_log", Block.class, Family::setPrimitiveLog)
                .register("primitive_stripped_log", Block.class, Family::setPrimitiveStrippedLog)
                .register("stick", Item.class, Family::setStick);

        this.loadAppliers
                .register("generate_surface_root", Boolean.class, Family::setHasSurfaceRoot)
                .register("generate_stripped_branch", Boolean.class, Family::setHasStrippedBranch)
                .register("fire_proof", Boolean.class, Family::setIsFireProof);

        this.reloadAppliers
                .register("primary_thickness", Integer.class, Family::setPrimaryThickness)
                .register("secondary_thickness", Integer.class, Family::setSecondaryThickness)
                .register("branch_is_ladder", Boolean.class, Family::setBranchIsLadder)
                .register("max_signal_depth", Integer.class, Family::setMaxSignalDepth)
                .register("loot_volume_multiplier", Float.class, Family::setLootVolumeMultiplier)
                .register("min_radius_for_stripping", Integer.class, Family::setMinRadiusForStripping)
                .register("reduce_radius_when_stripping", Boolean.class, Family::setReduceRadiusWhenStripping);

        registerMangroveAppliers();

        super.registerAppliers();
    }

    private void registerMangroveAppliers(){
        this.gatherDataAppliers
                .register("primitive_root", MangroveFamily.class, Block.class, MangroveFamily::setPrimitiveRoots)
                .register("primitive_filled_root", MangroveFamily.class, Block.class, MangroveFamily::setPrimitiveRootsFilled)
                .register("primitive_covered_root", MangroveFamily.class, Block.class, MangroveFamily::setPrimitiveRootsCovered)
                //to-do: put in soil properties instead
                .register("default_soil", MangroveFamily.class, SoilProperties.class, MangroveFamily::setDefaultSoil);
        this.setupAppliers
                .register("primitive_root", MangroveFamily.class, Block.class, MangroveFamily::setPrimitiveRoots)
                .register("primitive_filled_root", MangroveFamily.class, Block.class, MangroveFamily::setPrimitiveRootsFilled)
                .register("primitive_covered_root", MangroveFamily.class, Block.class, MangroveFamily::setPrimitiveRootsCovered)
                //.register("replaceable_by_roots", MangroveFamily.class , ,)
        ;
        this.reloadAppliers
                .register("default_soil", MangroveFamily.class, SoilProperties.class, MangroveFamily::setDefaultSoil)
                .registerArrayApplier("root_system_acceptable_soils", MangroveFamily.class, String.class, (Applier<MangroveFamily, String>) this::addAcceptableSoilForRootSystem);
        ;

    }

    /**
     * Generates a runnable for if there was not a registered {@link Species} under the specified {@code registryName}
     * to set as common for the specified {@code family}.
     *
     * @param family       the family
     * @param registryName the registry name of the requested family
     * @return a {@link Runnable} that logs the warning
     */
    private static Runnable setCommonWarn(final Family family, final ResourceLocation registryName) {
        return () -> LOGGER.warn("Could not set common species for \"" + family + "\" as species with name  \"" +
                registryName + "\" was not found.");
    }

    @Override
    protected void applyLoadAppliers(LoadData loadData, JsonObject json) {
        this.setBranchProperties(loadData.getResource(), json);
        super.applyLoadAppliers(loadData, json);
    }

    private void setBranchProperties(Family family, JsonObject json) {
        family.setProperties(JsonHelper.getBlockProperties(
                JsonHelper.getOrDefault(json, "branch_properties", JsonObject.class, new JsonObject()),
                family.getDefaultBranchMapColor(),
                family::getDefaultBranchProperties,
                error -> this.logError(family.getRegistryName(), error),
                warning -> this.logWarning(family.getRegistryName(), warning)
        ));
    }

    @Override
    protected void postLoadOnLoad(LoadData loadData, JsonObject json) {
        super.postLoadOnLoad(loadData, json);
        loadData.getResource().setupBlocks();
    }

    private PropertyApplierResult addAcceptableSoilForRootSystem(MangroveFamily family, String acceptableSoil) {
        return SoilHelper.applyIfSoilIsAcceptable(family, acceptableSoil, MangroveFamily::addAcceptableSoilsForRootSystem);
    }

}
