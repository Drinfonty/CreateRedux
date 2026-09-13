package com.drinfonty.create_redux.content.schematics.tools;

import com.drinfonty.create_redux.platform.PlayerMsgUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SymmetryWandItem extends Item {
	public enum SymmetryMode implements StringRepresentable {
		PLANE("plane"),
		CROSS("cross"),
		TRIPLE("triple");

		private final String name;

		SymmetryMode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}

	public SymmetryWandItem(Properties properties) {
		super(properties);
	}

	public static List<BlockPos> calculateSymmetricPositions(BlockPos placedPos, BlockPos origin, SymmetryMode mode) {
		Set<BlockPos> results = new LinkedHashSet<>();
		int dx = placedPos.getX() - origin.getX();
		int dy = placedPos.getY() - origin.getY();
		int dz = placedPos.getZ() - origin.getZ();

		// Always include the placed position
		results.add(placedPos);

		switch (mode) {
			case PLANE -> {
				results.add(new BlockPos(origin.getX() - dx, origin.getY() + dy, origin.getZ() + dz));
			}
			case CROSS -> {
				results.add(new BlockPos(origin.getX() - dx, origin.getY() + dy, origin.getZ() + dz));
				results.add(new BlockPos(origin.getX() + dx, origin.getY() + dy, origin.getZ() - dz));
				results.add(new BlockPos(origin.getX() - dx, origin.getY() + dy, origin.getZ() - dz));
			}
			case TRIPLE -> {
				for (int sx : new int[]{1, -1}) {
					for (int sy : new int[]{1, -1}) {
						for (int sz : new int[]{1, -1}) {
							results.add(new BlockPos(origin.getX() + sx * dx, origin.getY() + sy * dy, origin.getZ() + sz * dz));
						}
					}
				}
			}
		}

		return new ArrayList<>(results);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player != null && !context.getLevel().isClientSide()) {
			BlockPos clickedPos = context.getClickedPos();
			PlayerMsgUtil.sendOverlay(player, Component.literal("Symmetry mirror set at: " + clickedPos.toShortString()));
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
}
