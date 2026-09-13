package com.drinfonty.create_redux.platform.neoforge;

import com.drinfonty.create_redux.Create;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Create.ID)
public class CreateNeoForge {
	public CreateNeoForge(IEventBus modEventBus) {
		NeoForgeRegistryHelper.init(modEventBus);
		Create.init();
		CreateNeoForgeClient.init(modEventBus);
	}
}
