package com.drinfonty.create_redux.content.schematics.tools;

import com.drinfonty.create_redux.platform.PlayerMsgUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class SchematicItem extends Item {
	public SchematicItem(Properties properties) {
		super(properties);
	}

	public static String getSchematicName(ItemStack stack) {
		return "schematic.nbt";
	}

	public static BlockPos getAnchor(ItemStack stack) {
		return BlockPos.ZERO;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player != null && !context.getLevel().isClientSide()) {
			BlockPos clickedPos = context.getClickedPos().relative(context.getClickedFace());
			PlayerMsgUtil.sendOverlay(player, Component.literal("Schematic anchor aligned to: " + clickedPos.toShortString()));
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
}
