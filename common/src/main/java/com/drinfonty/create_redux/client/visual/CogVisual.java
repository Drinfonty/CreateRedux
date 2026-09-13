package com.drinfonty.create_redux.client.visual;

import com.drinfonty.create_redux.AllBlocks;
import com.drinfonty.create_redux.content.kinetics.KineticBlockEntity;

/**
 * Instanced visual for cogwheels with gear tooth meshing alignment.
 */
public class CogVisual extends ShaftVisual {
	private final boolean isLargeCog;

	public CogVisual(KineticBlockEntity blockEntity) {
		super(blockEntity);
		this.isLargeCog = blockEntity.getBlockState().is(AllBlocks.LARGE_COGWHEEL.get());
	}

	@Override
	public void beginFrame(float partialTicks) {
		if (shaftInstance == null || deleted) return;

		float speed = blockEntity.getSpeed();
		float cogOffset = KineticVisualMath.getCogOffset(pos, isLargeCog);

		if (speed == 0) {
			shaftInstance.setRotation(axis, cogOffset);
			return;
		}

		float angle = KineticVisualMath.getAngle(speed, partialTicks, blockEntity.getRotationOffset() + cogOffset);
		shaftInstance.setRotation(axis, angle);
	}

	public boolean isLargeCog() {
		return isLargeCog;
	}
}
