package com.drinfonty.create_redux.content.trains.track;

import java.util.Objects;

public class TrackEdge {
	private final TrackNode node1;
	private final TrackNode node2;
	private final double length;
	private final boolean isTurn;

	public TrackEdge(TrackNode node1, TrackNode node2, double length, boolean isTurn) {
		this.node1 = node1;
		this.node2 = node2;
		this.length = length;
		this.isTurn = isTurn;
	}

	public TrackNode getNode1() {
		return node1;
	}

	public TrackNode getNode2() {
		return node2;
	}

	public TrackNode getOtherNode(TrackNode from) {
		return from.equals(node1) ? node2 : node1;
	}

	public double getLength() {
		return length;
	}

	public boolean isTurn() {
		return isTurn;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TrackEdge trackEdge = (TrackEdge) o;
		return Double.compare(trackEdge.length, length) == 0 &&
				isTurn == trackEdge.isTurn &&
				((Objects.equals(node1, trackEdge.node1) && Objects.equals(node2, trackEdge.node2)) ||
				(Objects.equals(node1, trackEdge.node2) && Objects.equals(node2, trackEdge.node1)));
	}

	@Override
	public int hashCode() {
		return Objects.hash(node1.hashCode() + node2.hashCode(), length, isTurn);
	}
}
