package com.sguserver.remotebotfetch.client.mixin.lms;

import com.google.common.collect.ImmutableList;
import com.sguserver.remotebotfetch.client.ClientMod;
import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.feature.lms.ItemStorageDataManager;
import com.sguserver.remotebotfetch.client.util.ChatHelper;
import com.sguserver.remotebotfetch.client.util.MCUtils;
import com.sguserver.remotebotfetch.client.util.PlayerUtils;
import com.sguserver.remotebotfetch.client.util.ThreadUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen {
    @Shadow
    @Final
    protected T menu;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Shadow
    protected abstract void init();

    @Inject(
            method = "onClose",
            at = @At("TAIL")
    )
    private void onClose(CallbackInfo ci) {
        this.rbf$cacheData();
        if (Configs.LMS.AUTO_KILL_FAKE_PLAYERS.getBooleanValue()) {
            Set<String> fakePlayerNames = ItemStorageDataManager.WAIT_FOR_KILLING();
            for (String name : fakePlayerNames) {
                var titleString = this.title.getString();
                if (!titleString.contains(name))
                    continue;

                ThreadUtils.runOnTaskThread(() -> {
                    try {
                        Thread.sleep(Configs.LMS.AUTO_COOLDOWN.getIntegerValue());
                        MCUtils.executeTask(() -> MCUtils.executeCommand("player %s kill".formatted(name)));
                    }
                    catch (Exception e) {
                        ChatHelper.clientChat(ChatHelper.c(e.getMessage()));
                        ClientMod.LOGGER.error(e.getMessage(), e);
                    }
                    finally {
                        fakePlayerNames.remove(name);
                    }
                });
            }
        }
    }

    @Unique
    private void rbf$cacheData() {
        if (!Configs.LMS.FAKE_PLAYER_INVENTORY_ITEM_CACHE.getBooleanValue() || (Object) this instanceof InventoryScreen || !PlayerUtils.isSurvivalMode())
            return;

        String title = this.title.getString();
        List<String> names = Configs.Lists.FAKE_PLAYER_INVENTORY_ITEM_CACHE_WHITE_LIST.getStrings();
        for (String name : names) {
            if (title.startsWith(name)) {
                List<Slot> slots = this.menu.slots.stream().filter(slot -> !(slot.container instanceof Inventory))
                                                  .toList();
                ItemStorageDataManager.addPlayerInventory(name, new ItemStorageDataManager.PlayerInventory(ImmutableList.copyOf(slots)));
                ChatHelper.actionBar(ChatHelper.c("已缓存 %s 的物品栏".formatted(name)));
                break;
            }
        }
    }
}
