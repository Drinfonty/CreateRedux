package com.simibubi.create.platform.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simibubi.create.Create;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CKinetics;
import com.simibubi.create.platform.config.ConfigHelper;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class FabricConfigHelper implements ConfigHelper {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private CKinetics kinetics = new CKinetics();

	@Override
	public void registerConfigs() {
		loadOrCreateConfig();
		AllConfigs.setServerKinetics(kinetics);
	}

	@Override
	public CKinetics getKineticsConfig() {
		return kinetics;
	}

	private void loadOrCreateConfig() {
		try {
			Path configDir = FabricLoader.getInstance().getConfigDir();
			Path configFile = configDir.resolve("create.json");
			if (Files.exists(configFile)) {
				try (Reader reader = Files.newBufferedReader(configFile)) {
					CKinetics loaded = GSON.fromJson(reader, CKinetics.class);
					if (loaded != null) {
						this.kinetics = loaded;
					}
				}
			} else {
				Files.createDirectories(configDir);
				try (Writer writer = Files.newBufferedWriter(configFile)) {
					GSON.toJson(this.kinetics, writer);
				}
			}
		} catch (Exception e) {
			Create.LOGGER.error("Failed to load or save Create configuration on Fabric", e);
		}
	}
}
