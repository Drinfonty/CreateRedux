package com.drinfonty.create_redux.content.logistics;

import com.drinfonty.create_redux.AllBlocks;
import com.drinfonty.create_redux.content.kinetics.belt.BeltBlock;
import com.drinfonty.create_redux.content.kinetics.belt.BeltBlockEntity;
import com.drinfonty.create_redux.content.logistics.chute.ChuteBlockEntity;
import com.drinfonty.create_redux.content.logistics.depot.DepotBlockEntity;
import com.drinfonty.create_redux.content.logistics.funnel.FunnelBlockEntity;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LogisticsTest {

	@BeforeAll
	static void setup() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		try {
			java.lang.reflect.Method bindMethod = null;
			for (java.lang.reflect.Method m : net.minecraft.core.Holder.Reference.class.getMethods()) {
				if (m.getName().equals("bindComponents")) {
					bindMethod = m;
					break;
				}
			}
			if (bindMethod != null) {
				final java.lang.reflect.Method method = bindMethod;
				net.minecraft.core.component.DataComponentMap defaultComponents =
						net.minecraft.core.component.DataComponentMap.builder()
								.set(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE, 64)
								.build();
				net.minecraft.core.registries.BuiltInRegistries.ITEM.stream().forEach(item -> {
					try {
						method.invoke(item.builtInRegistryHolder(), defaultComponents);
					} catch (Throwable ignored) {}
				});
			}
		} catch (Throwable ignored) {}
	}

	@Test
	@DisplayName("DepotBlockEntity StorageProvider insertion and extraction")
	void testDepotStorage() {
		DepotBlockEntity depot = new DepotBlockEntity(BlockPos.ZERO, AllBlocks.DEPOT.get().defaultBlockState());
		assertTrue(depot.isEmpty());
		assertEquals(1, depot.getSlots());

		// Test simulated insertion
		ItemStack stack = new ItemStack(Items.IRON_INGOT, 16);
		ItemStack remainder = depot.insert(0, stack, true);
		assertTrue(remainder.isEmpty());
		assertTrue(depot.isEmpty(), "Simulate should not modify depot contents");

		// Test actual insertion
		remainder = depot.insert(0, stack, false);
		assertTrue(remainder.isEmpty());
		assertFalse(depot.isEmpty());
		assertEquals(16, depot.getItem(0).getCount());
		assertEquals(Items.IRON_INGOT, depot.getItem(0).getItem());

		// Test merge insertion
		ItemStack extra = new ItemStack(Items.IRON_INGOT, 10);
		remainder = depot.insert(0, extra, false);
		assertTrue(remainder.isEmpty());
		assertEquals(26, depot.getItem(0).getCount());

		// Test extraction
		ItemStack extracted = depot.extract(0, 6, false);
		assertEquals(6, extracted.getCount());
		assertEquals(Items.IRON_INGOT, extracted.getItem());
		assertEquals(20, depot.getItem(0).getCount());
	}

	@Test
	@DisplayName("BeltBlock rotational axis alignment and item management")
	void testBeltBlock() {
		BlockState northState = AllBlocks.BELT.get().defaultBlockState()
				.setValue(BeltBlock.HORIZONTAL_FACING, Direction.NORTH);
		assertEquals(Direction.Axis.X, AllBlocks.BELT.get().getRotationAxis(northState));

		BlockState eastState = AllBlocks.BELT.get().defaultBlockState()
				.setValue(BeltBlock.HORIZONTAL_FACING, Direction.EAST);
		assertEquals(Direction.Axis.Z, AllBlocks.BELT.get().getRotationAxis(eastState));

		BeltBlockEntity belt = new BeltBlockEntity(BlockPos.ZERO, northState);
		assertTrue(belt.canAcceptItem(Direction.UP));

		// Add item onto belt
		boolean added = belt.addItem(new ItemStack(Items.COPPER_INGOT, 5), Direction.UP);
		assertTrue(added);
		assertEquals(1, belt.getItems().size());
		assertEquals(5, belt.getItems().get(0).stack.getCount());
	}

	@Test
	@DisplayName("Chute directional faces and container logic")
	void testChuteBlockEntity() {
		ChuteBlockEntity chute = new ChuteBlockEntity(BlockPos.ZERO, AllBlocks.CHUTE.get().defaultBlockState());
		assertTrue(chute.isEmpty());

		// Place through top
		ItemStack item = new ItemStack(Items.GOLD_INGOT, 8);
		assertTrue(chute.canPlaceItemThroughFace(0, item, Direction.UP));
		assertFalse(chute.canPlaceItemThroughFace(0, item, Direction.DOWN));

		chute.setItem(0, item);
		assertFalse(chute.isEmpty());
		assertEquals(8, chute.getItem(0).getCount());

		// Extract through bottom
		assertTrue(chute.canTakeItemThroughFace(0, item, Direction.DOWN));
		assertFalse(chute.canTakeItemThroughFace(0, item, Direction.UP));
	}

	@Test
	@DisplayName("Funnel filter and extraction limit configuration")
	void testFunnelBlockEntity() {
		FunnelBlockEntity funnel = new FunnelBlockEntity(BlockPos.ZERO, AllBlocks.BRASS_FUNNEL.get().defaultBlockState());
		assertEquals(64, funnel.getExtractionLimit());

		// Configure filter
		funnel.setFilter(new ItemStack(Items.DIAMOND));
		assertEquals(Items.DIAMOND, funnel.getFilter().getItem());

		// Change extraction limit
		funnel.setExtractionLimit(32);
		assertEquals(32, funnel.getExtractionLimit());
	}
}
