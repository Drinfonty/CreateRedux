package com.drinfonty.create_redux.content.contraptions.components.structure.piston;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PistonExtensionPoleBlock extends RotatedPillarBlock {
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

	protected static final VoxelShape Y_SHAPE = Block.box(6, 0, 6, 10, 16, 10);
	protected static final VoxelShape Z_SHAPE = Block.box(6, 6, 0, 10, 10, 16);
	protected static final VoxelShape X_SHAPE = Block.box(0, 6, 6, 16, 10, 10);

	public PistonExtensionPoleBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.Y));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(AXIS)) {
			case X -> X_SHAPE;
			case Z -> Z_SHAPE;
			default -> Y_SHAPE;
		};
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(AXIS, context.getNearestLookingDirection().getAxis());
	}
}
