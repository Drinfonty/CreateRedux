package com.simibubi.create;

import com.simibubi.create.platform.registry.CreateRegistrate;
import com.simibubi.create.platform.registry.RegistryEntry;
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
					})
					.build()
	);

	public static void register() {
		// triggers static initialization
	}
}
