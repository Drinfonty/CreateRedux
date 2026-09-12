package com.drinfonty.create_redux.content.fluids.tank;

import com.drinfonty.create_redux.platform.transfer.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class FluidTankBlock extends Block implements EntityBlock {
	public static final BooleanProperty TOP = BooleanProperty.create("top");
	public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

	public FluidTankBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(TOP, true).setValue(BOTTOM, true));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(TOP, BOTTOM);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		boolean isAboveTank = level.getBlockState(pos.above()).is(this);
		boolean isBelowTank = level.getBlockState(pos.below()).is(this);
		return defaultBlockState().setValue(TOP, !isAboveTank).setValue(BOTTOM, !isBelowTank);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
		if (direction == Direction.UP) {
			return state.setValue(TOP, !neighborState.is(this));
		} else if (direction == Direction.DOWN) {
			return state.setValue(BOTTOM, !neighborState.is(this));
		}
		return state;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof FluidTankBlockEntity tank) {
			// Try bucket interaction first
			if (tank.handleBucket(player, InteractionHand.MAIN_HAND)) {
				return InteractionResult.SUCCESS;
			}

			// If empty hand, display fluid status
			if (!level.isClientSide() && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
				FluidStack fluid = tank.getFluid();
				if (fluid.isEmpty()) {
					player.sendOverlayMessage(Component.literal("Tank is empty (0 / " + tank.getCapacity() + " mB)"));
				} else {
					player.sendOverlayMessage(Component.literal("Fluid: " + fluid.getAmount() + " / " + tank.getCapacity() + " mB"));
				}
				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FluidTankBlockEntity(pos, state);
	}
}
