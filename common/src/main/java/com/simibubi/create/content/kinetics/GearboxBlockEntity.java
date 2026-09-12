package com.simibubi.create.content.kinetics;

import com.simibubi.create.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GearboxBlockEntity extends KineticBlockEntity {
	public GearboxBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.GEARBOX.get(), pos, state);
	}

	@Override
	public void propagateRotation() {
		if (level == null || level.isClientSide()) return;

		Direction.Axis primaryAxis = getRotationAxis();
		for (Direction dir : Direction.values()) {
			BlockPos neighborPos = worldPosition.relative(dir);
			BlockEntity be = level.getBlockEntity(neighborPos);
			if (be instanceof KineticBlockEntity neighbor) {
				float targetSpeed = dir.getAxis() == primaryAxis ? this.speed : -this.speed;
				if (neighbor.speed != targetSpeed) {
					neighbor.setSpeed(targetSpeed);
					neighbor.propagateRotation();
				}
			}
		}
	}
}
