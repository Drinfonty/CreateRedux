package com.simibubi.create.content.kinetics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CogWheelBlock extends ShaftBlock {
	protected static final VoxelShape SMALL_X_AABB = Block.box(6, 0, 0, 10, 16, 16);
	protected static final VoxelShape SMALL_Y_AABB = Block.box(0, 6, 0, 16, 10, 16);
	protected static final VoxelShape SMALL_Z_AABB = Block.box(0, 0, 6, 16, 16, 10);

	private final boolean isLarge;

	public static CogWheelBlock small(Properties properties) {
		return new CogWheelBlock(false, properties);
	}

	public static CogWheelBlock large(Properties properties) {
		return new CogWheelBlock(true, properties);
	}

	protected CogWheelBlock(boolean isLarge, Properties properties) {
		super(properties);
		this.isLarge = isLarge;
	}

	public boolean isLarge() {
		return isLarge;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (!isLarge) {
			Direction.Axis axis = state.getValue(AXIS);
			return switch (axis) {
				case X -> SMALL_X_AABB;
				case Z -> SMALL_Z_AABB;
				default -> SMALL_Y_AABB;
			};
		}
		return super.getShape(state, level, pos, context);
	}
}
