package com.kaelith.client;

import com.kaelith.aureon.AureonCore;
import com.kaelith.aureon.utils.ConfigKt;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class AureonClient implements ClientModInitializer {
	private static KeyMapping openClickGuiKey;

	@Override
	public void onInitializeClient() {
		AureonCore.INSTANCE.onInitializeClient();

		KeyMapping.Category aureonCategory = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("aureon", "aureon"));
		openClickGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.aureon.open_clickgui",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				aureonCategory
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openClickGuiKey.consumeClick()) {
				ConfigKt.getConfig().open();
			}
		});
	}
}
