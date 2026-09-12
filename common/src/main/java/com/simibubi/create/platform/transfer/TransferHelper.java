package com.simibubi.create.platform.transfer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.ServiceLoader;

public interface TransferHelper {
	TransferHelper INSTANCE = ServiceLoader.load(TransferHelper.class)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("No TransferHelper implementation found on classpath."));

	Optional<StorageProvider<ItemStack>> getItemStorage(Level level, BlockPos pos, @Nullable Direction side);

	Optional<StorageProvider<FluidStack>> getFluidStorage(Level level, BlockPos pos, @Nullable Direction side);

	static TransferHelper get() {
		return INSTANCE;
	}
}
