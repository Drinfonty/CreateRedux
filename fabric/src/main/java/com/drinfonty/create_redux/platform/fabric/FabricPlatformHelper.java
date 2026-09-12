package com.drinfonty.create_redux.platform.fabric;

import com.drinfonty.create_redux.platform.PlatformHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class FabricPlatformHelper implements PlatformHelper {
	@Override
	public boolean isFabric() {
		return true;
	}

	@Override
	public boolean isNeoForge() {
		return false;
	}

	@Override
	public boolean isPhysicalClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}

	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public String getPlatformName() {
		return "Fabric";
	}
}
