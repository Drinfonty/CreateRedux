package com.simibubi.create.content.contraptions.components.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class Contraption {
	protected final Map<BlockPos, BlockState> blocks = new HashMap<>();
	protected final Map<BlockPos, CompoundTag> blockEntities = new HashMap<>();
	protected BlockPos anchor = BlockPos.ZERO;
	protected AABB bounds = new AABB(0, 0, 0, 1, 1, 1);
	protected boolean assembled = false;

	public Map<BlockPos, BlockState> getBlocks() {
		return Collections.unmodifiableMap(blocks);
	}

	public Map<BlockPos, CompoundTag> getBlockEntities() {
		return Collections.unmodifiableMap(blockEntities);
	}

	public BlockPos getAnchor() {
		return anchor;
	}

	public AABB getBounds() {
		return bounds;
	}

	public boolean isAssembled() {
		return assembled;
	}

	public boolean assemble(Level level, BlockPos startPos, int maxBlocks) {
		this.anchor = startPos;
		this.blocks.clear();
		this.blockEntities.clear();

		Queue<BlockPos> queue = new ArrayDeque<>();
		Set<BlockPos> visited = new HashSet<>();

		queue.add(startPos);
		visited.add(startPos);

		int minX = 0, minY = 0, minZ = 0;
		int maxX = 0, maxY = 0, maxZ = 0;

		while (!queue.isEmpty() && blocks.size() < maxBlocks) {
			BlockPos current = queue.poll();
			BlockState state = level.getBlockState(current);

			if (state.isAir() || state.is(Blocks.BEDROCK)) continue;

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
					if (!nextState.isAir() && !nextState.is(Blocks.BEDROCK)) {
						queue.add(next);
					}
				}
			}
		}

		this.bounds = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
		this.assembled = !blocks.isEmpty();
		return this.assembled;
	}

	public void removeBlocksFromWorld(Level level) {
		for (BlockPos relPos : blocks.keySet()) {
			BlockPos worldPos = anchor.offset(relPos);
			level.removeBlockEntity(worldPos);
			level.setBlock(worldPos, Blocks.AIR.defaultBlockState(), 2 | 16);
		}
	}

	public void disassembleIntoWorld(Level level, BlockPos newAnchor) {
		for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
			BlockPos worldPos = newAnchor.offset(entry.getKey());
			level.setBlock(worldPos, entry.getValue(), 2 | 16);
			CompoundTag beTag = blockEntities.get(entry.getKey());
			if (beTag != null) {
				BlockEntity be = level.getBlockEntity(worldPos);
				if (be != null) {
					be.loadCustomOnly(net.minecraft.world.level.storage.TagValueInput.create(
							net.minecraft.util.ProblemReporter.DISCARDING,
							level.registryAccess(),
							beTag
					));
				}
			}
		}
		this.assembled = false;
	}
}
