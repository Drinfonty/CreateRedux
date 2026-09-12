package com.drinfonty.create_redux.content.fluids.hosePulley;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import com.drinfonty.create_redux.content.kinetics.KineticBlockEntity;
import com.drinfonty.create_redux.platform.transfer.FluidStack;
import com.drinfonty.create_redux.platform.transfer.StorageProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class HosePulleyBlockEntity extends KineticBlockEntity implements StorageProvider<FluidStack> {
	private float extension = 0.0f;
	private float maxExtension = 128.0f;
	private FluidStack internalBuffer = FluidStack.EMPTY;
	private static final long BUFFER_CAPACITY = 1000;

	public HosePulleyBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.HOSE_PULLEY.get(), pos, state);
	}

	public HosePulleyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public float getExtension() {
		return extension;
	}

	@Override
	public void tick() {
		super.tick();

		float spd = getSpeed();
		if (spd != 0.0f) {
			float delta = (spd / 256.0f);
			extension = Math.max(0.0f, Math.min(maxExtension, extension + delta));
		}

		if (level == null || level.isClientSide()) return;

		// Check fluid at tip of hose
		int tipDepth = (int) Math.floor(extension);
		if (tipDepth > 0 && internalBuffer.isEmpty()) {
			BlockPos tipPos = worldPosition.below(tipDepth);
			BlockState tipState = level.getBlockState(tipPos);

			if (tipState.is(Blocks.WATER)) {
				internalBuffer = new FluidStack(Fluids.WATER, 1000);
			} else if (tipState.is(Blocks.LAVA)) {
				internalBuffer = new FluidStack(Fluids.LAVA, 1000);
			}
		}
	}

	// --- StorageProvider<FluidStack> ---
	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public FluidStack getStackInSlot(int slot) {
		return internalBuffer;
	}

	@Override
	public long getSlotCapacity(int slot) {
		return BUFFER_CAPACITY;
	}

	@Override
	public FluidStack insert(int slot, FluidStack resource, boolean simulate) {
		if (resource.isEmpty() || slot != 0) return resource;

		if (internalBuffer.isEmpty()) {
			long toInsert = Math.min(resource.getAmount(), BUFFER_CAPACITY);
			if (!simulate) {
				internalBuffer = resource.copyWithAmount(toInsert);
				setChanged();
			}
			return resource.copyWithAmount(resource.getAmount() - toInsert);
		}
		return resource;
	}

	@Override
	public FluidStack extract(int slot, long maxAmount, boolean simulate) {
		if (internalBuffer.isEmpty() || slot != 0 || maxAmount <= 0) return FluidStack.EMPTY;

		long toExtract = Math.min(internalBuffer.getAmount(), maxAmount);
		FluidStack extracted = internalBuffer.copyWithAmount(toExtract);
		if (!simulate) {
			long rem = internalBuffer.getAmount() - toExtract;
			internalBuffer = internalBuffer.copyWithAmount(rem);
			setChanged();
		}
		return extracted;
	}

	// --- Serialization ---
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
		maxExtension = input.getFloatOr("MaxExtension", 128.0f);
	}
}
