package com.drinfonty.create_redux.client.render;

import com.drinfonty.create_redux.AllBlocks;
import com.drinfonty.create_redux.client.visual.KineticVisualMath;
import com.drinfonty.create_redux.content.kinetics.KineticBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.phys.Vec3;

/**
 * CPU fallback renderer for small and large cogwheels with tooth meshing alignment.
 */
public class CogWheelRenderer<T extends KineticBlockEntity> extends ShaftRenderer<T> {
	public CogWheelRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void extractRenderState(T be, KineticRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
		super.extractRenderState(be, state, partialTick, cameraPos, crumblingOverlay);
		state.isCogwheel = true;
		state.isLargeCog = be.getBlockState().is(AllBlocks.LARGE_COGWHEEL.get());

		float cogOffset = KineticVisualMath.getCogOffset(be.getBlockPos(), state.isLargeCog);
		state.angle = KineticVisualMath.getAngle(be.getSpeed(), partialTick, be.getRotationOffset() + cogOffset);
	}
}
