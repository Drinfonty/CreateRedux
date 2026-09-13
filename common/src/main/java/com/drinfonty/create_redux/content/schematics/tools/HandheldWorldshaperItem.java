package com.drinfonty.create_redux.content.schematics.tools;

import com.drinfonty.create_redux.platform.PlayerMsgUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

public class HandheldWorldshaperItem extends Item {
	public enum BrushMode implements StringRepresentable {
		CUBOID("cuboid"),
		SPHERE("sphere"),
		CYLINDER("cylinder"),
		REPLACE("replace");

		private final String name;

		BrushMode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}

	public HandheldWorldshaperItem(Properties properties) {
		super(properties);
	}

	public static Set<BlockPos> calculateAffectedBlocks(BlockPos center, BrushMode mode, int radius) {
		Set<BlockPos> positions = new HashSet<>();
		int r = Math.max(1, Math.min(16, radius));
		double rSq = r * r;

		switch (mode) {
			case CUBOID -> {
				for (int dx = -r; dx <= r; dx++) {
					for (int dy = -r; dy <= r; dy++) {
						for (int dz = -r; dz <= r; dz++) {
							positions.add(center.offset(dx, dy, dz));
						}
					}
				}
			}
			case SPHERE -> {
				for (int dx = -r; dx <= r; dx++) {
					for (int dy = -r; dy <= r; dy++) {
						for (int dz = -r; dz <= r; dz++) {
							if (dx * dx + dy * dy + dz * dz <= rSq) {
								positions.add(center.offset(dx, dy, dz));
							}
						}
					}
				}
			}
			case CYLINDER -> {
				for (int dx = -r; dx <= r; dx++) {
					for (int dy = -r; dy <= r; dy++) {
						for (int dz = -r; dz <= r; dz++) {
							if (dx * dx + dz * dz <= rSq) {
								positions.add(center.offset(dx, dy, dz));
							}
						}
					}
				}
			}
			case REPLACE -> {
				for (int dx = -r; dx <= r; dx++) {
					for (int dy = -r; dy <= r; dy++) {
						for (int dz = -r; dz <= r; dz++) {
							positions.add(center.offset(dx, dy, dz));
						}
					}
				}
			}
		}

		return positions;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		Level level = context.getLevel();

		if (player == null || !player.isCreative() || level.isClientSide()) {
			return InteractionResult.PASS;
		}

		BlockPos clickedPos = context.getClickedPos();
		BlockState heldState = player.getOffhandItem().isEmpty() ? null : net.minecraft.world.level.block.Block.byItem(player.getOffhandItem().getItem()).defaultBlockState();

		if (heldState != null && !heldState.isAir()) {
			Set<BlockPos> targets = calculateAffectedBlocks(clickedPos, BrushMode.CUBOID, 2);
			for (BlockPos pos : targets) {
				level.setBlock(pos, heldState, 3);
			}
			PlayerMsgUtil.sendOverlay(player, Component.literal("Worldshaper placed " + targets.size() + " blocks."));
			return InteractionResult.SUCCESS;
		} else {
			PlayerMsgUtil.sendOverlay(player, Component.literal("Hold a block in off-hand to paint with the Worldshaper."));
			return InteractionResult.SUCCESS;
		}
	}
}
