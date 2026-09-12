package com.simibubi.create.platform.fabric;

import com.simibubi.create.Create;
import net.fabricmc.api.ModInitializer;

public class CreateFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		Create.init();
	}
}
