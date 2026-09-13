package com.drinfonty.create_redux.content.trains.entity;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class Carriage {
	private final int index;
	private final CarriageContraption contraption;
	private final List<BlockPos> bogeyPositions = new ArrayList<>();
	private double bogeySpacing = 0.0;

	public Carriage(int index, CarriageContraption contraption, List<BlockPos> bogeyPositions) {
		this.index = index;
		this.contraption = contraption;
		this.bogeyPositions.addAll(bogeyPositions);
		if (bogeyPositions.size() >= 2) {
			this.bogeySpacing = Math.sqrt(bogeyPositions.get(0).distSqr(bogeyPositions.get(1)));
		}
	}

	public int getIndex() {
		return index;
	}

	public CarriageContraption getContraption() {
		return contraption;
	}

	public List<BlockPos> getBogeyPositions() {
		return bogeyPositions;
	}

	public double getBogeySpacing() {
		return bogeySpacing;
	}
}
