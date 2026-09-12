package com.simibubi.create.platform.registry;

import com.simibubi.create.Create;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;
import java.util.function.Supplier;

public class CreateRegistrate {

	public static BlockBehaviour.Properties blockProperties(String name) {
		return BlockBehaviour.Properties.of()
				.setId(ResourceKey.create(Registries.BLOCK, Create.asResource(name)));
	}

	public static Item.Properties itemProperties(String name) {
		return new Item.Properties()
				.setId(ResourceKey.create(Registries.ITEM, Create.asResource(name)));
	}

	public static <B extends Block> RegistryEntry<B> registerBlock(String name, Function<BlockBehaviour.Properties, B> blockFactory) {
		RegistryEntry<B> blockEntry = RegistryHelper.get().registerBlock(name, () -> blockFactory.apply(blockProperties(name)));
		RegistryHelper.get().registerItem(name, () -> new BlockItem(blockEntry.get(), itemProperties(name)));
		return blockEntry;
	}

	public static <B extends Block> RegistryEntry<B> registerBlock(String name, Supplier<B> blockSupplier) {
		RegistryEntry<B> blockEntry = RegistryHelper.get().registerBlock(name, blockSupplier);
		RegistryHelper.get().registerItem(name, () -> new BlockItem(blockEntry.get(), itemProperties(name)));
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
