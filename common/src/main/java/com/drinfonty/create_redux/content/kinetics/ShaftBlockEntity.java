package com.drinfonty.create_redux.content.kinetics;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ShaftBlockEntity extends KineticBlockEntity {
	public ShaftBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.SHAFT.get(), pos, state);
	}
}
