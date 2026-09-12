package com.drinfonty.create_redux.content.contraptions.components.structure.bearing;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WindmillBearingBlock extends BearingBlock {
	public WindmillBearingBlock(Properties properties) {
		super(properties);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WindmillBearingBlockEntity(pos, state);
	}
}
