package com.sguserver.remotebotfetch.client.mixin.rbf;

import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.gui.RbfPanelHost;
import net.minecraft.client.Minecraft;
//#if MC>=260101
import net.minecraft.client.gui.GuiGraphicsExtractor;
//#else
//$$ import net.minecraft.client.gui.GuiGraphics;
//#endif
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps the vanilla recipe book closed while the fetch panel mode is enabled, so
 * the recipe book button can be used as the panel switch without the recipe book
 * animation fighting the panel for the same screen space. The recipe book is
 * suppressed on every render pass as well, because the book re-opens itself as
 * soon as a new recipe is unlocked.
 */
@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin {
    @Inject(method = "toggleVisibility", at = @At("HEAD"), cancellable = true)
    private void rbf$toggleVisibility(CallbackInfo ci) {
        if (this.rbf$isSuppressed()) {
            ci.cancel();
//#if MC<12102
            //$$ // 1.21.1 declares no recipe book button callback on the inventory screen,
            //$$ // so the panel switch rides on the vanilla visibility toggle instead.
            //$$ net.minecraft.client.gui.screens.Screen current = com.sguserver.remotebotfetch.client.util.MCUtils.getScreen();
            //$$ if (current instanceof RbfPanelHost host) {
            //$$     com.sguserver.remotebotfetch.client.gui.RbfFetchPanel panel = host.rbf$getPanel();
            //$$     panel.toggle();
            //$$     host.rbf$updatePanelPosition(panel.isOpen());
            //$$ }
//#endif
        }
    }

//#if MC>=260101
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void rbf$hideBook(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial,
                              CallbackInfo ci) {
        if (this.rbf$isSuppressed()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractGhostRecipe", at = @At("HEAD"), cancellable = true)
    private void rbf$hideGhostRecipe(GuiGraphicsExtractor graphics, boolean resultSlot, CallbackInfo ci) {
        if (this.rbf$isSuppressed()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractTooltip", at = @At("HEAD"), cancellable = true)
    private void rbf$hideTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, Slot slot,
                                 CallbackInfo ci) {
        if (this.rbf$isSuppressed()) {
            ci.cancel();
        }
    }
//#else
    //$$ @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    //$$ private void rbf$hideBook(GuiGraphics graphics, int mouseX, int mouseY, float partial, CallbackInfo ci) {
    //$$     if (this.rbf$isSuppressed()) {
    //$$         ci.cancel();
    //$$     }
    //$$ }
//#if MC>=12103
    //$$ @Inject(method = "renderGhostRecipe", at = @At("HEAD"), cancellable = true)
    //$$ private void rbf$hideGhostRecipe(GuiGraphics graphics, boolean resultSlot, CallbackInfo ci) {
    //$$     if (this.rbf$isSuppressed()) {
    //$$         ci.cancel();
    //$$     }
    //$$ }
//$$
    //$$ @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    //$$ private void rbf$hideTooltip(GuiGraphics graphics, int mouseX, int mouseY, Slot slot, CallbackInfo ci) {
    //$$     if (this.rbf$isSuppressed()) {
    //$$         ci.cancel();
    //$$     }
    //$$ }
//#else
    //$$ @Inject(method = "renderGhostRecipe", at = @At("HEAD"), cancellable = true)
    //$$ private void rbf$hideGhostRecipe(GuiGraphics graphics, int mouseX, int mouseY, boolean resultSlot,
    //$$                                 float partial, CallbackInfo ci) {
    //$$     if (this.rbf$isSuppressed()) {
    //$$         ci.cancel();
    //$$     }
    //$$ }
//$$
    //$$ @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    //$$ private void rbf$hideTooltip(GuiGraphics graphics, int mouseX, int mouseY, int left, int top, CallbackInfo ci) {
    //$$     if (this.rbf$isSuppressed()) {
    //$$         ci.cancel();
    //$$     }
    //$$ }
//#endif
//#endif

    @Unique
    private boolean rbf$isSuppressed() {
        if (!Configs.LMS.INVENTORY_FETCH_MENU.getBooleanValue()) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        // Minecraft.screen moved to Minecraft.gui in 26.2; MCUtils.getScreen() already handles both.
        net.minecraft.client.gui.screens.Screen current = com.sguserver.remotebotfetch.client.util.MCUtils.getScreen();
        return current instanceof InventoryScreen screen && screen instanceof RbfPanelHost;
    }
}
