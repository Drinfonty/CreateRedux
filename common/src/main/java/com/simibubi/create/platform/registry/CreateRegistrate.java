package com.simibubi.create.platform.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;
import java.util.function.Supplier;

public class CreateRegistrate {
	public static <B extends Block> RegistryEntry<B> registerBlock(String name, Supplier<B> blockSupplier) {
		RegistryEntry<B> blockEntry = RegistryHelper.get().registerBlock(name, blockSupplier);
		RegistryHelper.get().registerItem(name, () -> new BlockItem(blockEntry.get(), new Item.Properties()));
		return blockEntry;
	}

	public static <B extends Block> RegistryEntry<B> registerBlock(String name, Supplier<B> blockSupplier, Function<B, Item> itemFactory) {
		RegistryEntry<B> blockEntry = RegistryHelper.get().registerBlock(name, blockSupplier);
		RegistryHelper.get().registerItem(name, () -> itemFactory.apply(blockEntry.get()));
		return blockEntry;
	}

	public static <I extends Item> RegistryEntry<I> registerItem(String name, Supplier<I> itemSupplier) {
		return RegistryHelper.get().registerItem(name, itemSupplier);
	}

	public static <T extends BlockEntity> RegistryEntry<BlockEntityType<T>> registerBlockEntityType(
			String name,
			BlockEntityType.BlockEntitySupplier<T> factory,
			Supplier<? extends Block[]> validBlocks
	) {
		return RegistryHelper.get().registerBlockEntityType(name, factory, validBlocks);
	}

	public static RegistryEntry<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tabSupplier) {
		return RegistryHelper.get().registerCreativeTab(name, tabSupplier);
	}
}
