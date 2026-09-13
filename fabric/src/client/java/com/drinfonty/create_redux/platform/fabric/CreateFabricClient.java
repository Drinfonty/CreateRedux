package com.drinfonty.create_redux.platform.fabric;

import com.drinfonty.create_redux.client.render.CreateClientRenderers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

public class CreateFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FabricNetworking.clientSender = ClientPlayNetworking::send;
		CreateClientRenderers.registerAll(BlockEntityRendererRegistry::register);
	}
}
