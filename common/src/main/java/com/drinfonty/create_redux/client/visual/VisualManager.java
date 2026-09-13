package com.drinfonty.create_redux.client.visual;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global manager for active Flywheel-style visual instances.
 * Dispatches per-frame animations and client ticks, and coordinates with CPU BER fallbacks.
 */
public class VisualManager {
	private static final VisualManager INSTANCE = new VisualManager();

	private boolean instancingEnabled = true;
	private final Map<BlockPos, Visual> visuals = new ConcurrentHashMap<>();
	private final List<DynamicVisual> dynamicVisuals = new ArrayList<>();
	private final List<TickableVisual> tickableVisuals = new ArrayList<>();

	public static VisualManager get() {
		return INSTANCE;
	}

	public static boolean isInstanced(BlockPos pos) {
		return INSTANCE.instancingEnabled && INSTANCE.visuals.containsKey(pos);
	}

	public boolean isInstancingEnabled() {
		return instancingEnabled;
	}

	public void setInstancingEnabled(boolean enabled) {
		this.instancingEnabled = enabled;
		if (!enabled) {
			clear();
		}
	}

	public synchronized Visual add(BlockEntity be) {
		if (be == null || !instancingEnabled) return null;
		BlockPos pos = be.getBlockPos();
		Visual existing = visuals.get(pos);
		if (existing != null) {
			return existing;
		}

		Visual visual = VisualRegistry.create(be);
		if (visual != null) {
			visuals.put(pos, visual);
			if (visual instanceof DynamicVisual dynamicVisual) {
				dynamicVisuals.add(dynamicVisual);
			}
			if (visual instanceof TickableVisual tickableVisual) {
				tickableVisuals.add(tickableVisual);
			}
		}
		return visual;
	}

	public synchronized void remove(BlockPos pos) {
		Visual visual = visuals.remove(pos);
		if (visual != null) {
			visual.delete();
			if (visual instanceof DynamicVisual) {
				dynamicVisuals.remove(visual);
			}
			if (visual instanceof TickableVisual) {
				tickableVisuals.remove(visual);
			}
		}
	}

	public void remove(BlockEntity be) {
		if (be != null) {
			remove(be.getBlockPos());
		}
	}

	public Visual getVisual(BlockPos pos) {
		return visuals.get(pos);
	}

	public synchronized void onTick() {
		if (!instancingEnabled) return;
		for (TickableVisual visual : tickableVisuals) {
			if (!visual.isDeleted()) {
				visual.tick();
			}
		}
	}

	public synchronized void onFrame(float partialTicks) {
		if (!instancingEnabled) return;
		for (DynamicVisual visual : dynamicVisuals) {
			if (!visual.isDeleted()) {
				visual.beginFrame(partialTicks);
			}
		}
	}

	public synchronized void clear() {
		for (Visual visual : visuals.values()) {
			visual.delete();
		}
		visuals.clear();
		dynamicVisuals.clear();
		tickableVisuals.clear();
	}

	public int getActiveVisualCount() {
		return visuals.size();
	}

	public int getDynamicVisualCount() {
		return dynamicVisuals.size();
	}
}
