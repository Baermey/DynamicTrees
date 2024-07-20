package com.ferreusveritas.dynamictrees.data.provider;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.block.rooty.AerialRootsSoilProperties;
import com.ferreusveritas.dynamictrees.block.rooty.SoilProperties;
import com.ferreusveritas.dynamictrees.data.DTBlockTags;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.family.MangroveFamily;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Harley O'Connor
 */
public class DTBlockTagsProvider extends BlockTagsProvider {
    public DTBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        if (this.modId.equals(DynamicTrees.MOD_ID)) {
            this.addDTOnlyTags();
        }
        this.addDTTags();
    }

    private void addDTOnlyTags() {
        this.tag(DTBlockTags.BRANCHES)
                .addTag(DTBlockTags.BRANCHES_THAT_BURN)
                .addTag(DTBlockTags.FUNGUS_BRANCHES);

        this.tag(DTBlockTags.FOLIAGE)
                .add(Blocks.GRASS)
                .add(Blocks.TALL_GRASS)
                .add(Blocks.FERN)
                .add(Blocks.LILY_PAD)
                .add(Blocks.PINK_PETALS)
                .add(Blocks.BROWN_MUSHROOM)
                .add(Blocks.RED_MUSHROOM)
                .add(Blocks.LILY_PAD)
                .addTag(BlockTags.FLOWERS)
                .addTag(BlockTags.REPLACEABLE_BY_TREES);

        this.tag(DTBlockTags.STRIPPED_BRANCHES)
                .addTag(DTBlockTags.STRIPPED_BRANCHES_THAT_BURN)
                .addTag(DTBlockTags.STRIPPED_FUNGUS_BRANCHES);

        this.tag(BlockTags.ENDERMAN_HOLDABLE)
                .addTag(DTBlockTags.FUNGUS_CAPS);

        this.tag(BlockTags.FLOWER_POTS)
                .add(DTRegistries.POTTED_SAPLING.get());

        Species.REGISTRY.get(DTTrees.WARPED).getSapling().ifPresent(sapling ->
                this.tag(BlockTags.HOGLIN_REPELLENTS).add(sapling));

        this.tag(BlockTags.LEAVES)
                .addTag(DTBlockTags.LEAVES);

        this.tag(BlockTags.LOGS)
                .addTag(DTBlockTags.BRANCHES);

        this.tag(BlockTags.LOGS_THAT_BURN)
                .addTag(DTBlockTags.BRANCHES_THAT_BURN)
                .addTag(DTBlockTags.STRIPPED_BRANCHES_THAT_BURN);

        this.tag(BlockTags.SAPLINGS)
                .addTag(DTBlockTags.SAPLINGS);

        this.tag(BlockTags.WART_BLOCKS)
                .addTag(DTBlockTags.WART_BLOCKS);

        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .addTag(DTBlockTags.ROOTS)
                .addTag(DTBlockTags.AERIAL_ROOTS_ROOTY_SOIL);
    }

    protected void addDTTags() {
        LeavesProperties.REGISTRY.dataGenerationStream(this.modId).forEach(leavesProperties ->
                leavesProperties.addGeneratedBlockTags(this::tag));

        Family.REGISTRY.dataGenerationStream(this.modId).forEach(family ->
                family.addGeneratedBlockTags(this::tag));

        Species.REGISTRY.dataGenerationStream(this.modId).forEach(species ->
                species.addGeneratedBlockTags(this::tag));

        SoilProperties.REGISTRY.dataGenerationStream(this.modId).forEach(soilProperties ->
                soilProperties.addGeneratedBlockTags(this::tag));
    }

    @Override
    public String getName() {
        return modId + " DT Block Tags";
    }
}
