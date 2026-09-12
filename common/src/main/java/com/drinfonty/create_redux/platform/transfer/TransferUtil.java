package com.drinfonty.create_redux.platform.transfer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TransferUtil {
	public static Optional<StorageProvider<ItemStack>> getItemStorage(Level level, BlockPos pos, @Nullable Direction side) {
		return TransferHelper.get().getItemStorage(level, pos, side);
	}

	public static Optional<StorageProvider<FluidStack>> getFluidStorage(Level level, BlockPos pos, @Nullable Direction side) {
		return TransferHelper.get().getFluidStorage(level, pos, side);
	}

	public static ItemStack insertItem(StorageProvider<ItemStack> storage, ItemStack stack, boolean simulate) {
		if (stack.isEmpty()) return ItemStack.EMPTY;
		ItemStack remainder = stack.copy();
		for (int i = 0; i < storage.getSlots(); i++) {
			remainder = storage.insert(i, remainder, simulate);
			if (remainder.isEmpty()) {
				return ItemStack.EMPTY;
			}
		}
		return remainder;
	}

	public static ItemStack extractItem(StorageProvider<ItemStack> storage, int maxAmount, boolean simulate) {
		for (int i = 0; i < storage.getSlots(); i++) {
			ItemStack extracted = storage.extract(i, maxAmount, simulate);
			if (!extracted.isEmpty()) {
				return extracted;
			}
		}
		return ItemStack.EMPTY;
	}
}
