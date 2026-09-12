package com.simibubi.create.content.contraptions.components.structure.bearing;

import com.simibubi.create.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WindmillBearingBlockEntity extends MechanicalBearingBlockEntity {
	public WindmillBearingBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.WINDMILL_BEARING.get(), pos, state);
	}

	@Override
	public boolean assemble() {
		boolean success = super.assemble();
		if (success && contraption != null) {
			int sails = contraption.getSailBlocksCount();
			if (sails >= 8) {
				this.capacity = sails * 512.0f;
				setSpeed(Math.min(16.0f + sails * 1.5f, 64.0f));
				propagateRotation();
			}
		}
		return success;
	}

	@Override
	public void disassemble() {
		super.disassemble();
		this.capacity = 0.0f;
		setSpeed(0.0f);
		propagateRotation();
	}

	@Override
	public void propagateRotation() {
		if (level == null || level.isClientSide()) return;

		Direction facing = getBlockState().getValue(BearingBlock.FACING);
		Direction backDir = facing.getOpposite();
		BlockPos neighborPos = worldPosition.relative(backDir);
		BlockEntity be = level.getBlockEntity(neighborPos);
		if (be instanceof com.simibubi.create.content.kinetics.KineticBlockEntity neighbor && neighbor.getRotationAxis() == facing.getAxis()) {
			if (neighbor.getSpeed() != this.speed) {
				neighbor.setSpeed(this.speed);
				neighbor.propagateRotation();
			}
		}
	}
}
