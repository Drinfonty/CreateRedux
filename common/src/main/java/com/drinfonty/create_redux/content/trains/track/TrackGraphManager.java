package com.drinfonty.create_redux.content.trains.track;

import net.minecraft.core.BlockPos;

import java.util.*;

public class TrackGraphManager {
	private static final TrackGraphManager INSTANCE = new TrackGraphManager();
	private final Map<UUID, TrackGraph> graphs = new HashMap<>();
	private final Map<BlockPos, TrackGraph> graphByPos = new HashMap<>();

	public static TrackGraphManager get() {
		return INSTANCE;
	}

	public TrackGraph getOrCreateDefaultGraph() {
		if (graphs.isEmpty()) {
			TrackGraph defaultGraph = new TrackGraph();
			graphs.put(defaultGraph.getId(), defaultGraph);
			return defaultGraph;
		}
		return graphs.values().iterator().next();
	}

	public void registerTrack(BlockPos pos, TrackShape shape) {
		TrackGraph graph = getOrCreateDefaultGraph();
		graph.getOrCreateNode(pos);
		graphByPos.put(pos.immutable(), graph);

		// Automatically connect straight neighbors along axis
		if (shape == TrackShape.XO) {
			checkAndConnect(graph, pos, pos.east(), 1.0);
			checkAndConnect(graph, pos, pos.west(), 1.0);
		} else if (shape == TrackShape.ZO) {
			checkAndConnect(graph, pos, pos.north(), 1.0);
			checkAndConnect(graph, pos, pos.south(), 1.0);
		}
	}

	private void checkAndConnect(TrackGraph graph, BlockPos pos1, BlockPos pos2, double length) {
		if (graph.getNodes().containsKey(pos2)) {
			graph.connect(pos1, pos2, length, false);
		}
	}

	public void unregisterTrack(BlockPos pos) {
		TrackGraph graph = graphByPos.remove(pos);
		if (graph != null) {
			graph.removeNode(pos);
		}
	}

	public TrackGraph getGraphAt(BlockPos pos) {
		return graphByPos.get(pos);
	}

	public void clear() {
		graphs.clear();
		graphByPos.clear();
	}
}
