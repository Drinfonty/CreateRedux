package com.drinfonty.create_redux.content.trains.station;

import com.drinfonty.create_redux.platform.PlayerMsgUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class StationBlock extends Block implements EntityBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty ASSEMBLING = BooleanProperty.create("assembling");

	protected static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 14, 16);

	public StationBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState()
				.setValue(FACING, Direction.NORTH)
				.setValue(ASSEMBLING, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, ASSEMBLING);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof StationBlockEntity station)) {
			return InteractionResult.PASS;
		}

		if (player.isShiftKeyDown()) {
			boolean newMode = !station.isAssembling();
			station.setAssembling(newMode);
			level.setBlock(pos, state.setValue(ASSEMBLING, newMode), 3);
			PlayerMsgUtil.sendOverlay(player, Component.literal("Station Mode: " + (newMode ? "Assembly Mode" : "Normal Mode")));
			return InteractionResult.SUCCESS;
		}

		if (station.isAssembling()) {
			boolean assembled = station.assembleTrain(level);
			if (assembled) {
				PlayerMsgUtil.sendOverlay(player, Component.literal("Train assembled at " + station.getStationName() + "!"));
			} else {
				PlayerMsgUtil.sendOverlay(player, Component.literal("Assembly failed: No bogeys detected on track in front of station."));
			}
			return InteractionResult.SUCCESS;
		}

		if (station.isTrainPresent()) {
			station.disassembleTrain(level);
			PlayerMsgUtil.sendOverlay(player, Component.literal("Train disassembled at " + station.getStationName()));
			return InteractionResult.SUCCESS;
		}

		PlayerMsgUtil.sendOverlay(player, Component.literal("Station: " + station.getStationName() + " (Ready)"));
		return InteractionResult.SUCCESS;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new StationBlockEntity(pos, state);
	}
}
