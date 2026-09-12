package com.drinfonty.create_redux.content.kinetics.belt;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import com.drinfonty.create_redux.content.kinetics.KineticBlock;
import com.drinfonty.create_redux.content.logistics.TransportedItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BeltBlock extends KineticBlock implements EntityBlock {
	public static final EnumProperty<Direction> HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<BeltSlope> SLOPE = EnumProperty.create("slope", BeltSlope.class);
	public static final EnumProperty<BeltPart> PART = EnumProperty.create("part", BeltPart.class);
	public static final BooleanProperty CASING = BooleanProperty.create("casing");

	private static final VoxelShape FLAT_SHAPE = Block.box(0, 0, 0, 16, 8, 16);

	public BeltBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState()
				.setValue(HORIZONTAL_FACING, Direction.NORTH)
				.setValue(SLOPE, BeltSlope.HORIZONTAL)
				.setValue(PART, BeltPart.START)
				.setValue(CASING, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING, SLOPE, PART, CASING);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction facing = context.getHorizontalDirection();
		return defaultBlockState()
				.setValue(HORIZONTAL_FACING, facing)
				.setValue(SLOPE, BeltSlope.HORIZONTAL)
				.setValue(PART, BeltPart.START)
				.setValue(CASING, false);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return FLAT_SHAPE;
	}

	@Override
	public Direction.Axis getRotationAxis(BlockState state) {
		Direction facing = state.getValue(HORIZONTAL_FACING);
		return facing.getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		BeltPart part = state.getValue(PART);
		if (part == BeltPart.MIDDLE) return false;
		return face.getAxis() == getRotationAxis(state);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;

		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof BeltBlockEntity belt) {
			ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
			List<TransportedItemStack> items = belt.getItems();

			if (held.isEmpty()) {
				// Player takes item off belt
				if (!items.isEmpty()) {
					TransportedItemStack tis = items.remove(items.size() - 1);
					player.setItemInHand(InteractionHand.MAIN_HAND, tis.stack);
					belt.setChanged();
					belt.notifyUpdate();
					return InteractionResult.SUCCESS;
				}
			} else {
				// Player places item onto belt
				if (belt.canAcceptItem(Direction.UP)) {
					ItemStack toAdd = held.copy();
					if (!player.isCreative()) {
						held.shrink(1);
						toAdd.setCount(1);
					}
					belt.addItem(toAdd, Direction.UP);
					return InteractionResult.SUCCESS;
				}
			}
		}

		return InteractionResult.PASS;
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof BeltBlockEntity belt) {
			Containers.dropContents(level, pos, belt);
		}
		super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BeltBlockEntity(pos, state);
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return (lvl, pos, st, be) -> {
			if (be instanceof BeltBlockEntity belt) {
				belt.tick();
			}
		};
	}
}
