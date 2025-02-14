package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.registry.Registries;
import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.api.registry.SimpleRegistry;
import com.ferreusveritas.dynamictrees.api.registry.TypeRegistryEvent;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.block.leaves.CherryLeavesProperties;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.block.leaves.PalmLeavesProperties;
import com.ferreusveritas.dynamictrees.block.leaves.ScruffyLeavesProperties;
import com.ferreusveritas.dynamictrees.block.leaves.SolidLeavesProperties;
import com.ferreusveritas.dynamictrees.block.leaves.WartProperties;
import com.ferreusveritas.dynamictrees.block.rooty.AerialRootsSoilProperties;
import com.ferreusveritas.dynamictrees.block.rooty.SoilProperties;
import com.ferreusveritas.dynamictrees.block.rooty.SpreadableSoilProperties;
import com.ferreusveritas.dynamictrees.block.rooty.WaterSoilProperties;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.resources.Resources;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.family.MangroveFamily;
import com.ferreusveritas.dynamictrees.tree.family.NetherFungusFamily;
import com.ferreusveritas.dynamictrees.tree.family.PalmFamily;
import com.ferreusveritas.dynamictrees.tree.species.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DTTrees {

    public static final ResourceLocation NULL = DynamicTrees.location("null");

    public static final ResourceLocation OAK = DynamicTrees.location("oak");
    public static final ResourceLocation BIRCH = DynamicTrees.location("birch");
    public static final ResourceLocation SPRUCE = DynamicTrees.location("spruce");
    public static final ResourceLocation JUNGLE = DynamicTrees.location("jungle");
    public static final ResourceLocation DARK_OAK = DynamicTrees.location("dark_oak");
    public static final ResourceLocation ACACIA = DynamicTrees.location("acacia");
    public static final ResourceLocation AZALEA = DynamicTrees.location("azalea");
    public static final ResourceLocation CRIMSON = DynamicTrees.location("crimson");
    public static final ResourceLocation WARPED = DynamicTrees.location("warped");

    @SubscribeEvent
    public static void registerSpecies(final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<Species> event) {
        // Registers fake species for generating mushrooms.
        event.getRegistry().registerAll(new FakeMushroomSpecies(true), new FakeMushroomSpecies(false));
    }

    @SubscribeEvent
    public static void registerSoilProperties(final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<SoilProperties> event) {
        event.getRegistry().registerAll(
                //SoilHelper.registerSoil(DynamicTrees.resLoc("dirt"),Blocks.DIRT, SoilHelper.DIRT_LIKE, ),//new SpreadableSoilProperties.SpreadableRootyBlock(Blocks.DIRT, 9, Blocks.GRASS_BLOCK, Blocks.MYCELIUM)
                //SoilHelper.registerSoil(DynamicTrees.resLoc("netherrack"),Blocks.NETHERRACK, SoilHelper.NETHER_LIKE, new SpreadableSoilProperties.SpreadableRootyBlock(Blocks.NETHERRACK, Items.BONE_MEAL, Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM))
        );
    }

    @SubscribeEvent
    public static void registerLeavesPropertiesTypes(final TypeRegistryEvent<LeavesProperties> event) {
        event.registerType(DynamicTrees.location("solid"), SolidLeavesProperties.TYPE);
        event.registerType(DynamicTrees.location("wart"), WartProperties.TYPE);
        event.registerType(DynamicTrees.location("palm"), PalmLeavesProperties.TYPE);
        event.registerType(DynamicTrees.location("scruffy"), ScruffyLeavesProperties.TYPE);
        //event.registerType(DynamicTrees.location("cherry"), CherryLeavesProperties.TYPE);
    }

    @SubscribeEvent
    public static void registerFamilyTypes(final TypeRegistryEvent<Family> event) {
        event.registerType(DynamicTrees.location("nether_fungus"), NetherFungusFamily.TYPE);
        event.registerType(DynamicTrees.location("mangrove"), MangroveFamily.TYPE);
        event.registerType(DynamicTrees.location("palm"), PalmFamily.TYPE);
    }

    @SubscribeEvent
    public static void registerSpeciesTypes(final TypeRegistryEvent<Species> event) {
        event.registerType(DynamicTrees.location("nether_fungus"), NetherFungusSpecies.TYPE);
        event.registerType(DynamicTrees.location("swamp_oak"), SwampOakSpecies.TYPE);
        event.registerType(DynamicTrees.location("palm"), PalmSpecies.TYPE);
        event.registerType(DynamicTrees.location("mangrove"), MangroveSpecies.TYPE);
    }

    @SubscribeEvent
    public static void registerSoilPropertiesTypes(final TypeRegistryEvent<SoilProperties> event) {
        event.registerType(DynamicTrees.location("water"), WaterSoilProperties.TYPE);
        event.registerType(DynamicTrees.location("spreadable"), SpreadableSoilProperties.TYPE);
        event.registerType(DynamicTrees.location("aerial_roots"), AerialRootsSoilProperties.TYPE);
    }

    @SubscribeEvent
    public static void newRegistry(NewRegistryEvent event) {
        final List<SimpleRegistry<?>> registries = Registries.REGISTRIES.stream()
                .filter(registry -> registry instanceof SimpleRegistry)
                .map(registry -> (SimpleRegistry<?>) registry)
                .collect(Collectors.toList());

        // Post registry events.
        registries.forEach(SimpleRegistry::postRegistryEvent);

        Resources.setupTreesResourceManager();

        // Register Forge registry entry getters and add-on Json object getters.
        JsonDeserialisers.registerForgeEntryGetters();
        JsonDeserialisers.postRegistryEvent();

        // Register feature cancellers.
        FeatureCanceller.REGISTRY.postRegistryEvent();
        FeatureCanceller.REGISTRY.lock();
    }

    @SubscribeEvent
    public static void loadResources(RegisterEvent event) {
        if (event.getRegistryKey() != ForgeRegistries.BLOCKS.getRegistryKey()) {
            return;
        }
        // Register any registry entries from Json files.
        Resources.MANAGER.load();
        // Lock all the registries.
        Registries.REGISTRIES.stream()
                .filter(registry -> registry instanceof SimpleRegistry)
                .forEach(Registry::lock);
    }
}
