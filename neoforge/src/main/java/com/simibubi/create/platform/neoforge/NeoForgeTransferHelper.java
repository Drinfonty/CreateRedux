package com.simibubi.create.platform.neoforge;

import com.simibubi.create.platform.transfer.FluidStack;
import com.simibubi.create.platform.transfer.StorageProvider;
import com.simibubi.create.platform.transfer.TransferHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class NeoForgeTransferHelper implements TransferHelper {
	@Override
	public Optional<StorageProvider<ItemStack>> getItemStorage(Level level, BlockPos pos, @Nullable Direction side) {
		ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, side);
		if (handler == null) {
			return Optional.empty();
		}
		return Optional.of(new NeoForgeItemStorageProvider(handler));
	}

	@Override
	public Optional<StorageProvider<FluidStack>> getFluidStorage(Level level, BlockPos pos, @Nullable Direction side) {
		ResourceHandler<FluidResource> handler = level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
		if (handler == null) {
			return Optional.empty();
		}
		return Optional.of(new NeoForgeFluidStorageProvider(handler));
	}

	private static class NeoForgeItemStorageProvider implements StorageProvider<ItemStack> {
		private final ResourceHandler<ItemResource> handler;

		public NeoForgeItemStorageProvider(ResourceHandler<ItemResource> handler) {
			this.handler = handler;
		}

		@Override
		public int getSlots() {
			return handler.size();
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			ItemResource res = handler.getResource(slot);
			if (res.isEmpty()) return ItemStack.EMPTY;
			return res.toStack((int) Math.min(Integer.MAX_VALUE, handler.getAmountAsLong(slot)));
		}

		@Override
		public long getSlotCapacity(int slot) {
			ItemResource res = handler.getResource(slot);
			return handler.getCapacityAsLong(slot, res);
		}

		@Override
		public ItemStack insert(int slot, ItemStack stack, boolean simulate) {
			if (stack.isEmpty()) return ItemStack.EMPTY;
			ItemResource res = ItemResource.of(stack);
			try (Transaction tx = Transaction.openRoot()) {
				int inserted = handler.insert(slot, res, stack.getCount(), tx);
				if (!simulate) {
					tx.commit();
				}
				if (inserted >= stack.getCount()) {
					return ItemStack.EMPTY;
				}
				ItemStack remainder = stack.copy();
				remainder.shrink(inserted);
				return remainder;
			}
		}

		@Override
		public ItemStack extract(int slot, long maxAmount, boolean simulate) {
			ItemResource res = handler.getResource(slot);
			if (res.isEmpty()) return ItemStack.EMPTY;
			int toExtract = (int) Math.min(Integer.MAX_VALUE, maxAmount);
			try (Transaction tx = Transaction.openRoot()) {
				int extracted = handler.extract(slot, res, toExtract, tx);
				if (!simulate) {
					tx.commit();
				}
				if (extracted <= 0) return ItemStack.EMPTY;
				return res.toStack(extracted);
			}
		}
	}

	private static class NeoForgeFluidStorageProvider implements StorageProvider<FluidStack> {
		private final ResourceHandler<FluidResource> handler;

		public NeoForgeFluidStorageProvider(ResourceHandler<FluidResource> handler) {
			this.handler = handler;
		}

		@Override
		public int getSlots() {
			return handler.size();
		}

		@Override
		public FluidStack getStackInSlot(int slot) {
			FluidResource res = handler.getResource(slot);
			if (res.isEmpty()) return FluidStack.EMPTY;
			long amount = handler.getAmountAsLong(slot);
			return new FluidStack(res.getFluid(), amount, res.getComponents());
		}

		@Override
		public long getSlotCapacity(int slot) {
			FluidResource res = handler.getResource(slot);
			return handler.getCapacityAsLong(slot, res);
		}

		@Override
		public FluidStack insert(int slot, FluidStack resource, boolean simulate) {
			if (resource.isEmpty()) return FluidStack.EMPTY;
			FluidResource res = FluidResource.of(resource.getFluid(), resource.getComponentsPatch());
			int amount = (int) Math.min(Integer.MAX_VALUE, resource.getAmount());
			try (Transaction tx = Transaction.openRoot()) {
				int inserted = handler.insert(slot, res, amount, tx);
				if (!simulate) {
					tx.commit();
				}
				long remainder = resource.getAmount() - inserted;
				return resource.copyWithAmount(remainder);
			}
		}

		@Override
		public FluidStack extract(int slot, long maxAmount, boolean simulate) {
			FluidResource res = handler.getResource(slot);
			if (res.isEmpty()) return FluidStack.EMPTY;
			int toExtract = (int) Math.min(Integer.MAX_VALUE, maxAmount);
			try (Transaction tx = Transaction.openRoot()) {
				int extracted = handler.extract(slot, res, toExtract, tx);
				if (!simulate) {
					tx.commit();
				}
				if (extracted <= 0) return FluidStack.EMPTY;
				return new FluidStack(res.getFluid(), extracted, res.getComponents());
			}
		}
	}
}
