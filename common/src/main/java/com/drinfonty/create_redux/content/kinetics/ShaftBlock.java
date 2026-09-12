package com.drinfonty.create_redux.content.kinetics;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ShaftBlock extends KineticBlock {
	protected static final VoxelShape X_AXIS_AABB = Block.box(0, 6, 6, 16, 10, 10);
	protected static final VoxelShape Y_AXIS_AABB = Block.box(6, 0, 6, 10, 16, 10);
	protected static final VoxelShape Z_AXIS_AABB = Block.box(6, 6, 0, 10, 10, 16);

	public ShaftBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		Direction.Axis axis = state.getValue(AXIS);
		return switch (axis) {
			case X -> X_AXIS_AABB;
			case Z -> Z_AXIS_AABB;
			default -> Y_AXIS_AABB;
		};
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ShaftBlockEntity(pos, state);
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return (lvl, pos, st, be) -> {
			if (be instanceof KineticBlockEntity kbe) {
				kbe.tick();
			}
		};
	}
}
