package com.drinfonty.create_redux.client.visual;

/**
 * Visual that receives high-frequency per-frame animation ticks.
 */
public interface DynamicVisual extends Visual {
	void beginFrame(float partialTicks);
}
