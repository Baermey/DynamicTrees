package com.ferreusveritas.dynamictrees.blocks.leaves;

import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.api.datapacks.IJsonApplierManager;
import com.ferreusveritas.dynamictrees.resources.ILoadListener;
import com.ferreusveritas.dynamictrees.resources.MultiJsonReloadListener;
import com.ferreusveritas.dynamictrees.util.json.JsonPropertyApplierList;
import com.google.gson.JsonElement;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class LeavesPropertiesManager extends MultiJsonReloadListener implements ILoadListener, IJsonApplierManager {

    private static final Logger LOGGER = LogManager.getLogger();

    /** A {@link JsonPropertyApplierList} for applying properties to {@link LeavesProperties} objects. */
    private final JsonPropertyApplierList<LeavesProperties> appliers = new JsonPropertyApplierList<>(LeavesProperties.class);

    public LeavesPropertiesManager() {
        super("leaves_properties");
        this.registerAppliers();
    }

    @Override
    public void registerAppliers() {
        this.appliers.register("primitive_leaves", ResourceLocation.class, LeavesProperties::setPrimitiveLeavesRegName)
                .register("cell_kit", CellKit.class, LeavesProperties::setCellKit)
                .register("smother", Integer.class, LeavesProperties::setSmotherLeavesMax)
                .register("light_requirement", Integer.class, LeavesProperties::setLightRequirement)
                .register("fire_spread", Integer.class, LeavesProperties::setFireSpreadSpeed)
                .register("flammability", Integer.class, LeavesProperties::setFlammability)
                .register("connect_any_radius", Boolean.class, LeavesProperties::setConnectAnyRadius);
    }

    @Override
    protected void apply(Map<ResourceLocation, List<Pair<String, JsonElement>>> map, IResourceManager resourceManager, IProfiler profiler) {
        for (final Map.Entry<ResourceLocation, List<Pair<String, JsonElement>>> entry : map.entrySet()) {
            final ResourceLocation registryName = entry.getKey();
            final LeavesProperties leavesProperties = new LeavesProperties(registryName);

            for (Pair<String, JsonElement> elementPair : entry.getValue()) {
                final String fileName = elementPair.getKey();
                final JsonElement jsonElement = elementPair.getValue();

                if (!jsonElement.isJsonObject()) {
                    LOGGER.warn("Skipping loading leaves properties for {} from {} as its root element is not a Json object.", registryName, fileName);
                    return;
                }

                this.appliers.applyAll(jsonElement.getAsJsonObject(), leavesProperties).forEach(failureResult -> LOGGER.warn("Error whilst loading tree family data for {} from {}: {}", registryName, fileName, failureResult.getErrorMessage()));
            }

            LeavesProperties.REGISTRY.register(leavesProperties);
            LOGGER.debug("Loaded and registered leaves properties: {}.", leavesProperties.getDisplayString());
        }
    }

    @Override
    public void load(IResourceManager resourceManager) {
        this.apply(this.prepare(resourceManager, EmptyProfiler.INSTANCE), resourceManager, EmptyProfiler.INSTANCE);
    }

}
