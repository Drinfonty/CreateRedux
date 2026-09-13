package com.drinfonty.create_redux.content.trains.bogey;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BogeyBlock extends Block implements EntityBlock {
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

	protected static final VoxelShape SMALL_SHAPE = Block.box(0, 0, 0, 16, 12, 16);
	protected static final VoxelShape LARGE_SHAPE = Block.box(0, 0, 0, 16, 24, 16);

	private final BogeySizes size;

	public BogeyBlock(Properties properties, BogeySizes size) {
		super(properties);
		this.size = size;
		registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.Z));
	}

	public BogeySizes getSize() {
		return size;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return size == BogeySizes.LARGE ? LARGE_SHAPE : SMALL_SHAPE;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis());
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		BogeyBlockEntity be = new BogeyBlockEntity(pos, state);
		be.setSize(size);
		return be;
	}
}
