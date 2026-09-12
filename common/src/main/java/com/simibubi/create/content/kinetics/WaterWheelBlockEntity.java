package com.simibubi.create.content.kinetics;

import com.simibubi.create.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class WaterWheelBlockEntity extends KineticBlockEntity {
	protected int checkTimer = 0;

	public WaterWheelBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.WATER_WHEEL.get(), pos, state);
	}

	@Override
	public void onPlaced() {
		super.onPlaced();
		updateWaterFlow();
	}

	public void updateWaterFlow() {
		if (level == null || level.isClientSide()) return;

		float flowSpeed = 0.0f;
		for (Direction dir : Direction.values()) {
			FluidState fluid = level.getFluidState(worldPosition.relative(dir));
			if (fluid.is(FluidTags.WATER)) {
				flowSpeed = 16.0f;
				break;
			}
		}

		this.capacity = flowSpeed != 0 ? 256.0f : 0.0f;
		setSpeed(flowSpeed);
		propagateRotation();
	}

	@Override
	public void tick() {
		super.tick();
		if (level != null && !level.isClientSide()) {
			checkTimer++;
			if (checkTimer >= 20) {
				checkTimer = 0;
				updateWaterFlow();
			}
		}
	}
}
