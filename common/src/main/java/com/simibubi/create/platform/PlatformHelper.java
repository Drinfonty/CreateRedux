package com.simibubi.create.platform;

import java.util.ServiceLoader;

public interface PlatformHelper {
	PlatformHelper INSTANCE = ServiceLoader.load(PlatformHelper.class)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("No PlatformHelper service implementation found on classpath."));

	boolean isFabric();

	boolean isNeoForge();

	boolean isPhysicalClient();

	boolean isModLoaded(String modId);

	String getPlatformName();

	static PlatformHelper get() {
		return INSTANCE;
	}
}
