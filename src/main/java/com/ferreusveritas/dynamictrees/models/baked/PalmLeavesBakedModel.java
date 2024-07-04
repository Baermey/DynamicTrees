package com.ferreusveritas.dynamictrees.models.baked;

import com.ferreusveritas.dynamictrees.block.leaves.PalmLeavesProperties;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class PalmLeavesBakedModel implements IDynamicBakedModel {


    protected RenderTypeGroup renderGroup = new RenderTypeGroup(RenderType.cutout(), RenderType.cutout());
    protected final BlockModel blockModel;

    ResourceLocation frondsResLoc;
    TextureAtlasSprite frondsTexture;

    protected final BakedModel[] bakedFronds = new BakedModel[8]; // 8 = Number of surrounding blocks

    public PalmLeavesBakedModel(ResourceLocation modelResLoc, ResourceLocation frondsResLoc){
        this.blockModel = new BlockModel(null, new ArrayList<>(), new HashMap<>(), false, BlockModel.GuiLight.FRONT, ItemTransforms.NO_TRANSFORMS, new ArrayList<>());
        this.frondsResLoc = frondsResLoc;
    }

    //This method defines the model and shapes of the fronds. Each implementation must define its own.
    public abstract void setupModels ();

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData extraData, @Nullable RenderType renderType) {
        if (state == null || side != null)
            return Collections.emptyList();

        LinkedList<BakedQuad> quads = new LinkedList<>();

        int direction = state.getValue(PalmLeavesProperties.DynamicPalmLeavesBlock.DIRECTION);

        if (direction != 0)
            quads.addAll(bakedFronds[direction-1].getQuads(state, null, rand, extraData, renderType));


        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return frondsTexture;
    }

    @Nonnull
    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    public static class BlockVertexData {

        public float x;
        public float y;
        public float z;
        public int color;
        public float u;
        public float v;

        public BlockVertexData(float x, float y, float z, float u, float v) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
            color = 0xFFFFFFFF;
        }

        public BlockVertexData(BakedQuad quad, int vIndex) {
            this(quad.getVertices(), vIndex);
        }

        public BlockVertexData(int[] data, int vIndex) {
            vIndex *= 8;
            x = Float.intBitsToFloat(data[vIndex++]);
            y = Float.intBitsToFloat(data[vIndex++]);
            z = Float.intBitsToFloat(data[vIndex++]);
            color = data[vIndex++];
            u = Float.intBitsToFloat(data[vIndex++]);
            v = Float.intBitsToFloat(data[vIndex]);
        }

        public int[] toInts() {
            return new int[] { Float.floatToRawIntBits(x), Float.floatToRawIntBits(y), Float.floatToRawIntBits(z),
                    color, Float.floatToRawIntBits(u), Float.floatToRawIntBits(v), 0, 0 };
        }

        protected int[] toInts(TextureAtlasSprite texture) {
            return new int[] {
                    Float.floatToRawIntBits(x), Float.floatToRawIntBits(y), Float.floatToRawIntBits(z),
                    color,
                    Float.floatToRawIntBits(texture.getU(u)), Float.floatToRawIntBits(texture.getV(v)),
                    0, 0
            };
        }

    }

}
