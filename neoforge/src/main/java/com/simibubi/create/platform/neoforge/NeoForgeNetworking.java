package com.simibubi.create.platform.neoforge;

import com.simibubi.create.platform.network.PlatformNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgeNetworking implements PlatformNetworking {
	@Override
	public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
		PacketDistributor.sendToPlayer(player, payload);
	}

	@Override
	public void sendToServer(CustomPacketPayload payload) {
		if (FMLEnvironment.getDist().isClient()) {
			ClientPacketDistributor.sendToServer(payload);
		}
	}
}
