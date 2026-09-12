package com.simibubi.create.platform.fabric;

import com.simibubi.create.platform.transfer.FluidStack;
import com.simibubi.create.platform.transfer.StorageProvider;
import com.simibubi.create.platform.transfer.TransferHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FabricTransferHelper implements TransferHelper {
	@Override
	public Optional<StorageProvider<ItemStack>> getItemStorage(Level level, BlockPos pos, @Nullable Direction side) {
		Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, side);
		if (storage == null) {
			return Optional.empty();
		}
		return Optional.of(new FabricItemStorageProvider(storage));
	}

	@Override
	public Optional<StorageProvider<FluidStack>> getFluidStorage(Level level, BlockPos pos, @Nullable Direction side) {
		Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, side);
		if (storage == null) {
			return Optional.empty();
		}
		return Optional.of(new FabricFluidStorageProvider(storage));
	}

	private static class FabricItemStorageProvider implements StorageProvider<ItemStack> {
		private final Storage<ItemVariant> storage;

		public FabricItemStorageProvider(Storage<ItemVariant> storage) {
			this.storage = storage;
		}

		@Override
		public int getSlots() {
			int count = 0;
			for (StorageView<ItemVariant> ignored : storage) {
				count++;
			}
			return count;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			int idx = 0;
			for (StorageView<ItemVariant> view : storage) {
				if (idx == slot) {
					return view.getResource().toStack((int) view.getAmount());
				}
				idx++;
			}
			return ItemStack.EMPTY;
		}

		@Override
		public long getSlotCapacity(int slot) {
			int idx = 0;
			for (StorageView<ItemVariant> view : storage) {
				if (idx == slot) {
					return view.getCapacity();
				}
				idx++;
			}
			return 0;
		}

		@Override
		public ItemStack insert(int slot, ItemStack stack, boolean simulate) {
			if (stack.isEmpty()) return ItemStack.EMPTY;
			try (Transaction tx = Transaction.openOuter()) {
				ItemVariant variant = ItemVariant.of(stack);
				long inserted = storage.insert(variant, stack.getCount(), tx);
				if (!simulate) {
					tx.commit();
				}
				if (inserted >= stack.getCount()) {
					return ItemStack.EMPTY;
				}
				ItemStack remainder = stack.copy();
				remainder.shrink((int) inserted);
				return remainder;
			}
		}

		@Override
		public ItemStack extract(int slot, long maxAmount, boolean simulate) {
			try (Transaction tx = Transaction.openOuter()) {
				int idx = 0;
				for (StorageView<ItemVariant> view : storage) {
					if (idx == slot && !view.isResourceBlank()) {
						ItemVariant variant = view.getResource();
						long extracted = view.extract(variant, maxAmount, tx);
						if (!simulate) {
							tx.commit();
						}
						return variant.toStack((int) extracted);
					}
					idx++;
				}
			}
			return ItemStack.EMPTY;
		}
	}

	private static class FabricFluidStorageProvider implements StorageProvider<FluidStack> {
		private final Storage<FluidVariant> storage;

		public FabricFluidStorageProvider(Storage<FluidVariant> storage) {
			this.storage = storage;
		}

		@Override
		public int getSlots() {
			int count = 0;
			for (StorageView<FluidVariant> ignored : storage) {
				count++;
			}
			return count;
		}

		@Override
		public FluidStack getStackInSlot(int slot) {
			int idx = 0;
			for (StorageView<FluidVariant> view : storage) {
				if (idx == slot) {
					long mb = view.getAmount() / 81; // convert 81000 droplets to 1000 mB
					return new FluidStack(view.getResource().getFluid(), mb, view.getResource().getComponents());
				}
				idx++;
			}
			return FluidStack.EMPTY;
		}

		@Override
		public long getSlotCapacity(int slot) {
			int idx = 0;
			for (StorageView<FluidVariant> view : storage) {
				if (idx == slot) {
					return view.getCapacity() / 81;
				}
				idx++;
			}
			return 0;
		}

		@Override
		public FluidStack insert(int slot, FluidStack resource, boolean simulate) {
			if (resource.isEmpty()) return FluidStack.EMPTY;
			try (Transaction tx = Transaction.openOuter()) {
				FluidVariant variant = FluidVariant.of(resource.getFluid(), resource.getComponentsPatch());
				long droplets = resource.getAmount() * 81;
				long inserted = storage.insert(variant, droplets, tx);
				if (!simulate) {
					tx.commit();
				}
				long remainderMb = resource.getAmount() - (inserted / 81);
				return resource.copyWithAmount(remainderMb);
			}
		}

		@Override
		public FluidStack extract(int slot, long maxAmount, boolean simulate) {
			try (Transaction tx = Transaction.openOuter()) {
				int idx = 0;
				for (StorageView<FluidVariant> view : storage) {
					if (idx == slot && !view.isResourceBlank()) {
						FluidVariant variant = view.getResource();
						long dropletsToExtract = maxAmount * 81;
						long extracted = view.extract(variant, dropletsToExtract, tx);
						if (!simulate) {
							tx.commit();
						}
						return new FluidStack(variant.getFluid(), extracted / 81, variant.getComponents());
					}
					idx++;
				}
			}
			return FluidStack.EMPTY;
		}
	}
}
