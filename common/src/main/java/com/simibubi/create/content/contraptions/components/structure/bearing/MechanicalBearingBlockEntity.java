package com.simibubi.create.content.contraptions.components.structure.bearing;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.kinetics.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MechanicalBearingBlockEntity extends KineticBlockEntity {
	protected BearingContraption contraption;
	protected float angle = 0.0f;
	protected boolean running = false;

	public MechanicalBearingBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.MECHANICAL_BEARING.get(), pos, state);
	}

	public MechanicalBearingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public boolean isAssembled() {
		return contraption != null && contraption.isAssembled();
	}

	public float getAngle() {
		return angle;
	}

	public BearingContraption getContraption() {
		return contraption;
	}

	public boolean assemble() {
		if (level == null || level.isClientSide()) return false;

		Direction facing = getBlockState().getValue(BearingBlock.FACING);
		BlockPos startPos = worldPosition.relative(facing);

		BearingContraption c = new BearingContraption(facing);
		if (c.assemble(level, startPos, 1024)) {
			c.removeBlocksFromWorld(level);
			this.contraption = c;
			this.running = true;
			setChanged();
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2 | 16);
			return true;
		}
		return false;
	}

	public void disassemble() {
		if (level == null || level.isClientSide()) return;

		if (contraption != null && contraption.isAssembled()) {
			Direction facing = getBlockState().getValue(BearingBlock.FACING);
			BlockPos targetPos = worldPosition.relative(facing);
			contraption.disassembleIntoWorld(level, targetPos);
			this.contraption = null;
			this.running = false;
			setChanged();
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2 | 16);
		}
	}

	@Override
	public void tick() {
		super.tick();

		if (running && getSpeed() != 0) {
			angle = (angle + getSpeed() * 3.0f / 20.0f) % 360.0f;
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putFloat("Angle", angle);
		output.putBoolean("Running", running);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		angle = input.getFloatOr("Angle", 0.0f);
		running = input.getBooleanOr("Running", false);
	}
}
