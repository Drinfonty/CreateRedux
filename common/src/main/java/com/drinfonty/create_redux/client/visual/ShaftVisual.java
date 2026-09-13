package com.drinfonty.create_redux.client.visual;

import com.drinfonty.create_redux.content.kinetics.KineticBlock;
import com.drinfonty.create_redux.content.kinetics.KineticBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Instanced visual for shafts and rotational kinetic blocks.
 */
public class ShaftVisual extends InstancedVisual<KineticBlockEntity> implements DynamicVisual {
	protected FlwModelInstance shaftInstance;
	protected Direction.Axis axis = Direction.Axis.Y;

	public ShaftVisual(KineticBlockEntity blockEntity) {
		super(blockEntity, blockEntity.getBlockPos());
	}

	@Override
	public void init() {
		BlockState state = blockEntity.getBlockState();
		if (state.hasProperty(KineticBlock.AXIS)) {
			this.axis = state.getValue(KineticBlock.AXIS);
		} else {
			this.axis = Direction.Axis.Y;
		}

		this.shaftInstance = new FlwModelInstance(state, pos.getX(), pos.getY(), pos.getZ());
		this.shaftInstance.setRotation(this.axis, 0.0f);
		this.instances.add(this.shaftInstance);
	}

	@Override
	public void update(float partialTicks) {
		BlockState state = blockEntity.getBlockState();
		if (state.hasProperty(KineticBlock.AXIS)) {
			this.axis = state.getValue(KineticBlock.AXIS);
		}
		if (shaftInstance != null) {
			shaftInstance.setModel(state);
		}
	}

	@Override
	public void beginFrame(float partialTicks) {
		if (shaftInstance == null || deleted) return;

		float speed = blockEntity.getSpeed();
		if (speed == 0) {
			shaftInstance.setRotation(axis, 0.0f);
			return;
		}

		float angle = KineticVisualMath.getAngle(speed, partialTicks, blockEntity.getRotationOffset());
		shaftInstance.setRotation(axis, angle);
	}

	public Direction.Axis getAxis() {
		return axis;
	}

	public FlwModelInstance getShaftInstance() {
		return shaftInstance;
	}
}
