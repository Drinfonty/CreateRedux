package com.drinfonty.create_redux;

import com.drinfonty.create_redux.content.contraptions.components.structure.bearing.BearingBlock;
import com.drinfonty.create_redux.content.contraptions.components.structure.bearing.WindmillBearingBlock;
import com.drinfonty.create_redux.content.contraptions.components.structure.piston.MechanicalPistonBlock;
import com.drinfonty.create_redux.content.contraptions.components.structure.piston.PistonExtensionPoleBlock;
import com.drinfonty.create_redux.content.kinetics.CogWheelBlock;
import com.drinfonty.create_redux.content.kinetics.GearboxBlock;
import com.drinfonty.create_redux.content.kinetics.HandCrankBlock;
import com.drinfonty.create_redux.content.kinetics.KineticStressRegistry;
import com.drinfonty.create_redux.content.kinetics.ShaftBlock;
import com.drinfonty.create_redux.content.kinetics.WaterWheelBlock;
import com.drinfonty.create_redux.platform.registry.CreateRegistrate;
import com.drinfonty.create_redux.platform.registry.RegistryEntry;
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

	public static final RegistryEntry<BearingBlock> MECHANICAL_BEARING = CreateRegistrate.registerBlock(
			"mechanical_bearing",
			props -> new BearingBlock(props.strength(1.5f).sound(SoundType.WOOD).noOcclusion())
	);

	public static final RegistryEntry<WindmillBearingBlock> WINDMILL_BEARING = CreateRegistrate.registerBlock(
			"windmill_bearing",
			props -> new WindmillBearingBlock(props.strength(1.5f).sound(SoundType.WOOD).noOcclusion())
	);

	public static final RegistryEntry<MechanicalPistonBlock> MECHANICAL_PISTON = CreateRegistrate.registerBlock(
			"mechanical_piston",
			props -> new MechanicalPistonBlock(props.strength(1.5f).sound(SoundType.WOOD).noOcclusion())
	);

	public static final RegistryEntry<PistonExtensionPoleBlock> PISTON_EXTENSION_POLE = CreateRegistrate.registerBlock(
			"piston_extension_pole",
			props -> new PistonExtensionPoleBlock(props.strength(1.5f).sound(SoundType.WOOD).noOcclusion())
	);

	public static final RegistryEntry<com.drinfonty.create_redux.content.logistics.depot.DepotBlock> DEPOT = CreateRegistrate.registerBlock(
			"depot",
			props -> new com.drinfonty.create_redux.content.logistics.depot.DepotBlock(props.strength(3.0f).sound(SoundType.METAL).noOcclusion())
	);

	public static final RegistryEntry<com.drinfonty.create_redux.content.kinetics.belt.BeltBlock> BELT = CreateRegistrate.registerBlock(
			"belt",
			props -> new com.drinfonty.create_redux.content.kinetics.belt.BeltBlock(props.strength(0.8f).sound(SoundType.WOOL).noOcclusion())
	);

	public static final RegistryEntry<com.drinfonty.create_redux.content.logistics.chute.ChuteBlock> CHUTE = CreateRegistrate.registerBlock(
			"chute",
			props -> new com.drinfonty.create_redux.content.logistics.chute.ChuteBlock(props.strength(2.0f).sound(SoundType.METAL).noOcclusion())
	);

	public static final RegistryEntry<com.drinfonty.create_redux.content.logistics.funnel.AndesiteFunnelBlock> ANDESITE_FUNNEL = CreateRegistrate.registerBlock(
			"andesite_funnel",
			props -> new com.drinfonty.create_redux.content.logistics.funnel.AndesiteFunnelBlock(props.strength(1.5f).sound(SoundType.STONE).noOcclusion())
	);

	public static final RegistryEntry<com.drinfonty.create_redux.content.logistics.funnel.BrassFunnelBlock> BRASS_FUNNEL = CreateRegistrate.registerBlock(
			"brass_funnel",
			props -> new com.drinfonty.create_redux.content.logistics.funnel.BrassFunnelBlock(props.strength(1.5f).sound(SoundType.METAL).noOcclusion())
	);

	public static void register() {
		KineticStressRegistry.registerCapacity(HAND_CRANK.get(), 256.0f);
		KineticStressRegistry.registerCapacity(WATER_WHEEL.get(), 256.0f);
		KineticStressRegistry.registerCapacity(WINDMILL_BEARING.get(), 2048.0f);
		KineticStressRegistry.registerImpact(SHAFT.get(), 0.0f);
		KineticStressRegistry.registerImpact(COGWHEEL.get(), 0.0f);
		KineticStressRegistry.registerImpact(LARGE_COGWHEEL.get(), 0.0f);
		KineticStressRegistry.registerImpact(GEARBOX.get(), 0.0f);
		KineticStressRegistry.registerImpact(MECHANICAL_BEARING.get(), 4.0f);
		KineticStressRegistry.registerImpact(MECHANICAL_PISTON.get(), 4.0f);
		KineticStressRegistry.registerImpact(BELT.get(), 1.0f);
	}
}
