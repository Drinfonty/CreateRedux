package com.drinfonty.create_redux.content.trains.track;

import net.minecraft.core.BlockPos;

import java.util.*;

public class TrackGraph {
	private final UUID id;
	private final Map<BlockPos, TrackNode> nodes = new HashMap<>();
	private final Set<TrackEdge> edges = new HashSet<>();

	public TrackGraph() {
		this(UUID.randomUUID());
	}

	public TrackGraph(UUID id) {
		this.id = id;
	}

	public UUID getId() {
		return id;
	}

	public Map<BlockPos, TrackNode> getNodes() {
		return Collections.unmodifiableMap(nodes);
	}

	public Set<TrackEdge> getEdges() {
		return Collections.unmodifiableSet(edges);
	}

	public TrackNode getOrCreateNode(BlockPos pos) {
		return nodes.computeIfAbsent(pos.immutable(), p -> new TrackNode(p, net.minecraft.world.phys.Vec3.atCenterOf(p)));
	}

	public TrackNode getNode(BlockPos pos) {
		return nodes.get(pos);
	}

	public TrackEdge connect(BlockPos pos1, BlockPos pos2, double length, boolean isTurn) {
		TrackNode n1 = getOrCreateNode(pos1);
		TrackNode n2 = getOrCreateNode(pos2);
		TrackEdge edge = new TrackEdge(n1, n2, length, isTurn);
		n1.addEdge(edge);
		n2.addEdge(edge);
		edges.add(edge);
		return edge;
	}

	public void removeNode(BlockPos pos) {
		TrackNode node = nodes.remove(pos);
		if (node != null) {
			for (TrackEdge edge : new ArrayList<>(node.getEdges())) {
				TrackNode other = edge.getOtherNode(node);
				other.removeEdge(edge);
				edges.remove(edge);
			}
		}
	}

	/**
	 * Dijkstra shortest path routing between two nodes on the track graph.
	 */
	public List<TrackNode> findPath(TrackNode start, TrackNode target) {
		if (start == null || target == null) return Collections.emptyList();
		if (start.equals(target)) return List.of(start);

		Map<TrackNode, Double> distances = new HashMap<>();
		Map<TrackNode, TrackNode> previous = new HashMap<>();
		PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));

		distances.put(start, 0.0);
		pq.add(new NodeDistance(start, 0.0));

		while (!pq.isEmpty()) {
			NodeDistance current = pq.poll();
			if (current.node.equals(target)) break;

			if (current.distance > distances.getOrDefault(current.node, Double.MAX_VALUE)) continue;

			for (TrackEdge edge : current.node.getEdges()) {
				TrackNode neighbor = edge.getOtherNode(current.node);
				double newDist = current.distance + edge.getLength();
				if (newDist < distances.getOrDefault(neighbor, Double.MAX_VALUE)) {
					distances.put(neighbor, newDist);
					previous.put(neighbor, current.node);
					pq.add(new NodeDistance(neighbor, newDist));
				}
			}
		}

		if (!previous.containsKey(target)) return Collections.emptyList();

		List<TrackNode> path = new ArrayList<>();
		TrackNode curr = target;
		while (curr != null) {
			path.add(0, curr);
			curr = previous.get(curr);
		}
		return path;
	}

	private record NodeDistance(TrackNode node, double distance) {}
}
