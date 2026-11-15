package com.sguserver.remotebotfetch.client.mixin.lms;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.feature.lms.ItemStorageDataManager;
import com.sguserver.remotebotfetch.client.util.MCUtils;
import com.sguserver.remotebotfetch.client.util.PlayerUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
//#if MC>=260101
import net.minecraft.world.inventory.ContainerInput;
//#else
//$$ import net.minecraft.world.inventory.ClickType;
//#endif
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    public CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu handler, Inventory inventory,
                                            Component title) {
        super(handler, inventory, title);
    }

//#if MC>=260101
    @Inject(
            method = "slotClicked",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/inventory/ContainerInput;THROW:Lnet/minecraft/world/inventory/ContainerInput;",
                    ordinal = 0,
                    opcode = Opcodes.GETSTATIC,
                    shift = At.Shift.AFTER
            )
    )
    protected void onMouseClick_1(Slot slot, int slotId, int button, ContainerInput actionType, CallbackInfo ci,
                                  @Share("type") LocalRef<Object> rbf$type) {
        if (Configs.lockCreativeScreen && actionType != ContainerInput.THROW && actionType != ContainerInput.QUICK_CRAFT) {
            rbf$type.set(actionType);
        }
    }
//#else
    //$$ @Inject(
    //$$         method = "slotClicked",
    //$$         at = @At(
    //$$                 value = "FIELD",
    //$$                 target = "Lnet/minecraft/world/inventory/ClickType;THROW:Lnet/minecraft/world/inventory/ClickType;",
    //$$                 ordinal = 0,
    //$$                 opcode = Opcodes.GETSTATIC,
    //$$                 shift = At.Shift.AFTER
    //$$         )
    //$$ )
    //$$ protected void onMouseClick_1(Slot slot, int slotId, int button, ClickType actionType, CallbackInfo ci,
    //$$                               @Share("type") LocalRef<Object> rbf$type) {
    //$$     if (Configs.lockCreativeScreen && actionType != ClickType.THROW && actionType != ClickType.QUICK_CRAFT) {
    //$$         rbf$type.set(actionType);
    //$$     }
    //$$ }
//#endif

    @Inject(
            method = "containerTick",
            at = @At(
                    value = "INVOKE",
//#if MC>=12105
                    target = "Lnet/minecraft/client/player/LocalPlayer;hasInfiniteMaterials()Z"
//#else
                    //$$ target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasInfiniteItems()Z"
//#endif
            ),
            cancellable = true
    )
    public void handledScreenTick(CallbackInfo ci) {
        if (Configs.lockCreativeScreen) {
            ci.cancel();
        }
    }

    /*
        slotID -999 (从创造模式物品栏丢出物品)
        button 0 左键
        button 1 右键
        SlotActionType PickUp 单击
    */
//#if MC>=12103
//#if MC>=260101
    @Inject(
            method = "slotClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;canDropItems()Z",
                    ordinal = 1
            ),
            cancellable = true
    )
    protected void onMouseClick_2(Slot slot, int slotId, int button, ContainerInput actionType, CallbackInfo ci,
                                  @Share("type") LocalRef<Object> rbf$type) {
        this.rbf$handleDropClick(slotId, button, actionType == ContainerInput.PICKUP, ci);
    }
//#else
    //$$ @Inject(
    //$$         method = "slotClicked",
    //$$         at = @At(
    //$$                 value = "INVOKE",
    //$$                 target = "Lnet/minecraft/client/player/LocalPlayer;canDropItems()Z",
    //$$                 ordinal = 1
    //$$         ),
    //$$         cancellable = true
    //$$ )
    //$$ protected void onMouseClick_2(Slot slot, int slotId, int button, ClickType actionType, CallbackInfo ci,
    //$$                               @Share("type") LocalRef<Object> rbf$type) {
    //$$     this.rbf$handleDropClick(slotId, button, actionType == ClickType.PICKUP, ci);
    //$$ }
//#endif
//#else
    // 1.21.1 has no LocalPlayer#canDropItems() call inside slotClicked yet, the carried stack is
    // dropped directly, so the click is filtered up front instead.
    //$$ @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    //$$ protected void onMouseClick_2(Slot slot, int slotId, int button, ClickType actionType, CallbackInfo ci,
    //$$                               @Share("type") LocalRef<Object> rbf$type) {
    //$$     this.rbf$handleDropClick(slotId, button, actionType == ClickType.PICKUP, ci);
    //$$ }
//#endif

    @Unique
    private void rbf$handleDropClick(int slotId, int button, boolean pickup, CallbackInfo ci) {
        if (Configs.lockCreativeScreen && pickup && slotId == -999) {
            if (!Configs.LMS.LMS_FETCH_SUPPORT.getBooleanValue())
                return;
            ItemStack stack = this.menu.getCarried();
            int count = -1;
            int maxCount = stack.getMaxStackSize();
            if (button == 0)
                count = maxCount;
            else if (button == 1)
                count = maxCount * 27;
            ItemStorageDataManager.submit(stack.getItem(), stack.getCount() * count);
            menu.setCarried(ItemStack.EMPTY);
            Configs.lockCreativeScreen = false;
            MCUtils.setScreen(null);
            ci.cancel();
        }
    }

    @Inject(
            method = "getTooltipFromContainerItem",
            at = @At("RETURN"),
            cancellable = true
    )
    public void getTooltipFromItem(ItemStack stack, CallbackInfoReturnable<List<Component>> cir) {
        if (Configs.LMS.LMS_FETCH_SUPPORT.getBooleanValue() && Configs.lockCreativeScreen) {
            if (!PlayerUtils.isSurvivalMode(minecraft.player))
                return;
            var texts = cir.getReturnValue();
            var text = ItemStorageDataManager.get(stack);
            texts.addAll(text);
            cir.setReturnValue(texts);
        }
    }

    @ModifyExpressionValue(
            method = "init",
            at = @At(
                    value = "INVOKE",
//#if MC>=12105
                    target = "Lnet/minecraft/client/player/LocalPlayer;hasInfiniteMaterials()Z"
//#else
                    //$$ target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasInfiniteItems()Z"
//#endif
            )
    )
    public boolean isInCreativeMode(boolean original) {
        return Configs.lockCreativeScreen || original;
    }

    @ModifyExpressionValue(
            method = "hasPermissions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;canUseGameMasterBlocks()Z"
            )
    )
    public boolean isCreativeLevelTwoOp(boolean original) {
        if (Configs.LMS.LMS_FETCH_SUPPORT.getBooleanValue() && Configs.lockCreativeScreen) {
            return true;
        }
        return original;
    }
}
