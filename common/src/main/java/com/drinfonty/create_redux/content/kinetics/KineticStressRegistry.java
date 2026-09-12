package com.drinfonty.create_redux.content.kinetics;

import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class KineticStressRegistry {
	private static final Map<Block, Float> CAPACITIES = new HashMap<>();
	private static final Map<Block, Float> IMPACTS = new HashMap<>();

	public static void registerCapacity(Block block, float capacity) {
		CAPACITIES.put(block, capacity);
	}

	public static void registerImpact(Block block, float impact) {
		IMPACTS.put(block, impact);
	}

	public static float getCapacity(Block block) {
		return CAPACITIES.getOrDefault(block, 0.0f);
	}

	public static float getImpact(Block block) {
		return IMPACTS.getOrDefault(block, 0.0f);
	}
}
