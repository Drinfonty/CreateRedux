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

	public static final RegistryEntry<BlockEntityType<com.drinfonty.create_redux.content.logistics.depot.DepotBlockEntity>> DEPOT = CreateRegistrate.registerBlockEntityType(
			"depot",
			com.drinfonty.create_redux.content.logistics.depot.DepotBlockEntity::new,
			() -> new Block[]{AllBlocks.DEPOT.get()}
	);

	public static final RegistryEntry<BlockEntityType<com.drinfonty.create_redux.content.kinetics.belt.BeltBlockEntity>> BELT = CreateRegistrate.registerBlockEntityType(
			"belt",
			com.drinfonty.create_redux.content.kinetics.belt.BeltBlockEntity::new,
			() -> new Block[]{AllBlocks.BELT.get()}
	);

	public static final RegistryEntry<BlockEntityType<com.drinfonty.create_redux.content.logistics.chute.ChuteBlockEntity>> CHUTE = CreateRegistrate.registerBlockEntityType(
			"chute",
			com.drinfonty.create_redux.content.logistics.chute.ChuteBlockEntity::new,
			() -> new Block[]{AllBlocks.CHUTE.get()}
	);

	public static final RegistryEntry<BlockEntityType<com.drinfonty.create_redux.content.logistics.funnel.FunnelBlockEntity>> FUNNEL = CreateRegistrate.registerBlockEntityType(
			"funnel",
			com.drinfonty.create_redux.content.logistics.funnel.FunnelBlockEntity::new,
			() -> new Block[]{AllBlocks.ANDESITE_FUNNEL.get(), AllBlocks.BRASS_FUNNEL.get()}
	);

	public static final RegistryEntry<BlockEntityType<com.drinfonty.create_redux.content.fluids.pipe.FluidPipeBlockEntity>> FLUID_PIPE = CreateRegistrate.registerBlockEntityType(
			"fluid_pipe",
			com.drinfonty.create_redux.content.fluids.pipe.FluidPipeBlockEntity::new,
			() -> new Block[]{AllBlocks.FLUID_PIPE.get()}
	);

	public static final RegistryEntry<BlockEntityType<com.drinfonty.create_redux.content.fluids.pump.MechanicalPumpBlockEntity>> MECHANICAL_PUMP = CreateRegistrate.registerBlockEntityType(
			"mechanical_pump",
			com.drinfonty.create_redux.content.fluids.pump.MechanicalPumpBlockEntity::new,
			() -> new Block[]{AllBlocks.MECHANICAL_PUMP.get()}
	);

	public static final RegistryEntry<BlockEntityType<com.drinfonty.create_redux.content.fluids.tank.FluidTankBlockEntity>> FLUID_TANK = CreateRegistrate.registerBlockEntityType(
			"fluid_tank",
			com.drinfonty.create_redux.content.fluids.tank.FluidTankBlockEntity::new,
			() -> new Block[]{AllBlocks.FLUID_TANK.get()}
	);

	public static final RegistryEntry<BlockEntityType<com.drinfonty.create_redux.content.fluids.spout.SpoutBlockEntity>> SPOUT = CreateRegistrate.registerBlockEntityType(
			"spout",
			com.drinfonty.create_redux.content.fluids.spout.SpoutBlockEntity::new,
			() -> new Block[]{AllBlocks.SPOUT.get()}
	);

	public static final RegistryEntry<BlockEntityType<com.drinfonty.create_redux.content.fluids.hosePulley.HosePulleyBlockEntity>> HOSE_PULLEY = CreateRegistrate.registerBlockEntityType(
			"hose_pulley",
			com.drinfonty.create_redux.content.fluids.hosePulley.HosePulleyBlockEntity::new,
			() -> new Block[]{AllBlocks.HOSE_PULLEY.get()}
	);

	public static final RegistryEntry<BlockEntityType<com.drinfonty.create_redux.content.trains.track.TrackBlockEntity>> TRACK = CreateRegistrate.registerBlockEntityType(
			"track",
			com.drinfonty.create_redux.content.trains.track.TrackBlockEntity::new,
			() -> new Block[]{AllBlocks.TRACK.get()}
	);

	public static final RegistryEntry<BlockEntityType<com.drinfonty.create_redux.content.trains.bogey.BogeyBlockEntity>> BOGEY = CreateRegistrate.registerBlockEntityType(
			"bogey",
			com.drinfonty.create_redux.content.trains.bogey.BogeyBlockEntity::new,
			() -> new Block[]{AllBlocks.SMALL_BOGEY.get(), AllBlocks.LARGE_BOGEY.get()}
	);

	public static final RegistryEntry<BlockEntityType<com.drinfonty.create_redux.content.trains.station.StationBlockEntity>> TRACK_STATION = CreateRegistrate.registerBlockEntityType(
			"track_station",
			com.drinfonty.create_redux.content.trains.station.StationBlockEntity::new,
			() -> new Block[]{AllBlocks.TRACK_STATION.get()}
	);

	public static final RegistryEntry<BlockEntityType<com.drinfonty.create_redux.content.trains.signal.TrackSignalBlockEntity>> TRACK_SIGNAL = CreateRegistrate.registerBlockEntityType(
			"track_signal",
			com.drinfonty.create_redux.content.trains.signal.TrackSignalBlockEntity::new,
			() -> new Block[]{AllBlocks.TRACK_SIGNAL.get()}
	);

	public static final RegistryEntry<BlockEntityType<com.drinfonty.create_redux.content.schematics.table.SchematicTableBlockEntity>> SCHEMATIC_TABLE = CreateRegistrate.registerBlockEntityType(
			"schematic_table",
			com.drinfonty.create_redux.content.schematics.table.SchematicTableBlockEntity::new,
			() -> new Block[]{AllBlocks.SCHEMATIC_TABLE.get()}
	);

	public static final RegistryEntry<BlockEntityType<com.drinfonty.create_redux.content.schematics.cannon.SchematicannonBlockEntity>> SCHEMATICANNON = CreateRegistrate.registerBlockEntityType(
			"schematicannon",
			com.drinfonty.create_redux.content.schematics.cannon.SchematicannonBlockEntity::new,
			() -> new Block[]{AllBlocks.SCHEMATICANNON.get()}
	);

	public static void register() {
		// triggers classloading
	}
}
