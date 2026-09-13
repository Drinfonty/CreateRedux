package com.drinfonty.create_redux.client.visual;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for visuals with instanced GPU model representations.
 */
public abstract class InstancedVisual<T> implements Visual {
	protected final T blockEntity;
	protected final BlockPos pos;
	protected final List<FlwModelInstance> instances = new ArrayList<>();
	protected boolean deleted = false;

	public InstancedVisual(T blockEntity, BlockPos pos) {
		this.blockEntity = blockEntity;
		this.pos = pos;
	}

	@Override
	public BlockPos getPos() {
		return pos;
	}

	public T getBlockEntity() {
		return blockEntity;
	}

	public List<FlwModelInstance> getInstances() {
		return Collections.unmodifiableList(instances);
	}

	@Override
	public void updateLight(int packedLight) {
		for (FlwModelInstance instance : instances) {
			instance.setLight(packedLight);
		}
	}

	@Override
	public void delete() {
		this.deleted = true;
		for (FlwModelInstance instance : instances) {
			instance.delete();
		}
		instances.clear();
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}
}
