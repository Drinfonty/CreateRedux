package com.drinfonty.create_redux.content.fluids.pipe;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import com.drinfonty.create_redux.platform.transfer.FluidStack;
import com.drinfonty.create_redux.platform.transfer.StorageProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FluidPipeBlockEntity extends BlockEntity implements StorageProvider<FluidStack> {
	private FluidStack fluid = FluidStack.EMPTY;
	private static final long PIPE_CAPACITY = 250; // 250 mB per pipe segment

	public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.FLUID_PIPE.get(), pos, state);
	}

	public FluidPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public FluidStack getFluid() {
		return fluid;
	}

	public void setFluid(FluidStack fluid) {
		this.fluid = fluid == null ? FluidStack.EMPTY : fluid;
		setChanged();
	}

	// --- StorageProvider<FluidStack> ---
	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public FluidStack getStackInSlot(int slot) {
		return fluid;
	}

	@Override
	public long getSlotCapacity(int slot) {
		return PIPE_CAPACITY;
	}

	@Override
	public FluidStack insert(int slot, FluidStack resource, boolean simulate) {
		if (resource.isEmpty() || slot != 0) return resource;

		if (fluid.isEmpty()) {
			long toInsert = Math.min(resource.getAmount(), PIPE_CAPACITY);
			if (!simulate) {
				setFluid(resource.copyWithAmount(toInsert));
			}
			return resource.copyWithAmount(resource.getAmount() - toInsert);
		}

		if (fluid.isFluidEqual(resource)) {
			long canAdd = PIPE_CAPACITY - fluid.getAmount();
			if (canAdd <= 0) return resource;

			long toAdd = Math.min(canAdd, resource.getAmount());
			if (!simulate) {
				setFluid(fluid.copyWithAmount(fluid.getAmount() + toAdd));
			}
			return resource.copyWithAmount(resource.getAmount() - toAdd);
		}

		return resource;
	}

	@Override
	public FluidStack extract(int slot, long maxAmount, boolean simulate) {
		if (fluid.isEmpty() || slot != 0 || maxAmount <= 0) return FluidStack.EMPTY;

		long toExtract = Math.min(fluid.getAmount(), maxAmount);
		FluidStack extracted = fluid.copyWithAmount(toExtract);
		if (!simulate) {
			setFluid(fluid.copyWithAmount(fluid.getAmount() - toExtract));
		}
		return extracted;
	}

	// --- Serialization ---
	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (!fluid.isEmpty()) {
			output.putString("FluidId", BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
			output.putLong("FluidAmount", fluid.getAmount());
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		String id = input.getStringOr("FluidId", "");
		long amount = input.getLongOr("FluidAmount", 0L);
		if (!id.isEmpty() && amount > 0) {
			BuiltInRegistries.FLUID.get(Identifier.parse(id))
					.map(Holder.Reference::value)
					.filter(f -> f != Fluids.EMPTY)
					.ifPresent(f -> this.fluid = new FluidStack(f, amount));
		}
	}
}
