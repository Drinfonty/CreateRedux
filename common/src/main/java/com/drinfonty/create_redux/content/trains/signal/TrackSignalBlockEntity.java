package com.drinfonty.create_redux.content.trains.signal;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TrackSignalBlockEntity extends BlockEntity {
	private SignalState signalState = SignalState.GREEN;
	private @Nullable UUID reservedByTrainId = null;

	public TrackSignalBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.TRACK_SIGNAL.get(), pos, state);
	}

	public SignalState getSignalState() {
		return signalState;
	}

	public void setSignalState(SignalState signalState) {
		this.signalState = signalState;
		if (level != null && !level.isClientSide()) {
			BlockState current = getBlockState();
			if (current.hasProperty(TrackSignalBlock.SIGNAL_STATE) && current.getValue(TrackSignalBlock.SIGNAL_STATE) != signalState) {
				level.setBlock(getBlockPos(), current.setValue(TrackSignalBlock.SIGNAL_STATE, signalState), 3);
			}
		}
		setChanged();
	}

	public @Nullable UUID getReservedByTrainId() {
		return reservedByTrainId;
	}

	public boolean reserve(UUID trainId) {
		if (reservedByTrainId == null || reservedByTrainId.equals(trainId)) {
			reservedByTrainId = trainId;
			setSignalState(SignalState.RED);
			return true;
		}
		return false;
	}

	public void release(UUID trainId) {
		if (reservedByTrainId != null && reservedByTrainId.equals(trainId)) {
			reservedByTrainId = null;
			setSignalState(SignalState.GREEN);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("SignalState", signalState.getSerializedName());
		if (reservedByTrainId != null) {
			output.putString("ReservedBy", reservedByTrainId.toString());
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		String stateName = input.getStringOr("SignalState", "green");
		try {
			this.signalState = SignalState.valueOf(stateName.toUpperCase());
		} catch (IllegalArgumentException e) {
			this.signalState = SignalState.GREEN;
		}
		String reservedStr = input.getStringOr("ReservedBy", "");
		if (!reservedStr.isEmpty()) {
			try {
				this.reservedByTrainId = UUID.fromString(reservedStr);
			} catch (IllegalArgumentException ignored) {}
		}
	}
}
