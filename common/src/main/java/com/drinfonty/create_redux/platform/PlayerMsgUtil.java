package com.drinfonty.create_redux.platform;

import java.lang.reflect.Method;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Cross-version utility for sending overlay/actionbar messages to players.
 * Handles the Minecraft API evolution between 1.21.x (displayClientMessage(Component, boolean))
 * and 26.x+ (sendOverlayMessage(Component)).
 */
public final class PlayerMsgUtil {
	private static final Method SEND_OVERLAY_METHOD;
	private static final Method DISPLAY_CLIENT_METHOD;

	static {
		Method sendOverlay = null;
		Method displayClient = null;
		for (Method m : Player.class.getMethods()) {
			if (m.getName().equals("sendOverlayMessage") && m.getParameterCount() == 1) {
				sendOverlay = m;
				break;
			}
		}
		for (Method m : Player.class.getMethods()) {
			if (m.getName().equals("displayClientMessage") && m.getParameterCount() == 2) {
				displayClient = m;
				break;
			}
		}
		SEND_OVERLAY_METHOD = sendOverlay;
		DISPLAY_CLIENT_METHOD = displayClient;
	}

	private PlayerMsgUtil() {}

	public static void sendOverlay(Player player, Component message) {
		if (SEND_OVERLAY_METHOD != null) {
			try {
				SEND_OVERLAY_METHOD.invoke(player, message);
				return;
			} catch (Exception ignored) {}
		}
		if (DISPLAY_CLIENT_METHOD != null) {
			try {
				DISPLAY_CLIENT_METHOD.invoke(player, message, true);
				return;
			} catch (Exception ignored) {}
		}
	}
}
