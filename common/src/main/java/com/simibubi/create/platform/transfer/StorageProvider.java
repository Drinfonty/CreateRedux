package com.simibubi.create.platform.transfer;

public interface StorageProvider<T> {
	int getSlots();

	T getStackInSlot(int slot);

	long getSlotCapacity(int slot);

	T insert(int slot, T resource, boolean simulate);

	T extract(int slot, long maxAmount, boolean simulate);
}
