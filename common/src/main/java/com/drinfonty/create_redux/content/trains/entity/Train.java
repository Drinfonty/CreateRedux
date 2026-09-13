package com.drinfonty.create_redux.content.trains.entity;

import com.drinfonty.create_redux.content.trains.bogey.BogeyBlockEntity;
import com.drinfonty.create_redux.content.trains.track.TrackEdge;
import com.drinfonty.create_redux.content.trains.track.TrackGraph;
import com.drinfonty.create_redux.content.trains.track.TrackNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Train {
	private final UUID id;
	private String name;
	private final List<Carriage> carriages = new ArrayList<>();
	private @Nullable TrackGraph graph;
	private @Nullable TrackEdge currentEdge;
	private @Nullable TrackNode currentNode;
	private @Nullable TrackNode targetNode;

	private double speed = 0.0;
	private double targetSpeed = 0.0;
	private double maxSpeed = 1.2; // ~24 blocks/second
	private double acceleration = 0.05;
	private double throttle = 0.0;
	private double distanceAlongEdge = 0.0;
	private double totalDistanceTraveled = 0.0;

	public Train(UUID id, String name) {
		this.id = id;
		this.name = name;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Carriage> getCarriages() {
		return Collections.unmodifiableList(carriages);
	}

	public void addCarriage(Carriage carriage) {
		this.carriages.add(carriage);
	}

	public @Nullable TrackGraph getGraph() {
		return graph;
	}

	public void setGraph(@Nullable TrackGraph graph) {
		this.graph = graph;
	}

	public @Nullable TrackEdge getCurrentEdge() {
		return currentEdge;
	}

	public void setCurrentEdge(@Nullable TrackEdge currentEdge) {
		this.currentEdge = currentEdge;
	}

	public @Nullable TrackNode getCurrentNode() {
		return currentNode;
	}

	public void setCurrentNode(@Nullable TrackNode currentNode) {
		this.currentNode = currentNode;
	}

	public @Nullable TrackNode getTargetNode() {
		return targetNode;
	}

	public void setTargetNode(@Nullable TrackNode targetNode) {
		this.targetNode = targetNode;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public double getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
	}

	public double getThrottle() {
		return throttle;
	}

	public void setThrottle(double throttle) {
		this.throttle = Math.max(-1.0, Math.min(1.0, throttle));
		this.targetSpeed = this.throttle * maxSpeed;
	}

	public double getDistanceAlongEdge() {
		return distanceAlongEdge;
	}

	public double getTotalDistanceTraveled() {
		return totalDistanceTraveled;
	}

	public void tick(@Nullable Level level) {
		// Update speed towards targetSpeed
		if (speed < targetSpeed) {
			speed = Math.min(targetSpeed, speed + acceleration);
		} else if (speed > targetSpeed) {
			speed = Math.max(targetSpeed, speed - acceleration);
		}

		if (Math.abs(speed) < 1e-4) {
			speed = 0.0;
			return;
		}

		double step = speed;
		distanceAlongEdge += step;
		totalDistanceTraveled += Math.abs(step);

		// Animate bogeys in world if level is provided
		if (level != null) {
			for (Carriage carriage : carriages) {
				for (BlockPos bogeyPos : carriage.getBogeyPositions()) {
					BlockEntity be = level.getBlockEntity(bogeyPos);
					if (be instanceof BogeyBlockEntity bogey) {
						bogey.animate(Math.abs(step));
					}
				}
			}
		}

		// Handle edge navigation
		if (currentEdge != null) {
			double edgeLength = currentEdge.getLength();
			if (distanceAlongEdge >= edgeLength) {
				distanceAlongEdge -= edgeLength;
				if (currentNode != null) {
					currentNode = currentEdge.getOtherNode(currentNode);
				}
			} else if (distanceAlongEdge < 0) {
				distanceAlongEdge = 0;
				speed = 0;
			}
		}
	}

	public void disassemble(Level level) {
		for (Carriage carriage : carriages) {
			carriage.getContraption().disassembleIntoWorld(level, carriage.getContraption().getAnchor());
		}
		carriages.clear();
	}
}
