package com.sguserver.remotebotfetch.client.mixin.rbf;

//#if MC>=260101
import com.sguserver.remotebotfetch.client.gui.RbfFetchPanel;
import com.sguserver.remotebotfetch.client.gui.RbfPanelHost;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.PreeditEvent;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Screen does not override {@code GuiEventListener#preeditUpdated}, so IME composition
 * events (Chinese / Japanese / Korean input) never reach the fetch panel's search box.
 * Adding the method here routes them into the panel while it is open.
 * <p>
 * {@code PreeditEvent} does not exist before 26.1, so this mixin is only compiled there.
 */
@Mixin(Screen.class)
public abstract class ScreenPreeditMixin {
    @SuppressWarnings("override")
    public boolean preeditUpdated(PreeditEvent event) {
        if ((Object) this instanceof RbfPanelHost host) {
            RbfFetchPanel panel = host.rbf$getPanel();
            if (panel != null && panel.isOpen() && panel.preeditUpdated(event)) {
                return true;
            }
        }

        return false;
    }
}
//#endif
