package com.drinfonty.create_redux.content.fluids.spout;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import com.drinfonty.create_redux.content.kinetics.belt.BeltBlockEntity;
import com.drinfonty.create_redux.content.logistics.TransportedItemStack;
import com.drinfonty.create_redux.content.logistics.depot.DepotBlockEntity;
import com.drinfonty.create_redux.platform.transfer.FluidStack;
import com.drinfonty.create_redux.platform.transfer.StorageProvider;
import com.drinfonty.create_redux.platform.transfer.TransferUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

public class SpoutBlockEntity extends BlockEntity implements StorageProvider<FluidStack> {
	private FluidStack fluid = FluidStack.EMPTY;
	private static final long CAPACITY = 1000;
	private int cooldown = 0;

	public SpoutBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.SPOUT.get(), pos, state);
	}

	public SpoutBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public FluidStack getFluid() {
		return fluid;
	}

	public void setFluid(FluidStack fluid) {
		this.fluid = fluid == null ? FluidStack.EMPTY : fluid;
		setChanged();
	}

	public void tick() {
		if (level == null || level.isClientSide()) return;

		// 1. If buffer has room, pull from fluid container above
		if (fluid.isEmpty() || fluid.getAmount() < CAPACITY) {
			Optional<StorageProvider<FluidStack>> aboveStorage =
					TransferUtil.getFluidStorage(level, worldPosition.above(), Direction.DOWN);
			if (aboveStorage.isPresent()) {
				long needed = CAPACITY - (fluid.isEmpty() ? 0 : fluid.getAmount());
				FluidStack extracted = aboveStorage.get().extract(0, needed, false);
				if (!extracted.isEmpty()) {
					if (fluid.isEmpty()) {
						setFluid(extracted);
					} else if (fluid.isFluidEqual(extracted)) {
						setFluid(fluid.copyWithAmount(fluid.getAmount() + extracted.getAmount()));
					}
				}
			}
		}

		if (fluid.isEmpty()) return;

		if (cooldown > 0) {
			cooldown--;
			return;
		}

		// 2. Dispense onto item on depot or belt below
		BlockPos belowPos = worldPosition.below();
		BlockEntity belowBe = level.getBlockEntity(belowPos);

		if (belowBe instanceof DepotBlockEntity depot) {
			TransportedItemStack tis = depot.getHeldItem();
			if (tis != null && !tis.stack.isEmpty()) {
				if (processItem(tis)) {
					depot.setChanged();
					depot.notifyUpdate();
					cooldown = 10;
				}
			}
		} else if (belowBe instanceof BeltBlockEntity belt) {
			for (TransportedItemStack tis : belt.getItems()) {
				if (tis.beltPosition >= 0.4f && tis.beltPosition <= 0.6f) {
					if (processItem(tis)) {
						belt.setChanged();
						belt.notifyUpdate();
						cooldown = 10;
						break;
					}
				}
			}
		}
	}

	private boolean processItem(TransportedItemStack tis) {
		ItemStack stack = tis.stack;

		// Fill empty bucket with water
		if (stack.is(Items.BUCKET) && fluid.getFluid() == Fluids.WATER && fluid.getAmount() >= 1000) {
			tis.stack = new ItemStack(Items.WATER_BUCKET);
			setFluid(fluid.copyWithAmount(fluid.getAmount() - 1000));
			return true;
		}

		// Fill empty bucket with lava
		if (stack.is(Items.BUCKET) && fluid.getFluid() == Fluids.LAVA && fluid.getAmount() >= 1000) {
			tis.stack = new ItemStack(Items.LAVA_BUCKET);
			setFluid(fluid.copyWithAmount(fluid.getAmount() - 1000));
			return true;
		}

		// Fill glass bottle with water
		if (stack.is(Items.GLASS_BOTTLE) && fluid.getFluid() == Fluids.WATER && fluid.getAmount() >= 250) {
			tis.stack = net.minecraft.world.item.alchemy.PotionContents.createItemStack(
					Items.POTION,
					net.minecraft.world.item.alchemy.Potions.WATER
			);
			setFluid(fluid.copyWithAmount(fluid.getAmount() - 250));
			return true;
		}

		return false;
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
		return CAPACITY;
	}

	@Override
	public FluidStack insert(int slot, FluidStack resource, boolean simulate) {
		if (resource.isEmpty() || slot != 0) return resource;

		if (fluid.isEmpty()) {
			long toInsert = Math.min(resource.getAmount(), CAPACITY);
			if (!simulate) {
				setFluid(resource.copyWithAmount(toInsert));
			}
			return resource.copyWithAmount(resource.getAmount() - toInsert);
		}

		if (fluid.isFluidEqual(resource)) {
			long canAdd = CAPACITY - fluid.getAmount();
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
