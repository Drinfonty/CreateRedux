package com.drinfonty.create_redux.content.logistics.funnel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BrassFunnelBlock extends FunnelBlock {
	public BrassFunnelBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (player.isShiftKeyDown()) {
			return super.useWithoutItem(state, level, pos, player, hitResult);
		}

		if (level.isClientSide()) return InteractionResult.SUCCESS;

		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof FunnelBlockEntity funnel) {
			ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
			if (held.isEmpty()) {
				// Clear filter
				funnel.setFilter(ItemStack.EMPTY);
				return InteractionResult.SUCCESS;
			} else {
				// Set filter
				funnel.setFilter(held.copyWithCount(1));
				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
	}
}
