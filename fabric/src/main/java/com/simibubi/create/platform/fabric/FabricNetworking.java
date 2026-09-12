package com.simibubi.create.platform.fabric;

import com.simibubi.create.platform.network.PlatformNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public class FabricNetworking implements PlatformNetworking {
	public static Consumer<CustomPacketPayload> clientSender = payload -> {};

	@Override
	public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
		ServerPlayNetworking.send(player, payload);
	}

	@Override
	public void sendToServer(CustomPacketPayload payload) {
		clientSender.accept(payload);
	}
}
