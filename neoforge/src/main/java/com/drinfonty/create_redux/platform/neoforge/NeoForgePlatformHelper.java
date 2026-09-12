package com.drinfonty.create_redux.platform.neoforge;

import com.drinfonty.create_redux.platform.PlatformHelper;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;

public class NeoForgePlatformHelper implements PlatformHelper {
	@Override
	public boolean isFabric() {
		return false;
	}

	@Override
	public boolean isNeoForge() {
		return true;
	}

	@Override
	public boolean isPhysicalClient() {
		return FMLEnvironment.getDist().isClient();
	}

	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public String getPlatformName() {
		return "NeoForge";
	}
}
