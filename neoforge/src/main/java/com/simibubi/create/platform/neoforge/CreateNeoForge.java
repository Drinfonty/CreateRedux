package com.simibubi.create.platform.neoforge;

import com.simibubi.create.Create;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Create.ID)
public class CreateNeoForge {
	public CreateNeoForge(IEventBus modEventBus) {
		NeoForgeRegistryHelper.init(modEventBus);
		Create.init();
	}
}
