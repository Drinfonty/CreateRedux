package com.drinfonty.create_redux.content.kinetics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class KineticBlockEntity extends BlockEntity {
	protected float speed = 0.0f;
	protected float capacity = 0.0f;
	protected float stress = 0.0f;
	protected boolean overStressed = false;
	protected float clientAngle = 0.0f;

	public KineticBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public float getSpeed() {
		return overStressed ? 0.0f : speed;
	}

	public void setSpeed(float speed) {
		if (this.speed != speed) {
			this.speed = speed;
			setChanged();
			if (level != null && !level.isClientSide()) {
				level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2 | 16);
			}
		}
	}

	public float getCapacity() {
		return capacity;
	}

	public float getStress() {
		return stress;
	}

	public boolean isOverStressed() {
		return overStressed;
	}

	public void setOverStressed(boolean overStressed) {
		if (this.overStressed != overStressed) {
			this.overStressed = overStressed;
			setChanged();
		}
	}

	public Direction.Axis getRotationAxis() {
		if (getBlockState().getBlock() instanceof KineticBlock kb) {
			return kb.getRotationAxis(getBlockState());
		}
		return Direction.Axis.Y;
	}

	public void onPlaced() {
		if (level != null && !level.isClientSide()) {
			propagateRotation();
		}
	}

	public void destroy() {
		if (level != null && !level.isClientSide()) {
			// Notify neighbors to update their kinetic networks
			for (Direction dir : Direction.values()) {
				BlockEntity be = level.getBlockEntity(worldPosition.relative(dir));
				if (be instanceof KineticBlockEntity neighbor) {
					neighbor.propagateRotation();
				}
			}
		}
	}

	public void propagateRotation() {
		if (level == null || level.isClientSide()) return;

		Direction.Axis axis = getRotationAxis();
		for (Direction dir : Direction.values()) {
			if (dir.getAxis() != axis) continue;
			BlockPos neighborPos = worldPosition.relative(dir);
			BlockEntity be = level.getBlockEntity(neighborPos);
			if (be instanceof KineticBlockEntity neighbor && neighbor.getRotationAxis() == axis) {
				if (neighbor.speed != this.speed) {
					neighbor.setSpeed(this.speed);
					neighbor.propagateRotation();
				}
			}
		}
	}

	public void tick() {
		if (level != null && level.isClientSide()) {
			float currentSpeed = getSpeed();
			if (currentSpeed != 0) {
				clientAngle = (clientAngle + currentSpeed * 3.0f / 20.0f) % 360.0f;
			}
		}
	}

	public float getClientAngle() {
		return clientAngle;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putFloat("Speed", speed);
		output.putFloat("Capacity", capacity);
		output.putFloat("Stress", stress);
		output.putBoolean("OverStressed", overStressed);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		speed = input.getFloatOr("Speed", 0.0f);
		capacity = input.getFloatOr("Capacity", 0.0f);
		stress = input.getFloatOr("Stress", 0.0f);
		overStressed = input.getBooleanOr("OverStressed", false);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		tag.putFloat("Speed", speed);
		tag.putBoolean("OverStressed", overStressed);
		return tag;
	}
}
