package com.drinfonty.create_redux.content.trains.entity;

import com.drinfonty.create_redux.content.contraptions.components.structure.Contraption;
import com.drinfonty.create_redux.content.trains.bogey.BogeyBlock;
import com.drinfonty.create_redux.content.trains.track.TrackBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class CarriageContraption extends Contraption {
	private final List<BlockPos> bogeyLocalPositions = new ArrayList<>();

	public List<BlockPos> getBogeyLocalPositions() {
		return Collections.unmodifiableList(bogeyLocalPositions);
	}

	public boolean assembleCarriage(Level level, List<BlockPos> bogeyPositions, int maxBlocks) {
		if (bogeyPositions.isEmpty()) return false;

		this.anchor = bogeyPositions.get(0);
		this.blocks.clear();
		this.blockEntities.clear();
		this.bogeyLocalPositions.clear();

		Queue<BlockPos> queue = new ArrayDeque<>();
		Set<BlockPos> visited = new HashSet<>();

		for (BlockPos bogeyPos : bogeyPositions) {
			bogeyLocalPositions.add(bogeyPos.subtract(anchor));
			queue.add(bogeyPos);
			visited.add(bogeyPos);
		}

		int minX = 0, minY = 0, minZ = 0;
		int maxX = 0, maxY = 0, maxZ = 0;

		while (!queue.isEmpty() && blocks.size() < maxBlocks) {
			BlockPos current = queue.poll();
			BlockState state = level.getBlockState(current);

			if (state.isAir() || state.is(Blocks.BEDROCK) || state.getBlock() instanceof TrackBlock) continue;

			BlockPos relPos = current.subtract(anchor);
			blocks.put(relPos, state);

			minX = Math.min(minX, relPos.getX());
			minY = Math.min(minY, relPos.getY());
			minZ = Math.min(minZ, relPos.getZ());
			maxX = Math.max(maxX, relPos.getX() + 1);
			maxY = Math.max(maxY, relPos.getY() + 1);
			maxZ = Math.max(maxZ, relPos.getZ() + 1);

			BlockEntity be = level.getBlockEntity(current);
			if (be != null) {
				blockEntities.put(relPos, be.saveCustomOnly(level.registryAccess()));
			}

			// Traverse 6 adjacent faces
			for (Direction dir : Direction.values()) {
				BlockPos next = current.relative(dir);
				if (!visited.contains(next)) {
					visited.add(next);
					BlockState nextState = level.getBlockState(next);
					// Do not traverse into track blocks beneath bogeys
					if (!nextState.isAir() && !nextState.is(Blocks.BEDROCK) && !(nextState.getBlock() instanceof TrackBlock)) {
						queue.add(next);
					}
				}
			}
		}

		this.bounds = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
		this.assembled = !blocks.isEmpty();
		return this.assembled;
	}
}
