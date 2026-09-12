package com.drinfonty.create_redux.platform.registry;

import com.drinfonty.create_redux.Create;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Supplier;

public class TestRegistryHelper implements RegistryHelper {

	public static void unfreezeRegistry(Registry<?> registry) {
		try {
			Field frozenField = MappedRegistry.class.getDeclaredField("frozen");
			frozenField.setAccessible(true);
			frozenField.set(registry, false);

			Field holdersField = MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
			holdersField.setAccessible(true);
			if (holdersField.get(registry) == null) {
				holdersField.set(registry, new IdentityHashMap<>());
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to unfreeze registry", e);
		}
	}

	@Override
	public <B extends Block> RegistryEntry<B> registerBlock(String name, Supplier<B> blockSupplier) {
		unfreezeRegistry(BuiltInRegistries.BLOCK);
		Identifier id = Create.asResource(name);
		B block = blockSupplier.get();
		Registry.register(BuiltInRegistries.BLOCK, id, block);
		return new TestEntry<>(id, block);
	}

	@Override
	public <I extends Item> RegistryEntry<I> registerItem(String name, Supplier<I> itemSupplier) {
		unfreezeRegistry(BuiltInRegistries.ITEM);
		Identifier id = Create.asResource(name);
		I item = itemSupplier.get();
		Registry.register(BuiltInRegistries.ITEM, id, item);
		return new TestEntry<>(id, item);
	}

	@Override
	public <T extends BlockEntity> RegistryEntry<BlockEntityType<T>> registerBlockEntityType(
			String name,
			BlockEntityType.BlockEntitySupplier<T> factory,
			Supplier<? extends Block[]> validBlocks
	) {
		unfreezeRegistry(BuiltInRegistries.BLOCK_ENTITY_TYPE);
		Identifier id = Create.asResource(name);
		Set<Block> blocks = new HashSet<>(Arrays.asList(validBlocks.get()));
		BlockEntityType<T> type = new BlockEntityType<>(factory, blocks);
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type);
		return new TestEntry<>(id, type);
	}

	@Override
	public RegistryEntry<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tabSupplier) {
		unfreezeRegistry(BuiltInRegistries.CREATIVE_MODE_TAB);
		Identifier id = Create.asResource(name);
		CreativeModeTab tab = tabSupplier.get();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id, tab);
		return new TestEntry<>(id, tab);
	}

	private static class TestEntry<T> implements RegistryEntry<T> {
		private final Identifier id;
		private final T value;

		public TestEntry(Identifier id, T value) {
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
