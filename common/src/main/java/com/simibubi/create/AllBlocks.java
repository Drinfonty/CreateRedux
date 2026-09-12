package com.simibubi.create;

import com.simibubi.create.content.kinetics.CogWheelBlock;
import com.simibubi.create.content.kinetics.ShaftBlock;
import com.simibubi.create.platform.registry.CreateRegistrate;
import com.simibubi.create.platform.registry.RegistryEntry;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class AllBlocks {
	public static final RegistryEntry<ShaftBlock> SHAFT = CreateRegistrate.registerBlock(
			"shaft",
			() -> new ShaftBlock(BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.WOOD).noOcclusion())
	);

	public static final RegistryEntry<CogWheelBlock> COGWHEEL = CreateRegistrate.registerBlock(
			"cogwheel",
			() -> CogWheelBlock.small(BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.WOOD).noOcclusion())
	);

	public static final RegistryEntry<CogWheelBlock> LARGE_COGWHEEL = CreateRegistrate.registerBlock(
			"large_cogwheel",
			() -> CogWheelBlock.large(BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.WOOD).noOcclusion())
	);

	public static void register() {
		// triggers static initialization
	}
}
