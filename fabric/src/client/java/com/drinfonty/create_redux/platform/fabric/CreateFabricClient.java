package com.drinfonty.create_redux.platform.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class CreateFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FabricNetworking.clientSender = ClientPlayNetworking::send;
	}
}
