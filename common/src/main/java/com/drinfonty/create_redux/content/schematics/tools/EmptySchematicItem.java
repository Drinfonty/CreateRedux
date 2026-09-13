package com.drinfonty.create_redux.content.schematics.tools;

import com.drinfonty.create_redux.platform.PlayerMsgUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EmptySchematicItem extends Item {
	public EmptySchematicItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide()) {
			PlayerMsgUtil.sendOverlay(player, Component.literal("Place in a Schematic Table to write or upload blueprints."));
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
}
