package com.drinfonty.create_redux.content.logistics.depot;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DepotBlock extends Block implements EntityBlock {
	private static final VoxelShape SHAPE = Shapes.or(
			Block.box(2, 0, 2, 14, 11, 14),
			Block.box(1, 11, 1, 15, 13, 15)
	);

	public DepotBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;

		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof DepotBlockEntity depot) {
			ItemStack heldByPlayer = player.getItemInHand(InteractionHand.MAIN_HAND);
			ItemStack depotItem = depot.getItem(0);

			if (heldByPlayer.isEmpty()) {
				// Player has empty hand -> extract item from depot
				if (!depotItem.isEmpty()) {
					player.setItemInHand(InteractionHand.MAIN_HAND, depotItem.copy());
					depot.clearContent();
					return InteractionResult.SUCCESS;
				}
			} else {
				// Player has item -> try to insert into depot
				if (depotItem.isEmpty()) {
					ItemStack toPlace = heldByPlayer.copy();
					if (!player.isCreative()) {
						heldByPlayer.setCount(0);
					}
					depot.setItem(0, toPlace);
					return InteractionResult.SUCCESS;
				} else if (ItemStack.isSameItemSameComponents(depotItem, heldByPlayer)) {
					int max = depotItem.getMaxStackSize();
					int canAdd = Math.min(max - depotItem.getCount(), heldByPlayer.getCount());
					if (canAdd > 0) {
						depotItem.grow(canAdd);
						if (!player.isCreative()) {
							heldByPlayer.shrink(canAdd);
						}
						depot.notifyUpdate();
						return InteractionResult.SUCCESS;
					}
				}
			}
		}

		return InteractionResult.PASS;
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof DepotBlockEntity depot) {
			Containers.dropContents(level, pos, depot);
		}
		super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DepotBlockEntity(pos, state);
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return (lvl, pos, st, be) -> {
			if (be instanceof DepotBlockEntity depot) {
				depot.tick();
			}
		};
	}
}
