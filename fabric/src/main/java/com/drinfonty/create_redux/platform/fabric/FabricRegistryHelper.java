package com.drinfonty.create_redux.platform.fabric;

import com.drinfonty.create_redux.Create;
import com.drinfonty.create_redux.platform.registry.RegistryEntry;
import com.drinfonty.create_redux.platform.registry.RegistryHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class FabricRegistryHelper implements RegistryHelper {
	@Override
	public <B extends Block> RegistryEntry<B> registerBlock(String name, Supplier<B> blockSupplier) {
		Identifier id = Create.asResource(name);
		B block = blockSupplier.get();
		Registry.register(BuiltInRegistries.BLOCK, id, block);
		return new SimpleRegistryEntry<>(id, block);
	}

	@Override
	public <I extends Item> RegistryEntry<I> registerItem(String name, Supplier<I> itemSupplier) {
		Identifier id = Create.asResource(name);
		I item = itemSupplier.get();
		Registry.register(BuiltInRegistries.ITEM, id, item);
		return new SimpleRegistryEntry<>(id, item);
	}

	@Override
	public <T extends BlockEntity> RegistryEntry<BlockEntityType<T>> registerBlockEntityType(
			String name,
			BlockEntityType.BlockEntitySupplier<T> factory,
			Supplier<? extends Block[]> validBlocks
	) {
		Identifier id = Create.asResource(name);
		Set<Block> blocks = new HashSet<>(Arrays.asList(validBlocks.get()));
		BlockEntityType<T> type = new BlockEntityType<>(factory, blocks);
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type);
		return new SimpleRegistryEntry<>(id, type);
	}

	@Override
	public RegistryEntry<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tabSupplier) {
		Identifier id = Create.asResource(name);
		CreativeModeTab tab = tabSupplier.get();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id, tab);
		return new SimpleRegistryEntry<>(id, tab);
	}

	private static class SimpleRegistryEntry<T> implements RegistryEntry<T> {
		private final Identifier id;
		private final T value;

		public SimpleRegistryEntry(Identifier id, T value) {
			this.id = id;
			this.value = value;
		}

		@Override
		public Identifier getId() {
			return id;
		}

		@Override
		public T get() {
			return value;
		}
	}
}
