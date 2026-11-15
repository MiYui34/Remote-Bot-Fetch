package com.sguserver.remotebotfetch.client.mixin.rbf;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessors for the protected geometry fields of the container screens. Mixin does
 * not resolve {@code @Shadow} fields which are only declared by a super class, so the
 * values are read and written through this interface instead.
 */
@Mixin(AbstractContainerScreen.class)
public interface ContainerScreenAccessor {
    @Accessor("leftPos")
    int rbf$getLeftPos();

    @Accessor("leftPos")
    void rbf$setLeftPos(int leftPos);

    @Accessor("topPos")
    int rbf$getTopPos();

    @Accessor("imageWidth")
    int rbf$getImageWidth();

    @Accessor("imageHeight")
    int rbf$getImageHeight();
}
