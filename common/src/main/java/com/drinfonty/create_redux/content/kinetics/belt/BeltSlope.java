package com.drinfonty.create_redux.content.kinetics.belt;

import net.minecraft.util.StringRepresentable;

public enum BeltSlope implements StringRepresentable {
	HORIZONTAL("horizontal"),
	UPWARD("upward"),
	DOWNWARD("downward"),
	VERTICAL("vertical");

	private final String name;

	BeltSlope(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
