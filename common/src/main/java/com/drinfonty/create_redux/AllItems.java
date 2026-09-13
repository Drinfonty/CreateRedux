package com.drinfonty.create_redux;

import com.drinfonty.create_redux.content.schematics.tools.*;
import com.drinfonty.create_redux.platform.registry.CreateRegistrate;
import com.drinfonty.create_redux.platform.registry.RegistryEntry;
import net.minecraft.world.item.Rarity;

public class AllItems {
	public static final RegistryEntry<EmptySchematicItem> EMPTY_SCHEMATIC = CreateRegistrate.registerItem(
			"empty_schematic",
			() -> new EmptySchematicItem(CreateRegistrate.itemProperties("empty_schematic"))
	);

	public static final RegistryEntry<SchematicAndQuillItem> SCHEMATIC_AND_QUILL = CreateRegistrate.registerItem(
			"schematic_and_quill",
			() -> new SchematicAndQuillItem(CreateRegistrate.itemProperties("schematic_and_quill").stacksTo(1))
	);

	public static final RegistryEntry<SchematicItem> SCHEMATIC = CreateRegistrate.registerItem(
			"schematic",
			() -> new SchematicItem(CreateRegistrate.itemProperties("schematic").stacksTo(1).rarity(Rarity.UNCOMMON))
	);

	public static final RegistryEntry<HandheldWorldshaperItem> HANDHELD_WORLDSHAPER = CreateRegistrate.registerItem(
			"handheld_worldshaper",
			() -> new HandheldWorldshaperItem(CreateRegistrate.itemProperties("handheld_worldshaper").stacksTo(1).rarity(Rarity.EPIC))
	);

	public static final RegistryEntry<SymmetryWandItem> SYMMETRY_WAND = CreateRegistrate.registerItem(
			"symmetry_wand",
			() -> new SymmetryWandItem(CreateRegistrate.itemProperties("symmetry_wand").stacksTo(1).rarity(Rarity.RARE))
	);

	public static void register() {
		// triggers static initialization
	}
}
