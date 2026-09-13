package com.drinfonty.create_redux.client.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/**
 * Clean-room kinematics rotation, meshing, and scroll math.
 */
public class KineticVisualMath {
	/**
	 * 1 RPM = 360 degrees / 1200 ticks = 0.3 degrees per tick.
	 */
	public static final float DEGREES_PER_TICK_PER_RPM = 0.3f;

	/**
	 * Computes dynamic rotation angle (in degrees [0, 360)) based on speed, partial ticks, and offset.
	 */
	public static float getAngle(float speed, float partialTicks, float offset) {
		float time = getRenderTime(partialTicks);
		return getAngleWithTime(time, speed, offset);
	}

	/**
	 * Computes angle given an explicit continuous time in ticks.
	 */
	public static float getAngleWithTime(float timeInTicks, float speed, float offset) {
		float angle = (timeInTicks * speed * DEGREES_PER_TICK_PER_RPM + offset) % 360.0f;
		return angle < 0 ? angle + 360.0f : angle;
	}

	/**
	 * Returns rotation offset for cogwheel tooth meshing.
	 * Small cogwheels (8 teeth) interlock at a 22.5 degree half-tooth offset.
	 * Large cogwheels (16 teeth) interlock at an 11.25 degree half-tooth offset.
	 */
	public static float getCogOffset(BlockPos pos, boolean isLarge) {
		int parity = Math.abs(pos.getX() + pos.getY() + pos.getZ()) & 1;
		if (isLarge) {
			return parity != 0 ? 11.25f : 0.0f;
		} else {
			return parity != 0 ? 22.5f : 0.0f;
		}
	}

	/**
	 * Computes UV scrolling offset for conveyor belts based on rotational speed.
	 */
	public static float getBeltScroll(float speed, float partialTicks) {
		float time = getRenderTime(partialTicks);
		return getBeltScrollWithTime(time, speed);
	}

	/**
	 * Computes UV scrolling offset given explicit time in ticks.
	 */
	public static float getBeltScrollWithTime(float timeInTicks, float speed) {
		float scroll = (timeInTicks * speed * 0.005f) % 1.0f;
		return scroll < 0 ? scroll + 1.0f : scroll;
	}

	/**
	 * Retrieves continuous rendering time in ticks (gameTime + partialTicks).
	 * Falls back safely to wall-clock time if client level is not initialized.
	 */
	public static float getRenderTime(float partialTicks) {
		try {
			Minecraft mc = Minecraft.getInstance();
			if (mc.level != null) {
				return mc.level.getGameTime() + partialTicks;
			}
		} catch (Throwable ignored) {
		}
		return (System.currentTimeMillis() % 10000000L) * 0.02f + partialTicks;
	}
}
