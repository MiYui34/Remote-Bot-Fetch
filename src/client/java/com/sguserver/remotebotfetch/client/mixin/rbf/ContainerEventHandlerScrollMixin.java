package com.sguserver.remotebotfetch.client.mixin.rbf;

//#if MC<12103
import com.sguserver.remotebotfetch.client.gui.RbfFetchPanel;
import com.sguserver.remotebotfetch.client.gui.RbfPanelHost;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mouse wheel handling for the fetch panel on 1.21.1, where {@code AbstractContainerScreen} does
 * not declare {@code mouseScrolled} and the scrolling runs through the {@link ContainerEventHandler}
 * default implementation instead. 1.21.3 and newer hook
 * {@link ContainerScreenPanelInputMixin} directly.
 */
@Mixin(ContainerEventHandler.class)
public interface ContainerEventHandlerScrollMixin {
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
}
//#endif
