package com.drinfonty.create_redux.platform.neoforge;

import com.drinfonty.create_redux.client.render.CreateClientRenderers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class CreateNeoForgeClient {
	public static void init(IEventBus modEventBus) {
		modEventBus.addListener(CreateNeoForgeClient::onRegisterRenderers);
	}

	public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
		CreateClientRenderers.registerAll(event::registerBlockEntityRenderer);
	}
}
