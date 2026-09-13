package com.drinfonty.create_redux.content.trains.track;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

public enum TrackShape implements StringRepresentable {
	XO("xo", Direction.Axis.X),
	ZO("zo", Direction.Axis.Z),
	PD("pd", null),
	ND("nd", null),
	CR_O("cr_o", null);

	private final String name;
	private final Direction.Axis axis;

	TrackShape(String name, Direction.Axis axis) {
		this.name = name;
		this.axis = axis;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public boolean isStraight() {
		return axis != null;
	}

	public boolean isDiagonal() {
		return this == PD || this == ND;
	}

	public Direction.Axis getAxis() {
		return axis;
	}
}
