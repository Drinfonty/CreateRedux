package com.drinfonty.create_redux.content.schematics.tools;

import com.drinfonty.create_redux.platform.PlayerMsgUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class SchematicAndQuillItem extends Item {
	private BlockPos firstCorner = null;

	public SchematicAndQuillItem(Properties properties) {
		super(properties);
	}

	public BlockPos getFirstCorner() {
		return firstCorner;
	}

	public void clearSelection() {
		this.firstCorner = null;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null || context.getLevel().isClientSide()) {
			return InteractionResult.PASS;
		}

		BlockPos clicked = context.getClickedPos();

		if (firstCorner == null) {
			firstCorner = clicked.immutable();
			PlayerMsgUtil.sendOverlay(player, Component.literal("First corner set at: " + firstCorner.toShortString()));
			return InteractionResult.SUCCESS;
		} else {
			BlockPos p1 = firstCorner;
			BlockPos p2 = clicked;
			int dx = Math.abs(p1.getX() - p2.getX()) + 1;
			int dy = Math.abs(p1.getY() - p2.getY()) + 1;
			int dz = Math.abs(p1.getZ() - p2.getZ()) + 1;
			firstCorner = null;
			PlayerMsgUtil.sendOverlay(player, Component.literal("Area selected: " + dx + "x" + dy + "x" + dz + " (" + (dx * dy * dz) + " blocks). Ready to save."));
			return InteractionResult.SUCCESS;
		}
	}
}
