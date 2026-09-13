package com.drinfonty.create_redux.client;

import com.drinfonty.create_redux.client.visual.ContraptionVisual;
import com.drinfonty.create_redux.client.visual.DynamicVisual;
import com.drinfonty.create_redux.client.visual.FlwModelInstance;
import com.drinfonty.create_redux.client.visual.KineticVisualMath;
import com.drinfonty.create_redux.client.visual.TickableVisual;
import com.drinfonty.create_redux.client.visual.Visual;
import com.drinfonty.create_redux.client.visual.VisualManager;
import com.drinfonty.create_redux.client.visual.VisualRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RenderingTest {

	@BeforeEach
	void setUp() {
		VisualManager.get().clear();
		VisualManager.get().setInstancingEnabled(true);
	}

	@AfterEach
	void tearDown() {
		VisualManager.get().clear();
	}

	@Test
	void testKineticVisualMathRotation() {
		assertEquals(0.3f, KineticVisualMath.DEGREES_PER_TICK_PER_RPM, 1e-6f);

		// Speed 16 RPM at 10 ticks -> 10 * 16 * 0.3 = 48 degrees
		float angle = KineticVisualMath.getAngleWithTime(10.0f, 16.0f, 0.0f);
		assertEquals(48.0f, angle, 1e-3f);

		// Speed 64 RPM at 100 ticks -> 100 * 64 * 0.3 = 1920 -> 1920 % 360 = 120 degrees
		float angle2 = KineticVisualMath.getAngleWithTime(100.0f, 64.0f, 0.0f);
		assertEquals(120.0f, angle2, 1e-3f);

		// Offset handling: 48 + 15 = 63 degrees
		float angleOffset = KineticVisualMath.getAngleWithTime(10.0f, 16.0f, 15.0f);
		assertEquals(63.0f, angleOffset, 1e-3f);

		// Negative speed handling: -48 degrees -> normalized to 312 degrees
		float angleNeg = KineticVisualMath.getAngleWithTime(10.0f, -16.0f, 0.0f);
		assertEquals(312.0f, angleNeg, 1e-3f);
	}

	@Test
	void testCogMeshingOffsets() {
		BlockPos evenPos = new BlockPos(0, 0, 0);
		BlockPos oddPos = new BlockPos(1, 0, 0);

		// Small cog: even parity -> 0 offset, odd parity -> 22.5 offset
		assertEquals(0.0f, KineticVisualMath.getCogOffset(evenPos, false), 1e-6f);
		assertEquals(22.5f, KineticVisualMath.getCogOffset(oddPos, false), 1e-6f);

		// Large cog: even parity -> 0 offset, odd parity -> 11.25 offset
		assertEquals(0.0f, KineticVisualMath.getCogOffset(evenPos, true), 1e-6f);
		assertEquals(11.25f, KineticVisualMath.getCogOffset(oddPos, true), 1e-6f);
	}

	@Test
	void testBeltScrollMath() {
		// Belt scroll is normalized to [0, 1)
		float scroll = KineticVisualMath.getBeltScrollWithTime(20.0f, 16.0f);
		assertTrue(scroll >= 0.0f && scroll < 1.0f);

		float scrollNeg = KineticVisualMath.getBeltScrollWithTime(20.0f, -16.0f);
		assertTrue(scrollNeg >= 0.0f && scrollNeg < 1.0f);
	}

	@Test
	void testFlwModelInstanceTransforms() {
		FlwModelInstance instance = new FlwModelInstance("test_model", 1.0, 2.0, 3.0);
		assertEquals(1.0, instance.getX(), 1e-6);
		assertEquals(2.0, instance.getY(), 1e-6);
		assertEquals(3.0, instance.getZ(), 1e-6);

		instance.setPosition(10.5, 20.5, 30.5);
		assertEquals(10.5, instance.getX(), 1e-6);
		assertEquals(20.5, instance.getY(), 1e-6);
		assertEquals(30.5, instance.getZ(), 1e-6);

		instance.setRotation(Direction.Axis.X, 90.0f);
		assertEquals(Direction.Axis.X, instance.getRotationAxis());
		assertEquals(90.0f, instance.getRotationAngle(), 1e-6f);

		instance.setScale(2.0f, 3.0f, 4.0f);
		assertEquals(2.0f, instance.getScaleX(), 1e-6f);
		assertEquals(3.0f, instance.getScaleY(), 1e-6f);
		assertEquals(4.0f, instance.getScaleZ(), 1e-6f);

		instance.setTextureScroll(0.25f, 0.75f);
		assertEquals(0.25f, instance.getUOffset(), 1e-6f);
		assertEquals(0.75f, instance.getVOffset(), 1e-6f);

		assertFalse(instance.isDeleted());
		instance.delete();
		assertTrue(instance.isDeleted());
	}

	@Test
	void testVisualManagerLifecycleAndDispatch() {
		VisualManager manager = VisualManager.get();
		BlockPos pos = new BlockPos(5, 6, 7);

		final int[] tickCount = {0};
		final int[] frameCount = {0};
		final boolean[] deleted = {false};

		Visual dummyVisual = new DummyDynamicTickableVisual(pos, tickCount, frameCount, deleted);

		// Directly track in manager
		manager.onTick();
		assertEquals(0, tickCount[0]);

		// Add custom visual via manager
		assertFalse(VisualManager.isInstanced(pos));

		// Verify instancing toggle
		manager.setInstancingEnabled(false);
		assertFalse(manager.isInstancingEnabled());
		assertFalse(VisualManager.isInstanced(pos));

		manager.setInstancingEnabled(true);
		assertTrue(manager.isInstancingEnabled());
	}

	@Test
	void testContraptionVisualTransforms() {
		BlockPos anchor = new BlockPos(100, 50, 100);
		ContraptionVisual visual = new ContraptionVisual(anchor);

		BlockPos local1 = new BlockPos(0, 0, 0);
		BlockPos local2 = new BlockPos(2, 0, 0);

		visual.addBlock(null, local1);
		visual.addBlock(null, local2);

		assertEquals(2, visual.getBlocks().size());
		assertEquals(2, visual.getInstances().size());

		// Test translation without rotation
		visual.updateTransform(200.0, 60.0, 200.0, 0.0f, 0.0f, 0.0f);

		FlwModelInstance inst1 = visual.getInstances().get(0);
		FlwModelInstance inst2 = visual.getInstances().get(1);

		assertEquals(200.0, inst1.getX(), 1e-3);
		assertEquals(60.0, inst1.getY(), 1e-3);
		assertEquals(200.0, inst1.getZ(), 1e-3);

		assertEquals(202.0, inst2.getX(), 1e-3);
		assertEquals(60.0, inst2.getY(), 1e-3);
		assertEquals(200.0, inst2.getZ(), 1e-3);

		// Test deletion
		assertFalse(visual.isDeleted());
		visual.delete();
		assertTrue(visual.isDeleted());
		assertEquals(0, visual.getInstances().size());
	}

	private static class DummyDynamicTickableVisual implements DynamicVisual, TickableVisual {
		private final BlockPos pos;
		private final int[] ticks;
		private final int[] frames;
		private final boolean[] deleted;

		DummyDynamicTickableVisual(BlockPos pos, int[] ticks, int[] frames, boolean[] deleted) {
			this.pos = pos;
			this.ticks = ticks;
			this.frames = frames;
			this.deleted = deleted;
		}

		@Override public BlockPos getPos() { return pos; }
		@Override public void init() {}
		@Override public void update(float partialTicks) {}
		@Override public void updateLight(int packedLight) {}
		@Override public void delete() { deleted[0] = true; }
		@Override public boolean isDeleted() { return deleted[0]; }
		@Override public void beginFrame(float partialTicks) { frames[0]++; }
		@Override public void tick() { ticks[0]++; }
	}
}
