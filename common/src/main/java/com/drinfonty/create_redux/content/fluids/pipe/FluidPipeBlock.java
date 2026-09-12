package com.drinfonty.create_redux.content.fluids.pipe;

import com.drinfonty.create_redux.AllBlocks;
import com.drinfonty.create_redux.platform.transfer.TransferUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class FluidPipeBlock extends Block implements EntityBlock {
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;
	public static final BooleanProperty UP = BlockStateProperties.UP;
	public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

	private static final VoxelShape CORE = Block.box(4, 4, 4, 12, 12, 12);
	private static final VoxelShape ARM_NORTH = Block.box(4, 4, 0, 12, 12, 4);
	private static final VoxelShape ARM_SOUTH = Block.box(4, 4, 12, 12, 12, 16);
	private static final VoxelShape ARM_WEST = Block.box(0, 4, 4, 4, 12, 12);
	private static final VoxelShape ARM_EAST = Block.box(12, 4, 4, 16, 12, 12);
	private static final VoxelShape ARM_DOWN = Block.box(4, 0, 4, 12, 4, 12);
	private static final VoxelShape ARM_UP = Block.box(4, 12, 4, 12, 16, 12);

	public FluidPipeBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState()
				.setValue(NORTH, false)
				.setValue(SOUTH, false)
				.setValue(EAST, false)
				.setValue(WEST, false)
				.setValue(UP, false)
				.setValue(DOWN, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
	}

	public boolean canConnectTo(Level level, BlockPos pos, Direction direction) {
		BlockPos neighborPos = pos.relative(direction);
		BlockState neighborState = level.getBlockState(neighborPos);
		Block block = neighborState.getBlock();

		if (block instanceof FluidPipeBlock) return true;
		if (AllBlocks.FLUID_TANK != null && block == AllBlocks.FLUID_TANK.get()) return true;
		if (AllBlocks.MECHANICAL_PUMP != null && block == AllBlocks.MECHANICAL_PUMP.get()) return true;
		if (AllBlocks.SPOUT != null && block == AllBlocks.SPOUT.get()) return true;
		if (AllBlocks.HOSE_PULLEY != null && block == AllBlocks.HOSE_PULLEY.get()) return true;

		return TransferUtil.getFluidStorage(level, neighborPos, direction.getOpposite()).isPresent();
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		return defaultBlockState()
				.setValue(NORTH, canConnectTo(level, pos, Direction.NORTH))
				.setValue(SOUTH, canConnectTo(level, pos, Direction.SOUTH))
				.setValue(EAST, canConnectTo(level, pos, Direction.EAST))
				.setValue(WEST, canConnectTo(level, pos, Direction.WEST))
				.setValue(UP, canConnectTo(level, pos, Direction.UP))
				.setValue(DOWN, canConnectTo(level, pos, Direction.DOWN));
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader levelReader, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
		if (levelReader instanceof Level level) {
			BooleanProperty prop = switch (direction) {
				case NORTH -> NORTH;
				case SOUTH -> SOUTH;
				case EAST -> EAST;
				case WEST -> WEST;
				case UP -> UP;
				case DOWN -> DOWN;
			};
			return state.setValue(prop, canConnectTo(level, pos, direction));
		}
		return state;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		VoxelShape shape = CORE;
		if (state.getValue(NORTH)) shape = Shapes.or(shape, ARM_NORTH);
		if (state.getValue(SOUTH)) shape = Shapes.or(shape, ARM_SOUTH);
		if (state.getValue(EAST)) shape = Shapes.or(shape, ARM_EAST);
		if (state.getValue(WEST)) shape = Shapes.or(shape, ARM_WEST);
		if (state.getValue(UP)) shape = Shapes.or(shape, ARM_UP);
		if (state.getValue(DOWN)) shape = Shapes.or(shape, ARM_DOWN);
		return shape;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FluidPipeBlockEntity(pos, state);
	}
}
