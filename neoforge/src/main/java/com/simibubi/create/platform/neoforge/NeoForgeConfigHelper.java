package com.simibubi.create.platform.neoforge;

import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CKinetics;
import com.simibubi.create.platform.config.ConfigHelper;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class NeoForgeConfigHelper implements ConfigHelper {
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	public static final ModConfigSpec SPEC;

	public static final ModConfigSpec.DoubleValue MAX_ROTATION_SPEED;
	public static final ModConfigSpec.DoubleValue STRESS_CAPACITY_MULTIPLIER;
	public static final ModConfigSpec.DoubleValue STRESS_IMPACT_MULTIPLIER;
	public static final ModConfigSpec.DoubleValue WATER_WHEEL_BASE_SPEED;
	public static final ModConfigSpec.DoubleValue CRANK_ROTATION_PER_CLICK;

	static {
		BUILDER.push("kinetics");
		MAX_ROTATION_SPEED = BUILDER.comment("Maximum rotation speed in RPM").defineInRange("maxRotationSpeed", 256.0, 0.0, 65536.0);
		STRESS_CAPACITY_MULTIPLIER = BUILDER.comment("Stress capacity multiplier").defineInRange("stressCapacityMultiplier", 1.0, 0.0, 1000.0);
		STRESS_IMPACT_MULTIPLIER = BUILDER.comment("Stress impact multiplier").defineInRange("stressImpactMultiplier", 1.0, 0.0, 1000.0);
		WATER_WHEEL_BASE_SPEED = BUILDER.comment("Water wheel base speed").defineInRange("waterWheelBaseSpeed", 8.0, 0.0, 256.0);
		CRANK_ROTATION_PER_CLICK = BUILDER.comment("Hand crank rotation per click").defineInRange("crankRotationPerClick", 32.0, 0.0, 256.0);
		BUILDER.pop();
		SPEC = BUILDER.build();
	}

	private final CKinetics kinetics = new CKinetics();

	@Override
	public void registerConfigs() {
		ModContainer container = ModLoadingContext.get().getActiveContainer();
		container.registerConfig(ModConfig.Type.SERVER, SPEC);
		container.getEventBus().addListener((ModConfigEvent.Loading event) -> onConfigEvent(event.getConfig()));
		container.getEventBus().addListener((ModConfigEvent.Reloading event) -> onConfigEvent(event.getConfig()));
	}

	private void onConfigEvent(ModConfig config) {
		if (config.getSpec() == SPEC) {
			kinetics.maxRotationSpeed = MAX_ROTATION_SPEED.get().floatValue();
			kinetics.stressCapacityMultiplier = STRESS_CAPACITY_MULTIPLIER.get().floatValue();
			kinetics.stressImpactMultiplier = STRESS_IMPACT_MULTIPLIER.get().floatValue();
			kinetics.waterWheelBaseSpeed = WATER_WHEEL_BASE_SPEED.get().floatValue();
			kinetics.crankRotationPerClick = CRANK_ROTATION_PER_CLICK.get().floatValue();
			AllConfigs.setServerKinetics(kinetics);
		}
	}

	@Override
	public CKinetics getKineticsConfig() {
		return kinetics;
	}
}
