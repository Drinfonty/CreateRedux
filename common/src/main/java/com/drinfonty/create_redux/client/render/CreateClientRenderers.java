package com.drinfonty.create_redux.client.render;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Loader-neutral registration helper for client block entity renderers.
 */
public class CreateClientRenderers {
	@FunctionalInterface
	public interface RegistrationConsumer {
		<T extends BlockEntity, S extends BlockEntityRenderState> void register(
				BlockEntityType<? extends T> type,
				BlockEntityRendererProvider<T, S> provider
		);
	}

	public static void registerAll(RegistrationConsumer consumer) {
		consumer.register(AllBlockEntityTypes.SHAFT.get(), ShaftRenderer::new);
		consumer.register(AllBlockEntityTypes.HAND_CRANK.get(), ShaftRenderer::new);
		consumer.register(AllBlockEntityTypes.WATER_WHEEL.get(), ShaftRenderer::new);
		consumer.register(AllBlockEntityTypes.GEARBOX.get(), ShaftRenderer::new);
		consumer.register(AllBlockEntityTypes.MECHANICAL_BEARING.get(), ShaftRenderer::new);
		consumer.register(AllBlockEntityTypes.WINDMILL_BEARING.get(), ShaftRenderer::new);
		consumer.register(AllBlockEntityTypes.BELT.get(), BeltRenderer::new);
	}
}
