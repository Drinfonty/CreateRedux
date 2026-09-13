package com.drinfonty.create_redux.content.trains.track;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class TrackBlock extends Block implements EntityBlock {
	public static final EnumProperty<TrackShape> SHAPE = EnumProperty.create("shape", TrackShape.class);
	public static final BooleanProperty HAS_BEZIER = BooleanProperty.create("has_bezier");

	protected static final VoxelShape FLAT_AABB = Block.box(0, 0, 0, 16, 2, 16);

	public TrackBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState()
				.setValue(SHAPE, TrackShape.ZO)
				.setValue(HAS_BEZIER, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(SHAPE, HAS_BEZIER);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return FLAT_AABB;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction.Axis axis = context.getHorizontalDirection().getAxis();
		TrackShape shape = axis == Direction.Axis.X ? TrackShape.XO : TrackShape.ZO;
		return defaultBlockState().setValue(SHAPE, shape);
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!state.is(oldState.getBlock())) {
			TrackGraphManager.get().registerTrack(pos, state.getValue(SHAPE));
		}
	}

	@Override
	public void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean isMoving) {
		TrackGraphManager.get().unregisterTrack(pos);
		super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TrackBlockEntity(pos, state);
	}
}
