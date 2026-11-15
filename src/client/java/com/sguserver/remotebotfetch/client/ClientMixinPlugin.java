package com.sguserver.remotebotfetch.client;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Applies the litematica integration mixins only when litematica is actually installed,
 * litematica stays an optional dependency of the client mod.
 */
public class ClientMixinPlugin implements IMixinConfigPlugin {
    private static final String LITEMATICA_MOD_ID = "litematica";
    private static final List<String> LITEMATICA_MIXINS =
        List.of(
            "com.sguserver.remotebotfetch.client.mixin.lms.MaterialListEntryMixin",
            "com.sguserver.remotebotfetch.client.mixin.lms.WorldUtilsMixin");

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (LITEMATICA_MIXINS.contains(mixinClassName)) {
            return FabricLoader.getInstance().isModLoaded(LITEMATICA_MOD_ID);
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(
        String targetClassName,
        ClassNode targetClass,
        String mixinClassName,
        IMixinInfo mixinInfo
    ) {
    }

    @Override
    public void postApply(
        String targetClassName,
        ClassNode targetClass,
        String mixinClassName,
        IMixinInfo mixinInfo
    ) {
    }
}
