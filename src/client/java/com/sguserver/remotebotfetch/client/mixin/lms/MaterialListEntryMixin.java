package com.sguserver.remotebotfetch.client.mixin.lms;

// Litematica is resolved for every version (1.21.1 uses the 1.21 build), so this integration is
// compiled whenever a litematica artifact is available.
//#if MC>=12101
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.feature.lms.ItemStorageDataManager;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MaterialListEntry.class)
public class MaterialListEntryMixin {
    @Shadow
    @Final
//#if MC>=260101
    private fi.dy.masa.malilib.util.data.ItemType item;
//#else
    //$$ private fi.dy.masa.malilib.util.ItemType item;
//#endif

    @ModifyReturnValue(
            method = "getCountAvailable",
            at = @At("RETURN")
    )
    private int getCountAvailable(int original) {
        int value = original;
        ItemStack stack = this.item.getStack();

        if (Configs.LMS.LITEMATICA_CALCULATE_QWP.getBooleanValue())
            value = value + ItemStorageDataManager.getCount(stack.getItem(), false);
        if (Configs.LMS.LITEMATICA_CALCULATE_FAKE.getBooleanValue())
            value = value + ItemStorageDataManager.getCount(stack.getItem(), true);

        return value;
    }
}
//#endif
