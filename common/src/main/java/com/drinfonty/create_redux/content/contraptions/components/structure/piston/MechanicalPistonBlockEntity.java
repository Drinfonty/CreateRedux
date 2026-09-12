package com.drinfonty.create_redux.content.contraptions.components.structure.piston;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import com.drinfonty.create_redux.content.contraptions.components.structure.Contraption;
import com.drinfonty.create_redux.content.kinetics.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MechanicalPistonBlockEntity extends KineticBlockEntity {
	protected Contraption contraption;
	protected float extension = 0.0f;
	protected float maxExtension = 16.0f;
	protected boolean moving = false;

	public MechanicalPistonBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.MECHANICAL_PISTON.get(), pos, state);
	}

	public float getExtension() {
		return extension;
	}

	public float getMaxExtension() {
		return maxExtension;
	}

	public boolean isMoving() {
		return moving;
	}

	@Override
	public void tick() {
		super.tick();

		float currentSpeed = getSpeed();
		if (currentSpeed != 0) {
			float delta = (currentSpeed / 256.0f) * 0.1f;
			float newExtension = Math.clamp(extension + delta, 0.0f, maxExtension);
			if (newExtension != extension) {
				extension = newExtension;
				moving = true;
			} else {
				moving = false;
			}
		} else {
			moving = false;
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putFloat("Extension", extension);
		output.putFloat("MaxExtension", maxExtension);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		extension = input.getFloatOr("Extension", 0.0f);
		maxExtension = input.getFloatOr("MaxExtension", 16.0f);
	}
}
