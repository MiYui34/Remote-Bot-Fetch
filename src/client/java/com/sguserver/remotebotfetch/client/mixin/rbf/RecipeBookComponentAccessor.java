package com.sguserver.remotebotfetch.client.mixin.rbf;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes {@link RecipeBookComponent#setVisible(boolean)} so the screen mixin can
 * force the vanilla recipe book closed when the fetch panel replaces it.
 */
@Mixin(RecipeBookComponent.class)
public interface RecipeBookComponentAccessor {
    @Invoker("setVisible")
    void rbf$setVisible(boolean visible);
}
