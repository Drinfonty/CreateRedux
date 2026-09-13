package com.drinfonty.create_redux.client.visual;

import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Functional interface to construct a Visual for a given BlockEntity.
 */
@FunctionalInterface
public interface VisualFactory<T extends BlockEntity> {
	Visual create(T blockEntity);
}
