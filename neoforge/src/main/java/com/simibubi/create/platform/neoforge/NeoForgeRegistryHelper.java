package com.simibubi.create.platform.neoforge;

import com.simibubi.create.Create;
import com.simibubi.create.platform.registry.RegistryEntry;
import com.simibubi.create.platform.registry.RegistryHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class NeoForgeRegistryHelper implements RegistryHelper {
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Create.ID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Create.ID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Create.ID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Create.ID);

	public static void init(IEventBus bus) {
		BLOCKS.register(bus);
		ITEMS.register(bus);
		BLOCK_ENTITIES.register(bus);
		CREATIVE_TABS.register(bus);
	}

	@Override
	public <B extends Block> RegistryEntry<B> registerBlock(String name, Supplier<B> blockSupplier) {
		DeferredHolder<Block, B> holder = BLOCKS.register(name, blockSupplier);
		return new HolderRegistryEntry<>(holder);
	}

	@Override
	public <I extends Item> RegistryEntry<I> registerItem(String name, Supplier<I> itemSupplier) {
		DeferredHolder<Item, I> holder = ITEMS.register(name, itemSupplier);
		return new HolderRegistryEntry<>(holder);
	}

	@Override
	public <T extends BlockEntity> RegistryEntry<BlockEntityType<T>> registerBlockEntityType(
			String name,
			BlockEntityType.BlockEntitySupplier<T> factory,
			Supplier<? extends Block[]> validBlocks
	) {
		DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> holder = BLOCK_ENTITIES.register(name, () -> {
			Set<Block> blocks = new HashSet<>(Arrays.asList(validBlocks.get()));
			return new BlockEntityType<>(factory, blocks);
		});
		return new HolderRegistryEntry<>(holder);
	}

	@Override
	public RegistryEntry<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tabSupplier) {
		DeferredHolder<CreativeModeTab, CreativeModeTab> holder = CREATIVE_TABS.register(name, tabSupplier);
		return new HolderRegistryEntry<>(holder);
	}

	private static class HolderRegistryEntry<R, T extends R> implements RegistryEntry<T> {
		private final DeferredHolder<R, T> holder;

		public HolderRegistryEntry(DeferredHolder<R, T> holder) {
			this.holder = holder;
		}

		@Override
		public Identifier getId() {
			return holder.getId();
		}

		@Override
		public T get() {
			return holder.get();
		}
	}
}
