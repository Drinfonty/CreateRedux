package com.drinfonty.create_redux.content.trains.signal;

import net.minecraft.util.StringRepresentable;

public enum SignalState implements StringRepresentable {
	GREEN("green"),
	YELLOW("yellow"),
	RED("red");

	private final String name;

	SignalState(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public boolean isClear() {
		return this == GREEN;
	}

	public boolean shouldStop() {
		return this == RED;
	}
}
