package com.simibubi.create.content.kinetics;

import com.simibubi.create.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HandCrankBlockEntity extends KineticBlockEntity {
	protected int inUse = 0;

	public HandCrankBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.HAND_CRANK.get(), pos, state);
	}

	public void turn(boolean backwards) {
		inUse = 30;
		this.capacity = 256.0f;
		setSpeed(backwards ? -32.0f : 32.0f);
		propagateRotation();
	}

	@Override
	public Direction.Axis getRotationAxis() {
		return getBlockState().getValue(HandCrankBlock.FACING).getAxis();
	}

	@Override
	public void propagateRotation() {
		if (level == null || level.isClientSide()) return;

		Direction facing = getBlockState().getValue(HandCrankBlock.FACING);
		Direction shaftDir = facing.getOpposite();
		BlockPos neighborPos = worldPosition.relative(shaftDir);
		BlockEntity be = level.getBlockEntity(neighborPos);
		if (be instanceof KineticBlockEntity neighbor && neighbor.getRotationAxis() == facing.getAxis()) {
			if (neighbor.speed != this.speed) {
				neighbor.setSpeed(this.speed);
				neighbor.propagateRotation();
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (level != null && !level.isClientSide()) {
			if (inUse > 0) {
				inUse--;
				if (inUse == 0) {
					this.capacity = 0.0f;
					setSpeed(0.0f);
					propagateRotation();
				}
			}
		}
	}
}
