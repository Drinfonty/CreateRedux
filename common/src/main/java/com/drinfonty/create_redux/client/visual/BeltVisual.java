package com.drinfonty.create_redux.client.visual;

import com.drinfonty.create_redux.content.kinetics.belt.BeltBlock;
import com.drinfonty.create_redux.content.kinetics.belt.BeltBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Instanced visual for conveyor belts and internal rotating pulleys.
 */
public class BeltVisual extends InstancedVisual<BeltBlockEntity> implements DynamicVisual {
	private FlwModelInstance beltInstance;
	private FlwModelInstance pulleyInstance;
	private Direction.Axis pulleyAxis = Direction.Axis.X;

	public BeltVisual(BeltBlockEntity blockEntity) {
		super(blockEntity, blockEntity.getBlockPos());
	}

	@Override
	public void init() {
		BlockState state = blockEntity.getBlockState();
		Direction facing = Direction.NORTH;
		if (state.hasProperty(BeltBlock.HORIZONTAL_FACING)) {
			facing = state.getValue(BeltBlock.HORIZONTAL_FACING);
		}

		// Pulley axis is perpendicular to conveyor transport direction
		this.pulleyAxis = (facing.getAxis() == Direction.Axis.Z) ? Direction.Axis.X : Direction.Axis.Z;

		this.beltInstance = new FlwModelInstance(state, pos.getX(), pos.getY(), pos.getZ());
		this.pulleyInstance = new FlwModelInstance("pulley", pos.getX(), pos.getY(), pos.getZ());
		this.pulleyInstance.setRotation(this.pulleyAxis, 0.0f);

		this.instances.add(this.beltInstance);
		this.instances.add(this.pulleyInstance);
	}

	@Override
	public void update(float partialTicks) {
		BlockState state = blockEntity.getBlockState();
		if (state.hasProperty(BeltBlock.HORIZONTAL_FACING)) {
			Direction facing = state.getValue(BeltBlock.HORIZONTAL_FACING);
			this.pulleyAxis = (facing.getAxis() == Direction.Axis.Z) ? Direction.Axis.X : Direction.Axis.Z;
		}
		if (beltInstance != null) {
			beltInstance.setModel(state);
		}
	}

	@Override
	public void beginFrame(float partialTicks) {
		if (deleted) return;

		float speed = blockEntity.getSpeed();
		float angle = KineticVisualMath.getAngle(speed, partialTicks, blockEntity.getRotationOffset());
		if (pulleyInstance != null) {
			pulleyInstance.setRotation(pulleyAxis, angle);
		}

		float scroll = KineticVisualMath.getBeltScroll(speed, partialTicks);
		if (beltInstance != null) {
			beltInstance.setTextureScroll(0.0f, scroll);
		}
	}

	public Direction.Axis getPulleyAxis() {
		return pulleyAxis;
	}

	public FlwModelInstance getBeltInstance() {
		return beltInstance;
	}

	public FlwModelInstance getPulleyInstance() {
		return pulleyInstance;
	}
}
