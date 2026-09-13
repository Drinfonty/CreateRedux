package com.drinfonty.create_redux.client.render;

import com.drinfonty.create_redux.client.visual.KineticVisualMath;
import com.drinfonty.create_redux.client.visual.VisualManager;
import com.drinfonty.create_redux.content.kinetics.belt.BeltBlock;
import com.drinfonty.create_redux.content.kinetics.belt.BeltBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * CPU fallback renderer for conveyor belts and internal pulleys on 1.21.11.
 */
public class BeltRenderer implements BlockEntityRenderer<BeltBlockEntity, BeltRenderState> {
	public BeltRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public BeltRenderState createRenderState() {
		return new BeltRenderState();
	}

	@Override
	public void extractRenderState(BeltBlockEntity be, BeltRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
		BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
		state.blockState = be.getBlockState();
		if (state.blockState.hasProperty(BeltBlock.HORIZONTAL_FACING)) {
			state.facing = state.blockState.getValue(BeltBlock.HORIZONTAL_FACING);
		}
		state.pulleyAxis = (state.facing.getAxis() == Direction.Axis.Z) ? Direction.Axis.X : Direction.Axis.Z;
		state.speed = be.getSpeed();
		state.pulleyAngle = KineticVisualMath.getAngle(be.getSpeed(), partialTick, be.getRotationOffset());
		state.scrollOffset = KineticVisualMath.getBeltScroll(be.getSpeed(), partialTick);
	}

	@Override
	public void submit(BeltRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
		if (state.blockState == null) return;
		if (VisualManager.isInstanced(state.blockPos)) {
			return;
		}

		poseStack.pushPose();

		// Submit belt casing/housing model
		collector.submitBlock(poseStack, state.blockState, state.lightCoords, 0, 0);

		// Render internal pulley shaft rotation
		poseStack.pushPose();
		poseStack.translate(0.5, 0.5, 0.5);
		if (state.pulleyAxis == Direction.Axis.X) {
			poseStack.mulPose(Axis.XP.rotationDegrees(state.pulleyAngle));
		} else {
			poseStack.mulPose(Axis.ZP.rotationDegrees(state.pulleyAngle));
		}
		poseStack.translate(-0.5, -0.5, -0.5);
		poseStack.popPose();

		poseStack.popPose();
	}
}
