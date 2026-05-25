package com.kaelith.aureon.mixins;

import com.kaelith.aureon.features.msc.InventoryButtons;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Collection;

/*
 * Modified inventory effect rendering behavior.
 * Under GPL 3.0 License
 */
@Mixin(EffectsInInventory.class)
public class MixinEffectsInInventory {
    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)

    private void Aureon$onDrawEffect(GuiGraphicsExtractor graphics, Collection<MobEffectInstance> activeEffects, int x0, int yStep, int mouseX, int mouseY, int maxWidth, CallbackInfo ci) {
        if (InventoryButtons.INSTANCE.isEnabled()) ci.cancel();
    }
}
