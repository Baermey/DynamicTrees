package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public class GrowthLogicKits {
	
	public static final GrowthLogicKit NULL = new NullLogic();
	public static final GrowthLogicKit DARK_OAK = new DarkOakLogic(DynamicTrees.resLoc("dark_oak"));
	public static final GrowthLogicKit CONIFER = new ConiferLogic(DynamicTrees.resLoc("conifer"));
	public static final GrowthLogicKit MEGA_CONIFER = new ConiferLogic(DynamicTrees.resLoc("mega_conifer"), 5);
	public static final GrowthLogicKit JUNGLE = new JungleLogic(DynamicTrees.resLoc("jungle"));

	public static void register(final IForgeRegistry<GrowthLogicKit> registry) {
		registry.registerAll(NULL, DARK_OAK, CONIFER, MEGA_CONIFER, JUNGLE);
	}

}
