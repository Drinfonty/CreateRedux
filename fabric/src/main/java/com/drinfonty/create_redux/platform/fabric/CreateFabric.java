package com.drinfonty.create_redux.platform.fabric;

import com.drinfonty.create_redux.Create;
import net.fabricmc.api.ModInitializer;

public class CreateFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		Create.init();
	}
}
