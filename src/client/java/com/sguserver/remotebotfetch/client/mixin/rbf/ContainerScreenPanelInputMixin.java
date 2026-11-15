package com.sguserver.remotebotfetch.client.mixin.rbf;

import com.sguserver.remotebotfetch.client.gui.RbfFetchPanel;
import com.sguserver.remotebotfetch.client.gui.RbfPanelHost;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
//#if MC>=12110
import net.minecraft.client.input.MouseButtonEvent;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mouse wheel, mouse release and (on the versions that do not carry those callbacks in
 * the recipe book screen) mouse drag handling for the fetch panel. Those methods are
 * only declared by {@link AbstractContainerScreen}, not by the recipe book screen,
 * so they are hooked here and guarded to the player inventory.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenPanelInputMixin {
//#if MC>=12103
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void rbf$mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof InventoryScreen) || !((Object) this instanceof RbfPanelHost host)) {
            return;
        }

        RbfFetchPanel panel = host.rbf$getPanel();
        if (panel.isOpen() && panel.isMouseOver(mouseX, mouseY) && panel.mouseScrolled(scrollY)) {
            cir.setReturnValue(true);
        }
    }
//#endif

//#if MC>=12110
    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void rbf$mouseReleased(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof RbfPanelHost host)) {
            return;
        }

        if (host.rbf$getPanel().mouseReleased()) {
            cir.setReturnValue(true);
        }
    }
//#else
//$$ @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
//$$ private void rbf$mouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
//$$     if (!((Object) this instanceof RbfPanelHost host)) {
//$$         return;
//$$     }
//$$
//$$     if (host.rbf$getPanel().mouseReleased()) {
//$$         cir.setReturnValue(true);
//$$     }
//$$ }
//$$
//$$ @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
//$$ private void rbf$mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
//$$     if (!((Object) this instanceof InventoryScreen) || !((Object) this instanceof RbfPanelHost host)) {
//$$         return;
//$$     }
//$$
//$$     RbfFetchPanel panel = host.rbf$getPanel();
//$$     if (panel.isOpen() && panel.mouseDragged(mouseX, mouseY)) {
//$$         cir.setReturnValue(true);
//$$     }
//$$ }
//#endif
}
