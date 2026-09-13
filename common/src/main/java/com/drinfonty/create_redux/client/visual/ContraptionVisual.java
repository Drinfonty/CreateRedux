package com.drinfonty.create_redux.client.visual;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Instanced visual for moving contraptions.
 * Applies composite matrix transformations across all constituent block instances.
 */
public class ContraptionVisual implements DynamicVisual {
	private final BlockPos anchorPos;
	private final List<ContraptionBlockEntry> blocks = new ArrayList<>();
	private final List<FlwModelInstance> instances = new ArrayList<>();
	private double posX;
	private double posY;
	private double posZ;
	private float yaw;
	private float pitch;
	private float roll;
	private boolean deleted = false;

	public static class ContraptionBlockEntry {
		public final BlockState state;
		public final BlockPos localPos;
		public final FlwModelInstance instance;

		public ContraptionBlockEntry(BlockState state, BlockPos localPos, FlwModelInstance instance) {
			this.state = state;
			this.localPos = localPos;
			this.instance = instance;
		}
	}

	public ContraptionVisual(BlockPos anchorPos) {
		this.anchorPos = anchorPos;
		this.posX = anchorPos.getX();
		this.posY = anchorPos.getY();
		this.posZ = anchorPos.getZ();
	}

	public void addBlock(BlockState state, BlockPos localPos) {
		FlwModelInstance instance = new FlwModelInstance(state, posX + localPos.getX(), posY + localPos.getY(), posZ + localPos.getZ());
		blocks.add(new ContraptionBlockEntry(state, localPos, instance));
		instances.add(instance);
	}

	public void updateTransform(double x, double y, double z, float yaw, float pitch, float roll) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.roll = roll;
		applyTransform();
	}

	private void applyTransform() {
		Matrix4f matrix = new Matrix4f();
		matrix.translate((float) posX, (float) posY, (float) posZ);
		matrix.rotate((float) Math.toRadians(yaw), 0.0f, 1.0f, 0.0f);
		matrix.rotate((float) Math.toRadians(pitch), 1.0f, 0.0f, 0.0f);
		matrix.rotate((float) Math.toRadians(roll), 0.0f, 0.0f, 1.0f);

		Vector4f vec = new Vector4f();
		for (ContraptionBlockEntry entry : blocks) {
			vec.set(entry.localPos.getX(), entry.localPos.getY(), entry.localPos.getZ(), 1.0f);
			vec.mul(matrix);
			entry.instance.setPosition(vec.x(), vec.y(), vec.z());
		}
	}

	@Override
	public BlockPos getPos() {
		return anchorPos;
	}

	@Override
	public void init() {
		applyTransform();
	}

	@Override
	public void update(float partialTicks) {
		applyTransform();
	}

	@Override
	public void updateLight(int packedLight) {
		for (FlwModelInstance instance : instances) {
			instance.setLight(packedLight);
		}
	}

	@Override
	public void beginFrame(float partialTicks) {
		if (deleted) return;
		applyTransform();
	}

	@Override
	public void delete() {
		this.deleted = true;
		for (FlwModelInstance instance : instances) {
			instance.delete();
		}
		instances.clear();
		blocks.clear();
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	public List<ContraptionBlockEntry> getBlocks() {
		return Collections.unmodifiableList(blocks);
	}

	public List<FlwModelInstance> getInstances() {
		return Collections.unmodifiableList(instances);
	}
}
