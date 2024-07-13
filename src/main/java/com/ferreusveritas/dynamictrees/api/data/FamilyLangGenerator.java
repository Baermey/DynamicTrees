package com.ferreusveritas.dynamictrees.api.data;

import com.ferreusveritas.dynamictrees.data.provider.DTLangProvider;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.family.MangroveFamily;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class FamilyLangGenerator implements Generator<DTLangProvider, Family>{
    DTLangProvider provider;

    @Override
    public void generate(DTLangProvider provider, Family input, Dependencies dependencies) {
        this.provider = provider;
        //familyLang(input, input.getLangOverride("family"));
        input.getBranch().ifPresent(branch -> treeLang(branch, input, input.getLangOverride("branch")));
//        if(input.hasSurfaceRoot()){
//            blockLang(input.getSurfaceRoot().get(), input.getLangOverride("surface_root"));
//        }
        if(input instanceof MangroveFamily mgf){
            mgf.getRoots().ifPresent(root -> treeLang(root, input, input.getLangOverride("roots")));
            //mgf.getDefaultSoil().getBlock().ifPresent(soil -> blockLang(soil, input.getLangOverride("soil")));
        }
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        return new Dependencies();
    }

    protected void familyLang(Family entry, Optional<String> override) {
        provider.add(I18n.get("family." + entry.getRegistryName().toString().replace(":", ".")), override.orElse(checkReplace(entry.getRegistryName().getPath())));
    }

    protected void treeLang(Block entry, Family family, Optional<String> blah) {
        provider.addBlock(() -> entry, blah.orElse(checkReplace(family.getRegistryName().getPath()+"_tree")));
    }

    protected void blockLang(Block entry, Optional<String> blah) {
        provider.addBlock(() -> entry, blah.orElse(checkReplace(ForgeRegistries.BLOCKS.getKey(entry).getPath())));
    }

    protected String checkReplace(String path) {
        return Arrays.stream(path.split("_"))
                .map(StringUtils::capitalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
    }
}