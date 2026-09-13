package com.drinfonty.create_redux.content.trains.bogey;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BogeyBlockEntity extends BlockEntity {
	private BogeySizes size = BogeySizes.SMALL;
	private float wheelAngle = 0.0f;
	private @Nullable UUID coupledTrainId = null;
	private int carriageIndex = -1;

	public BogeyBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.BOGEY.get(), pos, state);
	}

	public BogeySizes getSize() {
		return size;
	}

	public void setSize(BogeySizes size) {
		this.size = size;
		setChanged();
	}

	public float getWheelAngle() {
		return wheelAngle;
	}

	public void setWheelAngle(float wheelAngle) {
		this.wheelAngle = wheelAngle;
	}

	public void animate(double distanceTraveled) {
		double circumference = Math.PI * size.getWheelDiameter();
		double degrees = (distanceTraveled / circumference) * 360.0;
		this.wheelAngle = (float) ((this.wheelAngle + degrees) % 360.0);
	}

	public @Nullable UUID getCoupledTrainId() {
		return coupledTrainId;
	}

	public void setCoupledTrainId(@Nullable UUID coupledTrainId) {
		this.coupledTrainId = coupledTrainId;
		setChanged();
	}

	public int getCarriageIndex() {
		return carriageIndex;
	}

	public void setCarriageIndex(int carriageIndex) {
		this.carriageIndex = carriageIndex;
		setChanged();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("BogeySize", size.getSerializedName());
		output.putFloat("WheelAngle", wheelAngle);
		if (coupledTrainId != null) {
			output.putString("CoupledTrain", coupledTrainId.toString());
		}
		output.putInt("CarriageIndex", carriageIndex);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		String name = input.getStringOr("BogeySize", "small");
		this.size = "large".equalsIgnoreCase(name) ? BogeySizes.LARGE : BogeySizes.SMALL;
		this.wheelAngle = input.getFloatOr("WheelAngle", 0.0f);
		String coupledStr = input.getStringOr("CoupledTrain", "");
		if (!coupledStr.isEmpty()) {
			try {
				this.coupledTrainId = UUID.fromString(coupledStr);
			} catch (IllegalArgumentException ignored) {}
		}
		this.carriageIndex = input.getIntOr("CarriageIndex", -1);
	}
}
