package com.kaelith.aureon.mixins.accessors;

import net.minecraft.client.gui.components.LerpingBossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LerpingBossEvent.class)
public interface AccessorLerpingBossEvent {
    @Accessor("targetPercent")
    float getAureonTargetPercent();
}
