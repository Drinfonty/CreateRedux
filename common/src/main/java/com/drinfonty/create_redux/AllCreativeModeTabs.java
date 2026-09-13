package com.drinfonty.create_redux;

import com.drinfonty.create_redux.platform.registry.CreateRegistrate;
import com.drinfonty.create_redux.platform.registry.RegistryEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AllCreativeModeTabs {
	public static final RegistryEntry<CreativeModeTab> BASE_CREATIVE_TAB = CreateRegistrate.registerCreativeTab(
			"base",
			() -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
					.title(Component.translatable("itemGroup.create.base"))
					.icon(() -> AllBlocks.SHAFT != null ? new ItemStack(AllBlocks.SHAFT.get()) : new ItemStack(Items.COPPER_INGOT))
					.displayItems((params, output) -> {
						if (AllBlocks.SHAFT != null) output.accept(AllBlocks.SHAFT.get());
						if (AllBlocks.COGWHEEL != null) output.accept(AllBlocks.COGWHEEL.get());
						if (AllBlocks.LARGE_COGWHEEL != null) output.accept(AllBlocks.LARGE_COGWHEEL.get());
						if (AllBlocks.HAND_CRANK != null) output.accept(AllBlocks.HAND_CRANK.get());
						if (AllBlocks.WATER_WHEEL != null) output.accept(AllBlocks.WATER_WHEEL.get());
						if (AllBlocks.GEARBOX != null) output.accept(AllBlocks.GEARBOX.get());
						if (AllBlocks.MECHANICAL_BEARING != null) output.accept(AllBlocks.MECHANICAL_BEARING.get());
						if (AllBlocks.WINDMILL_BEARING != null) output.accept(AllBlocks.WINDMILL_BEARING.get());
						if (AllBlocks.MECHANICAL_PISTON != null) output.accept(AllBlocks.MECHANICAL_PISTON.get());
						if (AllBlocks.PISTON_EXTENSION_POLE != null) output.accept(AllBlocks.PISTON_EXTENSION_POLE.get());
						if (AllBlocks.DEPOT != null) output.accept(AllBlocks.DEPOT.get());
						if (AllBlocks.BELT != null) output.accept(AllBlocks.BELT.get());
						if (AllBlocks.CHUTE != null) output.accept(AllBlocks.CHUTE.get());
						if (AllBlocks.ANDESITE_FUNNEL != null) output.accept(AllBlocks.ANDESITE_FUNNEL.get());
						if (AllBlocks.BRASS_FUNNEL != null) output.accept(AllBlocks.BRASS_FUNNEL.get());
						if (AllBlocks.FLUID_PIPE != null) output.accept(AllBlocks.FLUID_PIPE.get());
						if (AllBlocks.MECHANICAL_PUMP != null) output.accept(AllBlocks.MECHANICAL_PUMP.get());
						if (AllBlocks.FLUID_TANK != null) output.accept(AllBlocks.FLUID_TANK.get());
						if (AllBlocks.SPOUT != null) output.accept(AllBlocks.SPOUT.get());
						if (AllBlocks.HOSE_PULLEY != null) output.accept(AllBlocks.HOSE_PULLEY.get());
						if (AllBlocks.TRACK != null) output.accept(AllBlocks.TRACK.get());
						if (AllBlocks.SMALL_BOGEY != null) output.accept(AllBlocks.SMALL_BOGEY.get());
						if (AllBlocks.LARGE_BOGEY != null) output.accept(AllBlocks.LARGE_BOGEY.get());
						if (AllBlocks.TRACK_STATION != null) output.accept(AllBlocks.TRACK_STATION.get());
						if (AllBlocks.TRACK_SIGNAL != null) output.accept(AllBlocks.TRACK_SIGNAL.get());
					})
					.build()
	);

	public static void register() {
		// triggers static initialization
	}
}
