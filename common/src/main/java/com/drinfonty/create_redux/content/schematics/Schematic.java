package com.drinfonty.create_redux.content.schematics;

import com.drinfonty.create_redux.platform.NbtCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class Schematic {
	private String name = "untitled";
	private int width = 0;
	private int height = 0;
	private int length = 0;
	private BlockPos anchor = BlockPos.ZERO;
	private final Map<BlockPos, BlockState> blocks = new HashMap<>();
	private final Map<BlockPos, CompoundTag> blockEntities = new HashMap<>();

	public Schematic() {}

	public Schematic(String name, int width, int height, int length) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.length = length;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getLength() {
		return length;
	}

	public BlockPos getAnchor() {
		return anchor;
	}

	public void setAnchor(BlockPos anchor) {
		this.anchor = anchor.immutable();
	}

	public Map<BlockPos, BlockState> getBlocks() {
		return Collections.unmodifiableMap(blocks);
	}

	public Map<BlockPos, CompoundTag> getBlockEntities() {
		return Collections.unmodifiableMap(blockEntities);
	}

	public void setBlock(BlockPos relativePos, BlockState state) {
		blocks.put(relativePos.immutable(), state);
		width = Math.max(width, relativePos.getX() + 1);
		height = Math.max(height, relativePos.getY() + 1);
		length = Math.max(length, relativePos.getZ() + 1);
	}

	public BlockState getBlock(BlockPos relativePos) {
		return blocks.getOrDefault(relativePos, Blocks.AIR.defaultBlockState());
	}

	public void setBlockEntity(BlockPos relativePos, CompoundTag tag) {
		blockEntities.put(relativePos.immutable(), tag.copy());
	}

	public CompoundTag getBlockEntity(BlockPos relativePos) {
		return blockEntities.get(relativePos);
	}

	public CompoundTag writeToTag(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		tag.putString("Name", name);
		tag.putInt("Width", width);
		tag.putInt("Height", height);
		tag.putInt("Length", length);
		tag.putLong("Anchor", anchor.asLong());

		// Palette mapping
		List<BlockState> palette = new ArrayList<>();
		Map<BlockState, Integer> paletteIndices = new HashMap<>();
		ListTag paletteTag = new ListTag();

		for (BlockState state : blocks.values()) {
			if (!paletteIndices.containsKey(state)) {
				paletteIndices.put(state, palette.size());
				palette.add(state);
				CompoundTag stateTag = new CompoundTag();
				stateTag.putString("Name", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
				paletteTag.add(stateTag);
			}
		}
		tag.put("Palette", paletteTag);

		// Block entries
		ListTag blocksTag = new ListTag();
		for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
			CompoundTag blockTag = new CompoundTag();
			BlockPos pos = entry.getKey();
			blockTag.putInt("X", pos.getX());
			blockTag.putInt("Y", pos.getY());
			blockTag.putInt("Z", pos.getZ());
			blockTag.putInt("State", paletteIndices.get(entry.getValue()));
			blocksTag.add(blockTag);
		}
		tag.put("Blocks", blocksTag);

		// Block entity entries
		ListTag beTag = new ListTag();
		for (Map.Entry<BlockPos, CompoundTag> entry : blockEntities.entrySet()) {
			CompoundTag entryTag = new CompoundTag();
			BlockPos pos = entry.getKey();
			entryTag.putInt("X", pos.getX());
			entryTag.putInt("Y", pos.getY());
			entryTag.putInt("Z", pos.getZ());
			entryTag.put("Data", entry.getValue());
			beTag.add(entryTag);
		}
		tag.put("BlockEntities", beTag);

		return tag;
	}

	public void readFromTag(CompoundTag tag, HolderLookup.Provider registries) {
		blocks.clear();
		blockEntities.clear();

		name = NbtCompat.getString(tag, "Name", "untitled");
		width = NbtCompat.getInt(tag, "Width", 0);
		height = NbtCompat.getInt(tag, "Height", 0);
		length = NbtCompat.getInt(tag, "Length", 0);
		long anchorPacked = NbtCompat.getLong(tag, "Anchor", 0L);
		if (anchorPacked != 0L) {
			anchor = BlockPos.of(anchorPacked);
		}

		// Read palette
		List<BlockState> palette = new ArrayList<>();
		ListTag paletteTag = NbtCompat.getList(tag, "Palette");
		for (int i = 0; i < paletteTag.size(); i++) {
			CompoundTag stateTag = NbtCompat.getCompoundAt(paletteTag, i);
			String blockId = NbtCompat.getString(stateTag, "Name", "");
			if (!blockId.isEmpty()) {
				var blockHolder = BuiltInRegistries.BLOCK.get(Identifier.parse(blockId));
				BlockState defaultState = blockHolder.map(h -> h.value().defaultBlockState()).orElse(Blocks.AIR.defaultBlockState());
				palette.add(defaultState);
			} else {
				palette.add(Blocks.AIR.defaultBlockState());
			}
		}

		// Read blocks
		ListTag blocksTag = NbtCompat.getList(tag, "Blocks");
		for (int i = 0; i < blocksTag.size(); i++) {
			CompoundTag blockTag = NbtCompat.getCompoundAt(blocksTag, i);
			int x = NbtCompat.getInt(blockTag, "X", 0);
			int y = NbtCompat.getInt(blockTag, "Y", 0);
			int z = NbtCompat.getInt(blockTag, "Z", 0);
			int stateIdx = NbtCompat.getInt(blockTag, "State", -1);
			BlockState state = (stateIdx >= 0 && stateIdx < palette.size()) ? palette.get(stateIdx) : Blocks.AIR.defaultBlockState();
			blocks.put(new BlockPos(x, y, z), state);
		}

		// Read block entities
		ListTag beTag = NbtCompat.getList(tag, "BlockEntities");
		for (int i = 0; i < beTag.size(); i++) {
			CompoundTag entryTag = NbtCompat.getCompoundAt(beTag, i);
			int x = NbtCompat.getInt(entryTag, "X", 0);
			int y = NbtCompat.getInt(entryTag, "Y", 0);
			int z = NbtCompat.getInt(entryTag, "Z", 0);
			CompoundTag data = NbtCompat.getCompound(entryTag, "Data");
			blockEntities.put(new BlockPos(x, y, z), data);
		}
	}
}
