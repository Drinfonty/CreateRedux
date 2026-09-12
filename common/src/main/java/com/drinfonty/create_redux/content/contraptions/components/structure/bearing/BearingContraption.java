package com.drinfonty.create_redux.content.contraptions.components.structure.bearing;

import com.drinfonty.create_redux.content.contraptions.components.structure.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public class BearingContraption extends Contraption {
	protected Direction facing = Direction.UP;
	protected int sailBlocksCount = 0;

	public BearingContraption() {
	}

	public BearingContraption(Direction facing) {
		this.facing = facing;
	}

	public Direction getFacing() {
		return facing;
	}

	public int getSailBlocksCount() {
		return sailBlocksCount;
	}

	@Override
	public boolean assemble(Level level, BlockPos startPos, int maxBlocks) {
		boolean success = super.assemble(level, startPos, maxBlocks);
		if (success) {
			countSailBlocks();
		}
		return success;
	}

	private void countSailBlocks() {
		sailBlocksCount = 0;
		for (var state : blocks.values()) {
			String path = state.getBlock().getDescriptionId();
			if (path.contains("sail") || path.contains("wool")) {
				sailBlocksCount++;
			}
		}
	}
}
