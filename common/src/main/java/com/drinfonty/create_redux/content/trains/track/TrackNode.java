package com.drinfonty.create_redux.content.trains.track;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TrackNode {
	private final BlockPos pos;
	private final Vec3 normal;
	private final List<TrackEdge> edges = new ArrayList<>();

	public TrackNode(BlockPos pos, Vec3 normal) {
		this.pos = pos.immutable();
		this.normal = normal;
	}

	public BlockPos getPos() {
		return pos;
	}

	public Vec3 getNormal() {
		return normal;
	}

	public List<TrackEdge> getEdges() {
		return Collections.unmodifiableList(edges);
	}

	public void addEdge(TrackEdge edge) {
		if (!edges.contains(edge)) {
			edges.add(edge);
		}
	}

	public void removeEdge(TrackEdge edge) {
		edges.remove(edge);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TrackNode trackNode = (TrackNode) o;
		return Objects.equals(pos, trackNode.pos);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pos);
	}
}
