package com.sguserver.remotebotfetch.client.mixin.rbf;

import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.gui.RbfFetchPanel;
import com.sguserver.remotebotfetch.client.gui.RbfPanelHost;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
//#if MC>=12102
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#endif

/**
 * The vanilla recipe book button becomes the on/off switch of the RBF fetch panel
 * while the feature is enabled. This hook is placed here instead of on the
 * {@link net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen}
 * because {@link InventoryScreen} overrides the click callback without calling
 * super, and it is invoked after the screen has already been re-positioned.
 * <p>
 * 1.21.1 has no {@code onRecipeBookButtonClick} method, the panel switch there rides
 * on {@code RecipeBookComponent#toggleVisibility} instead, so this mixin is empty.
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
//#if MC>=12102
    @Inject(method = "onRecipeBookButtonClick", at = @At("HEAD"))
    private void rbf$onRecipeBookButtonClick(CallbackInfo ci) {
        if (!Configs.LMS.INVENTORY_FETCH_MENU.getBooleanValue()) {
            return;
        }

        RbfPanelHost host = (RbfPanelHost) (Object) this;
        RbfFetchPanel panel = host.rbf$getPanel();
        panel.toggle();
        host.rbf$updatePanelPosition(panel.isOpen());
    }
//#endif
}
