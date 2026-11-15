package com.sguserver.remotebotfetch.client.mixin.lms;

// Litematica is resolved for every version (1.21.1 uses the 1.21 build), so this integration is
// compiled whenever a litematica artifact is available.
//#if MC>=12101
import com.llamalad7.mixinextras.sugar.Local;
import com.sguserver.remotebotfetch.client.ClientMod;
import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.feature.lms.ItemStorageDataManager;
import com.sguserver.remotebotfetch.client.util.PlayerUtils;
import fi.dy.masa.litematica.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldUtils.class)
public class WorldUtilsMixin {
    // Litematica calls doSchematicWorldPickBlock() twice for a single middle click (closest / exact
    // variants), which would spawn two fetch bots. Only the first call within the same client tick is used.
    private static long rbf$lastSubmitTick = Long.MIN_VALUE;

    @Inject(
            method = "doSchematicWorldPickBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/litematica/schematic/pickblock/SchematicPickBlockEventHandler;onSchematicPickBlockPrePick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private static void doSchematicWorldPickBlock_Inject(boolean closest, Minecraft mc,
                                                         CallbackInfoReturnable<Boolean> cir, @Local ItemStack stack,
                                                         @Local BlockPos pos) {
        if (mc.level == null) {
            return;
        }
        long tick = mc.level.getGameTime();

        if (rbf$lastSubmitTick == tick) {
            return;
        }
        if (PlayerUtils.checkRemainCount(stack.getItem()) == 0 && PlayerUtils.isSurvivalMode(mc.player) && mc.level
                .getBlockState(pos).getBlock() == Blocks.AIR) {
            if (Configs.LMS.MID_CLICK_TAKE_ITEM.getBooleanValue()) {
                rbf$lastSubmitTick = tick;
                int count = mc.player.isShiftKeyDown() ? stack.getMaxStackSize() * 27 : stack.getMaxStackSize();
                ClientMod.debugMessage(() -> "Submitted %s x%d to ItemStorageDataManager".formatted(stack.getItem(), count));
                ItemStorageDataManager.submit(stack.getItem(), count);
            }
        }
    }
}
//#endif
