package com.drinfonty.create_redux.client.visual;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Model instance representing an instanced GPU mesh transform buffer.
 * Provides fluent chainable methods for position, rotation, scale, color, and light.
 */
public class FlwModelInstance {
	private Object model;
	private double x;
	private double y;
	private double z;
	private Direction.Axis rotationAxis = Direction.Axis.Y;
	private float rotationAngle = 0.0f;
	private float scaleX = 1.0f;
	private float scaleY = 1.0f;
	private float scaleZ = 1.0f;
	private int packedLight = 0x00F000F0;
	private int color = 0xFFFFFFFF;
	private float uOffset = 0.0f;
	private float vOffset = 0.0f;
	private boolean visible = true;
	private boolean deleted = false;

	public FlwModelInstance(Object model) {
		this.model = model;
	}

	public FlwModelInstance(Object model, double x, double y, double z) {
		this.model = model;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Object getModel() {
		return model;
	}

	public FlwModelInstance setModel(Object model) {
		this.model = model;
		return this;
	}

	public double getX() { return x; }
	public double getY() { return y; }
	public double getZ() { return z; }

	public FlwModelInstance setPosition(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public Direction.Axis getRotationAxis() {
		return rotationAxis;
	}

	public float getRotationAngle() {
		return rotationAngle;
	}

	public FlwModelInstance setRotation(Direction.Axis axis, float angleDegrees) {
		this.rotationAxis = axis;
		this.rotationAngle = angleDegrees;
		return this;
	}

	public float getScaleX() { return scaleX; }
	public float getScaleY() { return scaleY; }
	public float getScaleZ() { return scaleZ; }

	public FlwModelInstance setScale(float scale) {
		return setScale(scale, scale, scale);
	}

	public FlwModelInstance setScale(float sx, float sy, float sz) {
		this.scaleX = sx;
		this.scaleY = sy;
		this.scaleZ = sz;
		return this;
	}

	public int getPackedLight() {
		return packedLight;
	}

	public FlwModelInstance setLight(int packedLight) {
		this.packedLight = packedLight;
		return this;
	}

	public int getColor() {
		return color;
	}

	public FlwModelInstance setColor(int color) {
		this.color = color;
		return this;
	}

	public float getUOffset() { return uOffset; }
	public float getVOffset() { return vOffset; }

	public FlwModelInstance setTextureScroll(float u, float v) {
		this.uOffset = u;
		this.vOffset = v;
		return this;
	}

	public boolean isVisible() {
		return visible;
	}

	public FlwModelInstance setVisible(boolean visible) {
		this.visible = visible;
		return this;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void delete() {
		this.deleted = true;
	}
}
