package com.drinfonty.create_redux.client.render;

import com.drinfonty.create_redux.client.visual.KineticVisualMath;
import com.drinfonty.create_redux.client.visual.VisualManager;
import com.drinfonty.create_redux.content.kinetics.KineticBlock;
import com.drinfonty.create_redux.content.kinetics.KineticBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class ShaftRenderer<T extends KineticBlockEntity> implements BlockEntityRenderer<T, KineticRenderState> {
	private final BlockModelResolver blockModelResolver;
	private final BlockModelRenderState modelRenderState = new BlockModelRenderState();
	private final BlockDisplayContext displayContext = BlockDisplayContext.create();

	public ShaftRenderer(BlockEntityRendererProvider.Context context) {
		this.blockModelResolver = context.blockModelResolver();
	}

	@Override
	public KineticRenderState createRenderState() {
		return new KineticRenderState();
	}

	@Override
	public void extractRenderState(T be, KineticRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
		BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
		state.blockState = be.getBlockState();
		if (state.blockState.hasProperty(KineticBlock.AXIS)) {
			state.axis = state.blockState.getValue(KineticBlock.AXIS);
		} else {
			state.axis = Direction.Axis.Y;
		}
		state.speed = be.getSpeed();
		state.angle = KineticVisualMath.getAngle(be.getSpeed(), partialTick, be.getRotationOffset());
	}

	@Override
	public void submit(KineticRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
		if (state.blockState == null) return;
		if (VisualManager.isInstanced(state.blockPos)) {
			return;
		}

		poseStack.pushPose();
		poseStack.translate(0.5, 0.5, 0.5);
		if (state.axis == Direction.Axis.X) {
			poseStack.mulPose(Axis.XP.rotationDegrees(state.angle));
		} else if (state.axis == Direction.Axis.Y) {
			poseStack.mulPose(Axis.YP.rotationDegrees(state.angle));
		} else if (state.axis == Direction.Axis.Z) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(state.angle));
		}
		poseStack.translate(-0.5, -0.5, -0.5);

		modelRenderState.clear();
		blockModelResolver.update(modelRenderState, state.blockState, displayContext);
		modelRenderState.submit(poseStack, collector, state.lightCoords, 0, 0);

		poseStack.popPose();
	}
}
