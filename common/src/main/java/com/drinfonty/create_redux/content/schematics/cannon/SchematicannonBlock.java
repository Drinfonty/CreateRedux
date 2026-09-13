package com.drinfonty.create_redux.content.schematics.cannon;

import com.drinfonty.create_redux.platform.PlayerMsgUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SchematicannonBlock extends Block implements EntityBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

	protected static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 14, 15);

	public SchematicannonBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
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
		if (!(be instanceof SchematicannonBlockEntity cannon)) {
			return InteractionResult.PASS;
		}

		ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
		if (held.is(Items.GUNPOWDER)) {
			cannon.addGunpowder(1.0f);
			if (!player.isCreative()) {
				held.shrink(1);
			}
			PlayerMsgUtil.sendOverlay(player, Component.literal("Gunpowder added. Fuel: " + String.format("%.1f", cannon.getGunpowderFuel())));
			return InteractionResult.SUCCESS;
		}

		String progress = cannon.getPrinter() != null ? String.format("%.1f%%", cannon.getPrinter().getProgress() * 100.0f) : "No Schematic";
		PlayerMsgUtil.sendOverlay(player, Component.literal("Schematicannon: " + cannon.getState().getSerializedName() + " | Progress: " + progress + " | Gunpowder: " + String.format("%.1f", cannon.getGunpowderFuel())));
		return InteractionResult.SUCCESS;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SchematicannonBlockEntity(pos, state);
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) return null;
		return (lvl, pos, st, be) -> {
			if (be instanceof SchematicannonBlockEntity cannon && lvl instanceof ServerLevel sLevel) {
				cannon.tick(sLevel);
			}
		};
	}
}
