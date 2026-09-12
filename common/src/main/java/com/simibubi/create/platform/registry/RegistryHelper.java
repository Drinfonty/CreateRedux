package com.simibubi.create.platform.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ServiceLoader;
import java.util.function.Supplier;

public interface RegistryHelper {
	RegistryHelper INSTANCE = ServiceLoader.load(RegistryHelper.class)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("No RegistryHelper service found on classpath."));

	<B extends Block> RegistryEntry<B> registerBlock(String name, Supplier<B> blockSupplier);

	<I extends Item> RegistryEntry<I> registerItem(String name, Supplier<I> itemSupplier);

	<T extends BlockEntity> RegistryEntry<BlockEntityType<T>> registerBlockEntityType(
			String name,
			BlockEntityType.BlockEntitySupplier<T> factory,
			Supplier<? extends Block[]> validBlocks
	);

	RegistryEntry<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tabSupplier);

	static RegistryHelper get() {
		return INSTANCE;
	}
}
