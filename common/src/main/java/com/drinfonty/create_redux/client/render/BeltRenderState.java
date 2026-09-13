package com.drinfonty.create_redux.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Render state for conveyor belts.
 */
public class BeltRenderState extends BlockEntityRenderState {
	public BlockState blockState;
	public Direction facing = Direction.NORTH;
	public Direction.Axis pulleyAxis = Direction.Axis.X;
	public float speed = 0;
	public float pulleyAngle = 0;
	public float scrollOffset = 0;
}
