package com.drinfonty.create_redux.content.schematics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class SchematicPrinter {
	private final Schematic schematic;
	private int currentX = 0;
	private int currentY = 0;
	private int currentZ = 0;
	private boolean finished = false;
	private int placedBlocks = 0;
	private final int totalNonAirBlocks;

	public SchematicPrinter(Schematic schematic) {
		this.schematic = schematic;
		int nonAir = 0;
		for (BlockState state : schematic.getBlocks().values()) {
			if (!state.isAir()) nonAir++;
		}
		this.totalNonAirBlocks = Math.max(1, nonAir);
	}

	public Schematic getSchematic() {
		return schematic;
	}

	public BlockPos getCurrentTargetOffset() {
		return new BlockPos(currentX, currentY, currentZ);
	}

	public BlockState getCurrentState() {
		return schematic.getBlock(getCurrentTargetOffset());
	}

	public boolean isFinished() {
		return finished;
	}

	public float getProgress() {
		return Math.min(1.0f, (float) placedBlocks / totalNonAirBlocks);
	}

	public int getPlacedBlocks() {
		return placedBlocks;
	}

	public int getTotalNonAirBlocks() {
		return totalNonAirBlocks;
	}

	/**
	 * Advances the print cursor to the next non-air block to place.
	 * Returns true if a valid non-air block is found, false if printing has finished.
	 */
	public boolean advance() {
		if (finished) return false;

		while (currentY < schematic.getHeight()) {
			while (currentZ < schematic.getLength()) {
				while (currentX < schematic.getWidth()) {
					BlockState state = schematic.getBlock(new BlockPos(currentX, currentY, currentZ));
					if (!state.isAir()) {
						placedBlocks++;
						// Move cursor past this block for the next iteration
						stepNext();
						return true;
					}
					currentX++;
				}
				currentX = 0;
				currentZ++;
			}
			currentZ = 0;
			currentY++;
		}

		finished = true;
		return false;
	}

	private void stepNext() {
		currentX++;
		if (currentX >= schematic.getWidth()) {
			currentX = 0;
			currentZ++;
			if (currentZ >= schematic.getLength()) {
				currentZ = 0;
				currentY++;
				if (currentY >= schematic.getHeight()) {
					finished = true;
				}
			}
		}
	}

	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> materials = new HashMap<>();
		for (BlockState state : schematic.getBlocks().values()) {
			if (state.isAir()) continue;
			Item item = state.getBlock().asItem();
			if (item != Items.AIR) {
				materials.merge(item, 1, Integer::sum);
			}
		}
		return materials;
	}

	public void reset() {
		currentX = 0;
		currentY = 0;
		currentZ = 0;
		placedBlocks = 0;
		finished = false;
	}
}
