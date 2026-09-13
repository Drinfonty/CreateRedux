package com.drinfonty.create_redux.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class KineticRenderState extends BlockEntityRenderState {
	public BlockState blockState;
	public Direction.Axis axis = Direction.Axis.Y;
	public float speed = 0;
	public float angle = 0;
	public boolean isLargeCog = false;
	public boolean isCogwheel = false;
}
