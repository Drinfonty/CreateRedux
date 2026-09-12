package com.drinfonty.create_redux.content.kinetics.belt;

import net.minecraft.util.StringRepresentable;

public enum BeltPart implements StringRepresentable {
	START("start"),
	MIDDLE("middle"),
	END("end"),
	PULLEY("pulley");

	private final String name;

	BeltPart(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
