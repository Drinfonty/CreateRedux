package com.drinfonty.create_redux.content.trains;

import com.drinfonty.create_redux.AllBlocks;
import com.drinfonty.create_redux.content.trains.bogey.BogeyBlockEntity;
import com.drinfonty.create_redux.content.trains.bogey.BogeySizes;
import com.drinfonty.create_redux.content.trains.entity.Carriage;
import com.drinfonty.create_redux.content.trains.entity.CarriageContraption;
import com.drinfonty.create_redux.content.trains.entity.Train;
import com.drinfonty.create_redux.content.trains.schedule.TrainSchedule;
import com.drinfonty.create_redux.content.trains.signal.SignalState;
import com.drinfonty.create_redux.content.trains.signal.TrackSignalBlockEntity;
import com.drinfonty.create_redux.content.trains.station.StationBlockEntity;
import com.drinfonty.create_redux.content.trains.track.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RailwayTest {

	@BeforeAll
	static void setup() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	@DisplayName("TrackGraph and Dijkstra pathfinding finds optimal routes")
	void testTrackGraphRouting() {
		TrackGraph graph = new TrackGraph();
		BlockPos pA = new BlockPos(0, 0, 0);
		BlockPos pB = new BlockPos(10, 0, 0);
		BlockPos pC = new BlockPos(5, 0, 5);
		BlockPos pD = new BlockPos(20, 0, 0);

		// Path 1: A -> B -> D (length 10 + 10 = 20)
		graph.connect(pA, pB, 10.0, false);
		graph.connect(pB, pD, 10.0, false);

		// Path 2: A -> C -> D (length 6 + 6 = 12, shorter)
		graph.connect(pA, pC, 6.0, true);
		graph.connect(pC, pD, 6.0, true);

		TrackNode nodeA = graph.getNode(pA);
		TrackNode nodeD = graph.getNode(pD);
		assertNotNull(nodeA);
		assertNotNull(nodeD);

		List<TrackNode> path = graph.findPath(nodeA, nodeD);
		assertEquals(3, path.size(), "Dijkstra must choose shorter 3-node path A -> C -> D");
		assertEquals(pA, path.get(0).getPos());
		assertEquals(pC, path.get(1).getPos());
		assertEquals(pD, path.get(2).getPos());
	}

	@Test
	@DisplayName("TrackGraphManager registers and auto-connects straight tracks")
	void testTrackGraphManagerAutoConnect() {
		TrackGraphManager manager = TrackGraphManager.get();
		manager.clear();

		BlockPos p1 = new BlockPos(0, 10, 0);
		BlockPos p2 = new BlockPos(1, 10, 0);
		BlockPos p3 = new BlockPos(2, 10, 0);

		manager.registerTrack(p1, TrackShape.XO);
		manager.registerTrack(p2, TrackShape.XO);
		manager.registerTrack(p3, TrackShape.XO);

		TrackGraph graph = manager.getGraphAt(p1);
		assertNotNull(graph);
		TrackNode n1 = graph.getNode(p1);
		TrackNode n2 = graph.getNode(p2);
		TrackNode n3 = graph.getNode(p3);
		assertNotNull(n1);
		assertNotNull(n2);
		assertNotNull(n3);

		assertEquals(1, n1.getEdges().size());
		assertEquals(2, n2.getEdges().size());
		assertEquals(1, n3.getEdges().size());

		// Path from p1 to p3
		List<TrackNode> path = graph.findPath(n1, n3);
		assertEquals(3, path.size());
	}

	@Test
	@DisplayName("Bogey wheel rotation animation calculates correct rotation angles")
	void testBogeyWheelRotation() {
		BogeyBlockEntity smallBogey = new BogeyBlockEntity(BlockPos.ZERO, AllBlocks.SMALL_BOGEY.get().defaultBlockState());
		smallBogey.setSize(BogeySizes.SMALL);

		// Wheel diameter 1.0 -> Circumference = PI * 1.0 ~ 3.14159
		// Traveled distance = Math.PI -> exactly 360 degrees
		smallBogey.animate(Math.PI);
		assertEquals(0.0f, smallBogey.getWheelAngle(), 0.01f);

		// Half revolution
		smallBogey.animate(Math.PI / 2.0);
		assertEquals(180.0f, smallBogey.getWheelAngle(), 0.1f);

		// Large bogey (wheel diameter 2.0 -> Circumference = 2 * PI)
		BogeyBlockEntity largeBogey = new BogeyBlockEntity(BlockPos.ZERO, AllBlocks.LARGE_BOGEY.get().defaultBlockState());
		largeBogey.setSize(BogeySizes.LARGE);
		largeBogey.animate(Math.PI);
		assertEquals(180.0f, largeBogey.getWheelAngle(), 0.1f);
	}

	@Test
	@DisplayName("Train physics advances throttle, acceleration, and edge distance")
	void testTrainPhysicsAndNavigation() {
		UUID trainId = UUID.randomUUID();
		Train train = new Train(trainId, "Test Express");
		assertEquals(0.0, train.getSpeed());

		// Set throttle to 1.0
		train.setThrottle(1.0);
		train.setAcceleration(0.1);
		train.setMaxSpeed(1.0);

		// Setup track edge
		TrackNode n1 = new TrackNode(new BlockPos(0, 0, 0), null);
		TrackNode n2 = new TrackNode(new BlockPos(10, 0, 0), null);
		TrackEdge edge = new TrackEdge(n1, n2, 10.0, false);
		train.setCurrentEdge(edge);
		train.setCurrentNode(n1);

		// Tick physics
		train.tick(null);
		assertEquals(0.1, train.getSpeed(), 1e-4);
		assertEquals(0.1, train.getDistanceAlongEdge(), 1e-4);

		// Accelerate to max speed (9 more ticks)
		for (int i = 0; i < 9; i++) {
			train.tick(null);
		}
		assertEquals(1.0, train.getSpeed(), 1e-4);

		// Travel until edge end is reached (10 blocks edge length)
		while (train.getCurrentNode() == n1) {
			train.tick(null);
		}
		// Node transitioned to n2
		assertEquals(n2, train.getCurrentNode());
	}

	@Test
	@DisplayName("TrackSignalBlockEntity reserves and releases block slices")
	void testTrackSignalReservation() {
		TrackSignalBlockEntity signal = new TrackSignalBlockEntity(BlockPos.ZERO, AllBlocks.TRACK_SIGNAL.get().defaultBlockState());
		assertEquals(SignalState.GREEN, signal.getSignalState());
		assertNull(signal.getReservedByTrainId());

		UUID train1 = UUID.randomUUID();
		UUID train2 = UUID.randomUUID();

		// Train 1 reserves
		assertTrue(signal.reserve(train1));
		assertEquals(SignalState.RED, signal.getSignalState());
		assertEquals(train1, signal.getReservedByTrainId());

		// Train 2 attempts reservation and is rejected
		assertFalse(signal.reserve(train2));
		assertEquals(SignalState.RED, signal.getSignalState());

		// Train 1 releases reservation
		signal.release(train1);
		assertEquals(SignalState.GREEN, signal.getSignalState());
		assertNull(signal.getReservedByTrainId());
	}

	@Test
	@DisplayName("TrainSchedule manages itineraries and advances automatically on dwell time completion")
	void testTrainSchedule() {
		TrainSchedule schedule = new TrainSchedule();
		schedule.addEntry("Station Alpha", 20);
		schedule.addEntry("Station Beta", 40);

		assertEquals("Station Alpha", schedule.getCurrentEntry().getStationName());
		assertEquals(20, schedule.getCurrentEntry().getWaitDurationTicks());

		// Tick through 19 ticks
		for (int i = 0; i < 19; i++) {
			assertFalse(schedule.tickWait());
		}
		assertEquals("Station Alpha", schedule.getCurrentEntry().getStationName());

		// 20th tick completes wait and advances to next station
		assertTrue(schedule.tickWait());
		assertEquals("Station Beta", schedule.getCurrentEntry().getStationName());
		assertEquals(40, schedule.getCurrentEntry().getWaitDurationTicks());

		// Advance past Beta loops back to Alpha
		schedule.advance();
		assertEquals("Station Alpha", schedule.getCurrentEntry().getStationName());
	}

	@Test
	@DisplayName("Carriage and Train composition with bogey spacing")
	void testTrainCarriageAssembly() {
		Train train = new Train(UUID.randomUUID(), "Metro 1");
		CarriageContraption contraption = new CarriageContraption();
		BlockPos bogey1 = new BlockPos(0, 0, 0);
		BlockPos bogey2 = new BlockPos(0, 0, 8);
		Carriage carriage = new Carriage(0, contraption, List.of(bogey1, bogey2));
		train.addCarriage(carriage);

		assertEquals(1, train.getCarriages().size());
		assertEquals(8.0, carriage.getBogeySpacing(), 1e-4);
		assertEquals(2, carriage.getBogeyPositions().size());
		assertEquals("Metro 1", train.getName());
	}
}
