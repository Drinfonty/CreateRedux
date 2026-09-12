package com.simibubi.create;

import com.simibubi.create.content.kinetics.ShaftBlockEntity;
import com.simibubi.create.platform.registry.CreateRegistrate;
import com.simibubi.create.platform.registry.RegistryEntry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class AllBlockEntityTypes {
	public static final RegistryEntry<BlockEntityType<ShaftBlockEntity>> SHAFT = CreateRegistrate.registerBlockEntityType(
			"shaft",
			ShaftBlockEntity::new,
			() -> new Block[]{AllBlocks.SHAFT.get(), AllBlocks.COGWHEEL.get(), AllBlocks.LARGE_COGWHEEL.get()}
	);

	public static void register() {
		// triggers classloading
	}
}
