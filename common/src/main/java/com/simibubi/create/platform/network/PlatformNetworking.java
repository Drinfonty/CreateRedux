package com.simibubi.create.platform.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.ServiceLoader;

public interface PlatformNetworking {
	PlatformNetworking INSTANCE = ServiceLoader.load(PlatformNetworking.class)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("No PlatformNetworking implementation found on classpath."));

	void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

	void sendToServer(CustomPacketPayload payload);

	static PlatformNetworking get() {
		return INSTANCE;
	}
}
