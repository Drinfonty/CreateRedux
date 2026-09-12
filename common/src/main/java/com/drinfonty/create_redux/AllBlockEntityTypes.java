package com.drinfonty.create_redux;

import com.drinfonty.create_redux.content.contraptions.components.structure.bearing.MechanicalBearingBlockEntity;
import com.drinfonty.create_redux.content.contraptions.components.structure.bearing.WindmillBearingBlockEntity;
import com.drinfonty.create_redux.content.contraptions.components.structure.piston.MechanicalPistonBlockEntity;
import com.drinfonty.create_redux.content.kinetics.GearboxBlockEntity;
import com.drinfonty.create_redux.content.kinetics.HandCrankBlockEntity;
import com.drinfonty.create_redux.content.kinetics.ShaftBlockEntity;
import com.drinfonty.create_redux.content.kinetics.WaterWheelBlockEntity;
import com.drinfonty.create_redux.platform.registry.CreateRegistrate;
import com.drinfonty.create_redux.platform.registry.RegistryEntry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class AllBlockEntityTypes {
	public static final RegistryEntry<BlockEntityType<ShaftBlockEntity>> SHAFT = CreateRegistrate.registerBlockEntityType(
			"shaft",
			ShaftBlockEntity::new,
			() -> new Block[]{AllBlocks.SHAFT.get(), AllBlocks.COGWHEEL.get(), AllBlocks.LARGE_COGWHEEL.get()}
	);

	public static final RegistryEntry<BlockEntityType<HandCrankBlockEntity>> HAND_CRANK = CreateRegistrate.registerBlockEntityType(
			"hand_crank",
			HandCrankBlockEntity::new,
			() -> new Block[]{AllBlocks.HAND_CRANK.get()}
	);

	public static final RegistryEntry<BlockEntityType<WaterWheelBlockEntity>> WATER_WHEEL = CreateRegistrate.registerBlockEntityType(
			"water_wheel",
			WaterWheelBlockEntity::new,
			() -> new Block[]{AllBlocks.WATER_WHEEL.get()}
	);

	public static final RegistryEntry<BlockEntityType<GearboxBlockEntity>> GEARBOX = CreateRegistrate.registerBlockEntityType(
			"gearbox",
			GearboxBlockEntity::new,
			() -> new Block[]{AllBlocks.GEARBOX.get()}
	);

	public static final RegistryEntry<BlockEntityType<MechanicalBearingBlockEntity>> MECHANICAL_BEARING = CreateRegistrate.registerBlockEntityType(
			"mechanical_bearing",
			MechanicalBearingBlockEntity::new,
			() -> new Block[]{AllBlocks.MECHANICAL_BEARING.get()}
	);

	public static final RegistryEntry<BlockEntityType<WindmillBearingBlockEntity>> WINDMILL_BEARING = CreateRegistrate.registerBlockEntityType(
			"windmill_bearing",
			WindmillBearingBlockEntity::new,
			() -> new Block[]{AllBlocks.WINDMILL_BEARING.get()}
	);

	public static final RegistryEntry<BlockEntityType<MechanicalPistonBlockEntity>> MECHANICAL_PISTON = CreateRegistrate.registerBlockEntityType(
			"mechanical_piston",
			MechanicalPistonBlockEntity::new,
			() -> new Block[]{AllBlocks.MECHANICAL_PISTON.get()}
	);

	public static void register() {
		// triggers classloading
	}
}
