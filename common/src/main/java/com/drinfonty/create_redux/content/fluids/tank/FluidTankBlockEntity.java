package com.drinfonty.create_redux.content.fluids.tank;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import com.drinfonty.create_redux.platform.transfer.FluidStack;
import com.drinfonty.create_redux.platform.transfer.StorageProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FluidTankBlockEntity extends BlockEntity implements StorageProvider<FluidStack> {
	private FluidStack fluid = FluidStack.EMPTY;
	private long capacity = 8000; // 8,000 mB default (8 buckets)

	public FluidTankBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.FLUID_TANK.get(), pos, state);
	}

	public FluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public FluidStack getFluid() {
		return fluid;
	}

	public void setFluid(FluidStack fluid) {
		this.fluid = fluid == null ? FluidStack.EMPTY : fluid;
		setChanged();
		notifyUpdate();
	}

	public long getCapacity() {
		return capacity;
	}

	public void setCapacity(long capacity) {
		this.capacity = Math.max(1000, capacity);
		setChanged();
	}

	public void notifyUpdate() {
		if (level != null && !level.isClientSide()) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2 | 16);
		}
	}

	public boolean handleBucket(Player player, InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);
		if (held.isEmpty()) return false;

		// Fill tank from water bucket
		if (held.is(Items.WATER_BUCKET)) {
			if (fluid.isEmpty() || (fluid.getFluid() == Fluids.WATER && fluid.getAmount() + 1000 <= capacity)) {
				long newAmount = fluid.isEmpty() ? 1000 : fluid.getAmount() + 1000;
				setFluid(new FluidStack(Fluids.WATER, newAmount));
				if (!player.isCreative()) {
					player.setItemInHand(hand, new ItemStack(Items.BUCKET));
				}
				return true;
			}
		}

		// Fill tank from lava bucket
		if (held.is(Items.LAVA_BUCKET)) {
			if (fluid.isEmpty() || (fluid.getFluid() == Fluids.LAVA && fluid.getAmount() + 1000 <= capacity)) {
				long newAmount = fluid.isEmpty() ? 1000 : fluid.getAmount() + 1000;
				setFluid(new FluidStack(Fluids.LAVA, newAmount));
				if (!player.isCreative()) {
					player.setItemInHand(hand, new ItemStack(Items.BUCKET));
				}
				return true;
			}
		}

		// Empty tank into bucket
		if (held.is(Items.BUCKET) && !fluid.isEmpty() && fluid.getAmount() >= 1000) {
			ItemStack filledBucket = ItemStack.EMPTY;
			if (fluid.getFluid() == Fluids.WATER) {
				filledBucket = new ItemStack(Items.WATER_BUCKET);
			} else if (fluid.getFluid() == Fluids.LAVA) {
				filledBucket = new ItemStack(Items.LAVA_BUCKET);
			}

			if (!filledBucket.isEmpty()) {
				long remaining = fluid.getAmount() - 1000;
				setFluid(fluid.copyWithAmount(remaining));
				if (!player.isCreative()) {
					held.shrink(1);
					if (held.isEmpty()) {
						player.setItemInHand(hand, filledBucket);
					} else if (!player.getInventory().add(filledBucket)) {
						player.drop(filledBucket, false);
					}
				}
				return true;
			}
		}

		return false;
	}

	// --- StorageProvider<FluidStack> Implementation ---
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
		return capacity;
	}

	@Override
	public FluidStack insert(int slot, FluidStack resource, boolean simulate) {
		if (resource.isEmpty() || slot != 0) return resource;

		if (fluid.isEmpty()) {
			long toInsert = Math.min(resource.getAmount(), capacity);
			if (!simulate) {
				setFluid(resource.copyWithAmount(toInsert));
			}
			long rem = resource.getAmount() - toInsert;
			return resource.copyWithAmount(rem);
		}

		if (fluid.isFluidEqual(resource)) {
			long canAdd = capacity - fluid.getAmount();
			if (canAdd <= 0) return resource;

			long toAdd = Math.min(canAdd, resource.getAmount());
			if (!simulate) {
				setFluid(fluid.copyWithAmount(fluid.getAmount() + toAdd));
			}
			long rem = resource.getAmount() - toAdd;
			return resource.copyWithAmount(rem);
		}

		return resource;
	}

	@Override
	public FluidStack extract(int slot, long maxAmount, boolean simulate) {
		if (fluid.isEmpty() || slot != 0 || maxAmount <= 0) return FluidStack.EMPTY;

		long toExtract = Math.min(fluid.getAmount(), maxAmount);
		FluidStack extracted = fluid.copyWithAmount(toExtract);
		if (!simulate) {
			long remaining = fluid.getAmount() - toExtract;
			setFluid(fluid.copyWithAmount(remaining));
		}
		return extracted;
	}

	// --- Serialization ---
	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putLong("Capacity", capacity);
		if (!fluid.isEmpty()) {
			output.putString("FluidId", BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
			output.putLong("FluidAmount", fluid.getAmount());
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		capacity = input.getLongOr("Capacity", 8000L);
		String fluidId = input.getStringOr("FluidId", "");
		long amount = input.getLongOr("FluidAmount", 0L);
		if (!fluidId.isEmpty() && amount > 0) {
			BuiltInRegistries.FLUID.get(Identifier.parse(fluidId))
					.map(Holder.Reference::value)
					.filter(f -> f != Fluids.EMPTY)
					.ifPresent(f -> this.fluid = new FluidStack(f, amount));
		}
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		tag.putLong("Capacity", capacity);
		if (!fluid.isEmpty()) {
			tag.putString("FluidId", BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
			tag.putLong("FluidAmount", fluid.getAmount());
		}
		return tag;
	}
}
