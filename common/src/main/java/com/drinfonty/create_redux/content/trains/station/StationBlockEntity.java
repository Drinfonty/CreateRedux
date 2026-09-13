package com.drinfonty.create_redux.content.trains.station;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import com.drinfonty.create_redux.content.trains.bogey.BogeyBlock;
import com.drinfonty.create_redux.content.trains.bogey.BogeyBlockEntity;
import com.drinfonty.create_redux.content.trains.entity.Carriage;
import com.drinfonty.create_redux.content.trains.entity.CarriageContraption;
import com.drinfonty.create_redux.content.trains.entity.Train;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StationBlockEntity extends BlockEntity {
	private String stationName = "Central Station";
	private boolean assembling = false;
	private @Nullable Train assembledTrain = null;
	private boolean trainPresent = false;

	public StationBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.TRACK_STATION.get(), pos, state);
	}

	public String getStationName() {
		return stationName;
	}

	public void setStationName(String stationName) {
		this.stationName = stationName;
		setChanged();
	}

	public boolean isAssembling() {
		return assembling;
	}

	public void setAssembling(boolean assembling) {
		this.assembling = assembling;
		setChanged();
	}

	public @Nullable Train getAssembledTrain() {
		return assembledTrain;
	}

	public boolean isTrainPresent() {
		return trainPresent;
	}

	public void setTrainPresent(boolean trainPresent) {
		this.trainPresent = trainPresent;
		setChanged();
	}

	/**
	 * Attempt to assemble a train located on the track adjacent to this station.
	 */
	public boolean assembleTrain(Level level) {
		Direction facing = getBlockState().getValue(StationBlock.FACING);
		BlockPos trackPos = getBlockPos().relative(facing);

		// Scan along the orthogonal axis of facing for bogeys
		Direction.Axis trackAxis = facing.getClockWise().getAxis();
		List<BlockPos> detectedBogeys = new ArrayList<>();

		for (int offset = -16; offset <= 16; offset++) {
			BlockPos checkPos = trackPos.relative(Direction.fromAxisAndDirection(trackAxis, offset >= 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE), Math.abs(offset));
			// Look for bogey on track or above track
			if (level.getBlockState(checkPos).getBlock() instanceof BogeyBlock) {
				detectedBogeys.add(checkPos);
			} else if (level.getBlockState(checkPos.above()).getBlock() instanceof BogeyBlock) {
				detectedBogeys.add(checkPos.above());
			}
		}

		if (detectedBogeys.isEmpty()) {
			return false;
		}

		Train train = new Train(UUID.randomUUID(), stationName + " Express");

		// Group detected bogeys into carriages (1 or 2 bogeys per carriage)
		for (int i = 0; i < detectedBogeys.size(); i++) {
			BlockPos bogeyPos = detectedBogeys.get(i);
			CarriageContraption contraption = new CarriageContraption();
			if (contraption.assembleCarriage(level, List.of(bogeyPos), 1024)) {
				contraption.removeBlocksFromWorld(level);
				Carriage carriage = new Carriage(i, contraption, List.of(bogeyPos));
				train.addCarriage(carriage);

				BlockEntity be = level.getBlockEntity(bogeyPos);
				if (be instanceof BogeyBlockEntity bogeyBE) {
					bogeyBE.setCoupledTrainId(train.getId());
					bogeyBE.setCarriageIndex(i);
				}
			}
		}

		if (train.getCarriages().isEmpty()) {
			return false;
		}

		this.assembledTrain = train;
		this.trainPresent = true;
		this.assembling = false;
		setChanged();
		return true;
	}

	public boolean disassembleTrain(Level level) {
		if (assembledTrain != null) {
			assembledTrain.disassemble(level);
			assembledTrain = null;
			trainPresent = false;
			setChanged();
			return true;
		}
		return false;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("StationName", stationName);
		output.putBoolean("Assembling", assembling);
		output.putBoolean("TrainPresent", trainPresent);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.stationName = input.getStringOr("StationName", "Central Station");
		this.assembling = input.getBooleanOr("Assembling", false);
		this.trainPresent = input.getBooleanOr("TrainPresent", false);
	}
}
