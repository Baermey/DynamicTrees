package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.api.applier.Applier;
import com.ferreusveritas.dynamictrees.api.applier.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.api.applier.VoidApplier;
import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.ferreusveritas.dynamictrees.util.JsonMapWrapper;
import com.ferreusveritas.dynamictrees.util.holderset.DTBiomeHolderSet;
import com.ferreusveritas.dynamictrees.util.holderset.DelayedHolderSet;
import com.ferreusveritas.dynamictrees.util.holderset.NameRegexMatchHolderSet;
import com.ferreusveritas.dynamictrees.util.holderset.TagsRegexMatchHolderSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.holdersets.OrHolderSet;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public final class BiomeListDeserialiser implements JsonDeserialiser<DTBiomeHolderSet> {

    public static final Supplier<Registry<Biome>> DELAYED_BIOME_REGISTRY = () -> {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null)
            throw new IllegalStateException("Queried biome registry too early; server does not exist yet!");

        return currentServer.registryAccess().registryOrThrow(Registries.BIOME);
    };

    private static final Applier<DTBiomeHolderSet, String> TAG_APPLIER = (biomeList, tagRegex) -> {
        tagRegex = tagRegex.toLowerCase(Locale.ENGLISH);
        final boolean notOperator = usingNotOperator(tagRegex);
        if (notOperator)
            tagRegex = tagRegex.substring(1);
        if (tagRegex.charAt(0) == '#')
            tagRegex = tagRegex.substring(1);

        try {
            ResourceLocation tagLocation = new ResourceLocation(tagRegex);
            TagKey<Biome> tagKey = TagKey.create(Registries.BIOME, tagLocation);

            // TODO UPDATE: This is used as a regex in 1.19.2. Double check!!!
            (notOperator ? biomeList.getExcludeComponents() : biomeList.getIncludeComponents()).add(new DelayedHolderSet<>(() -> DELAYED_BIOME_REGISTRY.get().getOrCreateTag(tagKey)));
        } catch (ResourceLocationException e) {
            return PropertyApplierResult.failure(e.getMessage());
        }

        // TODO UPDATE
        // (notOperator ? biomeList.getExcludeComponents() : biomeList.getIncludeComponents()).add(new TagsRegexMatchHolderSet<>(DELAYED_BIOME_REGISTRY, tagRegex));
        return PropertyApplierResult.success();
    };

    private static final VoidApplier<DTBiomeHolderSet, String> NAME_APPLIER = (biomeList, nameRegex) -> {
        nameRegex = nameRegex.toLowerCase(Locale.ENGLISH);
        final boolean notOperator = usingNotOperator(nameRegex);
        if (notOperator)
            nameRegex = nameRegex.substring(1);

        String finalNameRegex = nameRegex;
        (notOperator ? biomeList.getExcludeComponents() : biomeList.getIncludeComponents()).add(new DelayedHolderSet<>(
                () -> new NameRegexMatchHolderSet<>(DELAYED_BIOME_REGISTRY.get().asLookup(), finalNameRegex)));
    };

    private static boolean usingNotOperator(String categoryString) {
        return categoryString.charAt(0) == '!';
    }

    private static final VoidApplier<DTBiomeHolderSet, JsonArray> NAMES_OR_APPLIER = (biomeList, json) -> {
        final List<String> nameRegexes = JsonResult.forInput(json)
                .mapEachIfArray(String.class, (Result.SimpleMapper<String, String>) String::toLowerCase)
                .orElse(Collections.emptyList(), LogManager.getLogger()::error, LogManager.getLogger()::warn);

        List<HolderSet<Biome>> orIncludes = new ArrayList<>();
        List<HolderSet<Biome>> orExcludes = new ArrayList<>();
        nameRegexes.forEach(nameRegex -> {
            nameRegex = nameRegex.toLowerCase(Locale.ENGLISH);
            final boolean notOperator = usingNotOperator(nameRegex);
            if (notOperator)
                nameRegex = nameRegex.substring(1);

        String finalNameRegex = nameRegex;
        (notOperator ? orExcludes : orIncludes).add(new DelayedHolderSet<>(
                () -> new NameRegexMatchHolderSet<>(DELAYED_BIOME_REGISTRY.get().asLookup(), finalNameRegex)));
        });

        if (!orIncludes.isEmpty())
            biomeList.getIncludeComponents().add(new OrHolderSet<>(orIncludes));
        if (!orExcludes.isEmpty())
            biomeList.getExcludeComponents().add(new OrHolderSet<>(orExcludes));
    };

    private static final VoidApplier<DTBiomeHolderSet, JsonArray> TAGS_OR_APPLIER = (biomeList, json) -> {
        final List<String> nameRegexes = JsonResult.forInput(json)
                .mapEachIfArray(String.class, (Result.SimpleMapper<String, String>) String::toLowerCase)
                .orElse(Collections.emptyList(), LogManager.getLogger()::error, LogManager.getLogger()::warn);

        List<HolderSet<Biome>> orIncludes = new ArrayList<>();
        List<HolderSet<Biome>> orExcludes = new ArrayList<>();
        nameRegexes.forEach(tagRegex -> {
            tagRegex = tagRegex.toLowerCase(Locale.ENGLISH);
            final boolean notOperator = usingNotOperator(tagRegex);
            if (notOperator)
                tagRegex = tagRegex.substring(1);
            if (tagRegex.charAt(0) == '#')
                tagRegex = tagRegex.substring(1);

            (notOperator ? orExcludes : orIncludes).add(new TagsRegexMatchHolderSet<>(DELAYED_BIOME_REGISTRY.get().asLookup(), tagRegex));
        });

        if (!orIncludes.isEmpty())
            biomeList.getIncludeComponents().add(new OrHolderSet<>(orIncludes));
        if (!orExcludes.isEmpty())
            biomeList.getExcludeComponents().add(new OrHolderSet<>(orExcludes));
    };

    private final VoidApplier<DTBiomeHolderSet, JsonObject> andOperator =
            (biomes, jsonObject) -> applyAllAppliers(jsonObject, biomes);

    private final VoidApplier<DTBiomeHolderSet, JsonArray> orOperator = (biomeList, json) -> {
        List<HolderSet<Biome>> appliedList = new LinkedList<>();

        JsonResult.forInput(json)
                .mapEachIfArray(JsonObject.class, object -> {
                    DTBiomeHolderSet subList = new DTBiomeHolderSet();
                    applyAllAppliers(object, subList);
                    appliedList.add(subList);
                    return object;
                })
                .orElse(null, LogManager.getLogger()::error, LogManager.getLogger()::warn);

        if (!appliedList.isEmpty())
            biomeList.getIncludeComponents().add(new OrHolderSet<>(appliedList));
    };

    private final VoidApplier<DTBiomeHolderSet, JsonObject> notOperator = (biomeList, jsonObject) -> {
        final DTBiomeHolderSet notBiomeList = new DTBiomeHolderSet();
        applyAllAppliers(jsonObject, notBiomeList);
        biomeList.getExcludeComponents().add(notBiomeList);
    };

    private final JsonPropertyAppliers<DTBiomeHolderSet> appliers = new JsonPropertyAppliers<>(DTBiomeHolderSet.class);

    public BiomeListDeserialiser() {
        registerAppliers();
    }

    private void registerAppliers() {
        this.appliers
                .register("tag", String.class, TAG_APPLIER)
                .registerArrayApplier("tags", String.class, TAG_APPLIER)
                .register("tags_or", JsonArray.class, TAGS_OR_APPLIER)
                .register("name", String.class, NAME_APPLIER)
                .registerArrayApplier("names", String.class, NAME_APPLIER)
                .register("names_or", JsonArray.class, NAMES_OR_APPLIER)
                .registerArrayApplier("AND", JsonObject.class, andOperator)
                .register("OR", JsonArray.class, orOperator)
                .register("NOT", JsonObject.class, notOperator);
    }

    private void applyAllAppliers(JsonObject json, DTBiomeHolderSet biomes) {
        appliers.applyAll(new JsonMapWrapper(json), biomes);
    }

    @Override
    public Result<DTBiomeHolderSet, JsonElement> deserialise(final JsonElement input) {
        return JsonResult.forInput(input)
                .mapIfType(String.class, biomeName -> {
                    DTBiomeHolderSet biomes = new DTBiomeHolderSet();
                    biomes.getIncludeComponents().add(new DelayedHolderSet<>(() -> new NameRegexMatchHolderSet<>(DELAYED_BIOME_REGISTRY.get().asLookup(), biomeName.toLowerCase(Locale.ENGLISH))));
                    return biomes;
                })
                .elseMapIfType(JsonObject.class, selectorObject -> {
                    final DTBiomeHolderSet biomes = new DTBiomeHolderSet();
                    // Apply from all appliers
                    applyAllAppliers(selectorObject, biomes);
                    return biomes;
                }).elseTypeError();
    }

}