package com.drinfonty.create_redux.client.visual;

import net.minecraft.core.BlockPos;

/**
 * Clean-room abstraction for a rendered visual representation.
 */
public interface Visual {
	BlockPos getPos();
	void init();
	void update(float partialTicks);
	void updateLight(int packedLight);
	void delete();
	boolean isDeleted();
}
