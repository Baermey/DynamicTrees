package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.GroundFinder;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ForgeBiomeTagsProvider;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public final class OverworldGroundFinder implements GroundFinder {

    @Override
    public List<BlockPos> findGround(LevelAccessor level, BlockPos start, @Nullable Heightmap.Types heightmap) {
		//We start of by getting the surface ground
		LinkedList<BlockPos> surfaceGround = new LinkedList<>(SURFACE.findGround(level, start, heightmap));
		BlockPos surfaceBlock = surfaceGround.get(0);
		//Then we do a very sparse check to find underground biomes
		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(start.getX(), 0, start.getZ());
		boolean caveBiomeFound = false;
		while (CoordUtils.inRange(pos, level.getMinBuildHeight(), surfaceBlock.getY())) {
			if (level.getBiome(pos).is(Tags.Biomes.IS_UNDERGROUND)){
				caveBiomeFound = true;
				break;
			}
			pos.move(0,-10,0);
		}
		//If underground biomes are present, we want to include them
		if (caveBiomeFound){
			List<BlockPos> subterraneanGround = SUBTERRANEAN.findGround(level, start, heightmap);
			surfaceGround.addAll(subterraneanGround);
			return new LinkedList<>(surfaceGround);
		}
		return surfaceGround;
    }

}
