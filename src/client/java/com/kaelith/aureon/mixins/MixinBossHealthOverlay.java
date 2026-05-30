package com.kaelith.aureon.mixins;

import com.kaelith.aureon.features.dungeons.BossBarHealth;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BossHealthOverlay.class)
public class MixinBossHealthOverlay {
    @Redirect(
        method = "extractRenderState",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/LerpingBossEvent;getName()Lnet/minecraft/network/chat/Component;")
    )
    private Component aureon$appendBossHealth(LerpingBossEvent event) {
        return BossBarHealth.appendHealth(event, event.getName());
    }
}
