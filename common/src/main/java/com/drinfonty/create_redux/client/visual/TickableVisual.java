package com.drinfonty.create_redux.client.visual;

/**
 * Visual that receives client tick updates (20 TPS).
 */
public interface TickableVisual extends Visual {
	void tick();
}
