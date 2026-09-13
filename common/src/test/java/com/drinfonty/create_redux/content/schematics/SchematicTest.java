package com.drinfonty.create_redux.content.schematics;

import com.drinfonty.create_redux.AllBlocks;
import com.drinfonty.create_redux.content.schematics.cannon.SchematicannonBlockEntity;
import com.drinfonty.create_redux.content.schematics.tools.HandheldWorldshaperItem;
import com.drinfonty.create_redux.content.schematics.tools.SymmetryWandItem;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SchematicTest {

	@BeforeAll
	static void setup() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	@DisplayName("Schematic NBT serialization and deserialization roundtrip")
	void testSchematicSerialization() {
		Schematic schematic = new Schematic("test_machine", 4, 3, 5);
		schematic.setAnchor(new BlockPos(10, 64, 20));

		BlockState stone = Blocks.STONE.defaultBlockState();
		BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
		BlockState iron = Blocks.IRON_BLOCK.defaultBlockState();

		schematic.setBlock(new BlockPos(0, 0, 0), stone);
		schematic.setBlock(new BlockPos(1, 1, 1), planks);
		schematic.setBlock(new BlockPos(2, 2, 2), iron);

		CompoundTag beData = new CompoundTag();
		beData.putString("CustomField", "SpecialValue123");
		schematic.setBlockEntity(new BlockPos(1, 1, 1), beData);

		// Write to NBT
		CompoundTag nbt = schematic.writeToTag(null);
		assertNotNull(nbt);
		assertEquals("test_machine", com.drinfonty.create_redux.platform.NbtCompat.getString(nbt, "Name", ""));
		assertEquals(4, com.drinfonty.create_redux.platform.NbtCompat.getInt(nbt, "Width", 0));

		// Read back
		Schematic loaded = new Schematic();
		loaded.readFromTag(nbt, null);

		assertEquals("test_machine", loaded.getName());
		assertEquals(4, loaded.getWidth());
		assertEquals(3, loaded.getHeight());
		assertEquals(5, loaded.getLength());
		assertEquals(new BlockPos(10, 64, 20), loaded.getAnchor());

		assertEquals(stone, loaded.getBlock(new BlockPos(0, 0, 0)));
		assertEquals(planks, loaded.getBlock(new BlockPos(1, 1, 1)));
		assertEquals(iron, loaded.getBlock(new BlockPos(2, 2, 2)));

		CompoundTag loadedBe = loaded.getBlockEntity(new BlockPos(1, 1, 1));
		assertNotNull(loadedBe);
		assertEquals("SpecialValue123", com.drinfonty.create_redux.platform.NbtCompat.getString(loadedBe, "CustomField", ""));
	}

	@Test
	@DisplayName("SchematicPrinter step-by-step traversal and material requirements")
	void testSchematicPrinter() {
		Schematic schematic = new Schematic("wall", 2, 2, 1);
		schematic.setBlock(new BlockPos(0, 0, 0), Blocks.STONE.defaultBlockState());
		schematic.setBlock(new BlockPos(1, 0, 0), Blocks.STONE.defaultBlockState());
		schematic.setBlock(new BlockPos(0, 1, 0), Blocks.OAK_PLANKS.defaultBlockState());
		schematic.setBlock(new BlockPos(1, 1, 0), Blocks.OAK_PLANKS.defaultBlockState());

		SchematicPrinter printer = new SchematicPrinter(schematic);
		assertEquals(4, printer.getTotalNonAirBlocks());
		assertEquals(0.0f, printer.getProgress());
		assertFalse(printer.isFinished());

		// Check materials
		Map<Item, Integer> materials = printer.getRequiredMaterials();
		assertEquals(2, materials.get(Items.STONE));
		assertEquals(2, materials.get(Items.OAK_PLANKS));

		// Step 1
		assertTrue(printer.advance());
		assertEquals(1, printer.getPlacedBlocks());
		assertEquals(0.25f, printer.getProgress(), 1e-4);

		// Step 2, 3, 4
		assertTrue(printer.advance());
		assertTrue(printer.advance());
		assertTrue(printer.advance());
		assertEquals(4, printer.getPlacedBlocks());
		assertEquals(1.0f, printer.getProgress(), 1e-4);

		// Finished
		assertFalse(printer.advance());
		assertTrue(printer.isFinished());
	}

	@Test
	@DisplayName("Schematicannon gunpowder consumption and autonomous aiming")
	void testSchematicannonGunpowderAndAiming() {
		SchematicannonBlockEntity cannon = new SchematicannonBlockEntity(BlockPos.ZERO, AllBlocks.SCHEMATICANNON.get().defaultBlockState());
		assertEquals(SchematicannonBlockEntity.State.IDLE, cannon.getState());
		assertEquals(0.0f, cannon.getGunpowderFuel());

		// Setup small schematic with 2 blocks
		Schematic schematic = new Schematic("target", 3, 2, 3);
		schematic.setBlock(new BlockPos(2, 1, 2), Blocks.STONE.defaultBlockState());
		SchematicPrinter printer = new SchematicPrinter(schematic);

		cannon.setPrinter(printer, new BlockPos(10, 0, 10));
		assertEquals(SchematicannonBlockEntity.State.RUNNING, cannon.getState());

		// Attempt tick without gunpowder -> transitions to MISSING_GUNPOWDER
		cannon.tick(null);
		assertEquals(SchematicannonBlockEntity.State.MISSING_GUNPOWDER, cannon.getState());

		// Add gunpowder -> automatically resumes RUNNING
		cannon.addGunpowder(1.0f);
		assertEquals(SchematicannonBlockEntity.State.RUNNING, cannon.getState());
		assertEquals(1.0f, cannon.getGunpowderFuel());

		// Tick once -> places block, consumes fuel, calculates aiming yaw & pitch
		cannon.tick(null);
		assertEquals(0.95f, cannon.getGunpowderFuel(), 1e-4);

		// Cannon aimed towards target anchor offset (10+2=12, 0+1=1, 10+2=12) from cannon pos (0, 0, 0)
		assertNotEquals(0.0f, cannon.getCannonYaw());
		assertNotEquals(0.0f, cannon.getCannonPitch());

		// Next tick completes print
		cannon.tick(null);
		assertEquals(SchematicannonBlockEntity.State.FINISHED, cannon.getState());
	}

	@Test
	@DisplayName("HandheldWorldshaper brush volume calculations")
	void testWorldshaperBrushes() {
		BlockPos center = new BlockPos(0, 64, 0);

		// Cuboid radius 1 -> (2*1 + 1)^3 = 27 blocks
		Set<BlockPos> cuboid = HandheldWorldshaperItem.calculateAffectedBlocks(center, HandheldWorldshaperItem.BrushMode.CUBOID, 1);
		assertEquals(27, cuboid.size());
		assertTrue(cuboid.contains(center));
		assertTrue(cuboid.contains(new BlockPos(1, 65, 1)));
		assertTrue(cuboid.contains(new BlockPos(-1, 63, -1)));

		// Sphere radius 2 -> all points with dx^2 + dy^2 + dz^2 <= 4
		Set<BlockPos> sphere = HandheldWorldshaperItem.calculateAffectedBlocks(center, HandheldWorldshaperItem.BrushMode.SPHERE, 2);
		assertTrue(sphere.size() > 0);
		assertTrue(sphere.contains(center));
		assertTrue(sphere.contains(center.above(2)));
		assertFalse(sphere.contains(center.offset(2, 2, 2)), "Corner outside sphere");

		// Cylinder radius 2
		Set<BlockPos> cylinder = HandheldWorldshaperItem.calculateAffectedBlocks(center, HandheldWorldshaperItem.BrushMode.CYLINDER, 2);
		assertTrue(cylinder.size() > 0);
		assertTrue(cylinder.contains(center));
	}

	@Test
	@DisplayName("SymmetryWand mirror reflections across plane, cross, and triple modes")
	void testSymmetryReflections() {
		BlockPos origin = new BlockPos(10, 64, 10);
		BlockPos placed = new BlockPos(12, 65, 13); // dx=2, dy=1, dz=3

		// Plane mode -> 2 positions
		List<BlockPos> planeSym = SymmetryWandItem.calculateSymmetricPositions(placed, origin, SymmetryWandItem.SymmetryMode.PLANE);
		assertEquals(2, planeSym.size());
		assertTrue(planeSym.contains(placed));
		assertTrue(planeSym.contains(new BlockPos(8, 65, 13))); // dx=-2, dy=1, dz=3

		// Cross mode -> 4 positions
		List<BlockPos> crossSym = SymmetryWandItem.calculateSymmetricPositions(placed, origin, SymmetryWandItem.SymmetryMode.CROSS);
		assertEquals(4, crossSym.size());
		assertTrue(crossSym.contains(placed));
		assertTrue(crossSym.contains(new BlockPos(8, 65, 13)));
		assertTrue(crossSym.contains(new BlockPos(12, 65, 7))); // dx=2, dz=-3
		assertTrue(crossSym.contains(new BlockPos(8, 65, 7)));  // dx=-2, dz=-3

		// Triple mode -> 8 positions (all 8 octants)
		List<BlockPos> tripleSym = SymmetryWandItem.calculateSymmetricPositions(placed, origin, SymmetryWandItem.SymmetryMode.TRIPLE);
		assertEquals(8, tripleSym.size());
		assertTrue(tripleSym.contains(placed));
		assertTrue(tripleSym.contains(new BlockPos(8, 63, 7))); // dx=-2, dy=-1, dz=-3
	}
}
