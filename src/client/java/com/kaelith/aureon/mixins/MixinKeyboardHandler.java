package com.kaelith.aureon.mixins;

import com.kaelith.aureon.api.zenith.Zenith;
import com.kaelith.aureon.events.EventBus;
import com.kaelith.aureon.events.core.GuiEvent;
import com.kaelith.aureon.events.core.KeyEvent;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void Aureon$onKey(long handle, int action, net.minecraft.client.input.KeyEvent event, CallbackInfo ci) {
        if (handle == Zenith.getWindowHandle()) {
            if (action == 1) {
                if (EventBus.INSTANCE.post(new KeyEvent.Press(event.key(), event.scancode(), event.modifiers()))) ci.cancel();
            } else if (action == 0) {
                if (EventBus.INSTANCE.post(new KeyEvent.Release(event.key(), event.scancode(), event.modifiers()))) ci.cancel();
            }
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void Aureon$onChar(long handle, net.minecraft.client.input.CharacterEvent event, CallbackInfo ci) {
        Screen screen = Zenith.getClient().screen;
        if (screen == null) return;
        char charTyped = (char) event.codepoint();
        boolean cancelled = EventBus.INSTANCE.post(new GuiEvent.Key(null, GLFW.GLFW_KEY_UNKNOWN, charTyped, 0, screen));
        if (cancelled) ci.cancel();
    }
}