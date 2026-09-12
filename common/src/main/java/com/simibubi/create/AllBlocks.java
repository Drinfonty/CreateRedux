package com.simibubi.create;

import com.simibubi.create.content.kinetics.CogWheelBlock;
import com.simibubi.create.content.kinetics.GearboxBlock;
import com.simibubi.create.content.kinetics.HandCrankBlock;
import com.simibubi.create.content.kinetics.KineticStressRegistry;
import com.simibubi.create.content.kinetics.ShaftBlock;
import com.simibubi.create.content.kinetics.WaterWheelBlock;
import com.simibubi.create.platform.registry.CreateRegistrate;
import com.simibubi.create.platform.registry.RegistryEntry;
import net.minecraft.world.level.block.SoundType;

public class AllBlocks {
	public static final RegistryEntry<ShaftBlock> SHAFT = CreateRegistrate.registerBlock(
			"shaft",
			props -> new ShaftBlock(props.strength(1.5f).sound(SoundType.WOOD).noOcclusion())
	);

	public static final RegistryEntry<CogWheelBlock> COGWHEEL = CreateRegistrate.registerBlock(
			"cogwheel",
			props -> CogWheelBlock.small(props.strength(1.5f).sound(SoundType.WOOD).noOcclusion())
	);

	public static final RegistryEntry<CogWheelBlock> LARGE_COGWHEEL = CreateRegistrate.registerBlock(
			"large_cogwheel",
			props -> CogWheelBlock.large(props.strength(1.5f).sound(SoundType.WOOD).noOcclusion())
	);

	public static final RegistryEntry<HandCrankBlock> HAND_CRANK = CreateRegistrate.registerBlock(
			"hand_crank",
			props -> new HandCrankBlock(props.strength(1.5f).sound(SoundType.WOOD).noOcclusion())
	);

	public static final RegistryEntry<WaterWheelBlock> WATER_WHEEL = CreateRegistrate.registerBlock(
			"water_wheel",
			props -> new WaterWheelBlock(props.strength(1.5f).sound(SoundType.WOOD).noOcclusion())
	);

	public static final RegistryEntry<GearboxBlock> GEARBOX = CreateRegistrate.registerBlock(
			"gearbox",
			props -> new GearboxBlock(props.strength(1.5f).sound(SoundType.WOOD).noOcclusion())
	);

	public static void register() {
		KineticStressRegistry.registerCapacity(HAND_CRANK.get(), 256.0f);
		KineticStressRegistry.registerCapacity(WATER_WHEEL.get(), 256.0f);
		KineticStressRegistry.registerImpact(SHAFT.get(), 0.0f);
		KineticStressRegistry.registerImpact(COGWHEEL.get(), 0.0f);
		KineticStressRegistry.registerImpact(LARGE_COGWHEEL.get(), 0.0f);
		KineticStressRegistry.registerImpact(GEARBOX.get(), 0.0f);
	}
}
