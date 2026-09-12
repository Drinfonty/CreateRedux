package com.drinfonty.create_redux.platform.config;

import com.drinfonty.create_redux.infrastructure.config.CKinetics;

import java.util.ServiceLoader;

public interface ConfigHelper {
	void registerConfigs();
	CKinetics getKineticsConfig();

	static ConfigHelper get() {
		return ServiceLoader.load(ConfigHelper.class)
				.findFirst()
				.orElseGet(DefaultConfigHelper::new);
	}

	class DefaultConfigHelper implements ConfigHelper {
		private final CKinetics kinetics = new CKinetics();

		@Override
		public void registerConfigs() {
		}

		@Override
		public CKinetics getKineticsConfig() {
			return kinetics;
		}
	}
}
