package com.drinfonty.create_redux.client.visual;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry associating BlockEntityTypes with their VisualFactories.
 */
public class VisualRegistry {
	private static final Map<BlockEntityType<?>, VisualFactory<?>> FACTORIES = new HashMap<>();
	private static boolean initialized = false;

	public static synchronized <T extends BlockEntity> void register(BlockEntityType<T> type, VisualFactory<T> factory) {
		FACTORIES.put(type, factory);
	}

	@SuppressWarnings("unchecked")
	public static synchronized <T extends BlockEntity> VisualFactory<T> getFactory(BlockEntityType<T> type) {
		ensureInitialized();
		return (VisualFactory<T>) FACTORIES.get(type);
	}

	public static boolean hasVisual(BlockEntityType<?> type) {
		ensureInitialized();
		return FACTORIES.containsKey(type);
	}

	@SuppressWarnings("unchecked")
	public static Visual create(BlockEntity be) {
		if (be == null) return null;
		VisualFactory<BlockEntity> factory = (VisualFactory<BlockEntity>) getFactory(be.getType());
		if (factory != null) {
			Visual visual = factory.create(be);
			if (visual != null) {
				visual.init();
			}
			return visual;
		}
		return null;
	}

	public static synchronized void ensureInitialized() {
		if (initialized) return;
		initialized = true;

		// Default visual registrations
		register(AllBlockEntityTypes.SHAFT.get(), ShaftVisual::new);
		register(AllBlockEntityTypes.BELT.get(), BeltVisual::new);
		register(AllBlockEntityTypes.HAND_CRANK.get(), ShaftVisual::new);
		register(AllBlockEntityTypes.WATER_WHEEL.get(), ShaftVisual::new);
		register(AllBlockEntityTypes.GEARBOX.get(), ShaftVisual::new);
		register(AllBlockEntityTypes.MECHANICAL_BEARING.get(), ShaftVisual::new);
		register(AllBlockEntityTypes.WINDMILL_BEARING.get(), ShaftVisual::new);
	}
}
