package com.drinfonty.create_redux.content.trains.bogey;

import net.minecraft.util.StringRepresentable;

public enum BogeySizes implements StringRepresentable {
	SMALL("small", 1.0f),
	LARGE("large", 2.0f);

	private final String name;
	private final float wheelDiameter;

	BogeySizes(String name, float wheelDiameter) {
		this.name = name;
		this.wheelDiameter = wheelDiameter;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public float getWheelDiameter() {
		return wheelDiameter;
	}
}
