package com.drinfonty.create_redux.content.logistics;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class TransportedItemStack {
	public ItemStack stack;
	public float beltPosition;
	public float prevBeltPosition;
	public Direction insertedFrom;
	public boolean locked;
	public int processingTicks;

	public TransportedItemStack(ItemStack stack) {
		this.stack = stack == null ? ItemStack.EMPTY : stack;
		this.beltPosition = 0.0f;
		this.prevBeltPosition = 0.0f;
		this.insertedFrom = Direction.UP;
		this.locked = false;
		this.processingTicks = 0;
	}

	public TransportedItemStack copy() {
		TransportedItemStack copy = new TransportedItemStack(stack.copy());
		copy.beltPosition = this.beltPosition;
		copy.prevBeltPosition = this.prevBeltPosition;
		copy.insertedFrom = this.insertedFrom;
		copy.locked = this.locked;
		copy.processingTicks = this.processingTicks;
		return copy;
	}
}
