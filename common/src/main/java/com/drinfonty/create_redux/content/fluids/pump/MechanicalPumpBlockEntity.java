package com.drinfonty.create_redux.content.fluids.pump;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import com.drinfonty.create_redux.content.kinetics.KineticBlockEntity;
import com.drinfonty.create_redux.platform.transfer.FluidStack;
import com.drinfonty.create_redux.platform.transfer.StorageProvider;
import com.drinfonty.create_redux.platform.transfer.TransferUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class MechanicalPumpBlockEntity extends KineticBlockEntity {
	public MechanicalPumpBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.MECHANICAL_PUMP.get(), pos, state);
	}

	public MechanicalPumpBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null || level.isClientSide()) return;

		float spd = getSpeed();
		if (spd == 0.0f) return;

		BlockState state = getBlockState();
		if (!state.hasProperty(MechanicalPumpBlock.FACING)) return;

		Direction facing = state.getValue(MechanicalPumpBlock.FACING);
		Direction pushDir = spd > 0 ? facing : facing.getOpposite();
		Direction pullDir = pushDir.getOpposite();

		long flowRate = (long) Math.max(1, Math.abs(spd) * 0.5f); // flow rate in mB/tick

		BlockPos pullPos = worldPosition.relative(pullDir);
		BlockPos pushPos = worldPosition.relative(pushDir);

		Optional<StorageProvider<FluidStack>> sourceOpt = TransferUtil.getFluidStorage(level, pullPos, pushDir);
		Optional<StorageProvider<FluidStack>> targetOpt = TransferUtil.getFluidStorage(level, pushPos, pullDir);

		if (sourceOpt.isPresent() && targetOpt.isPresent()) {
			StorageProvider<FluidStack> source = sourceOpt.get();
			StorageProvider<FluidStack> target = targetOpt.get();

			// Simulate extract from source
			FluidStack simulatedExtract = source.extract(0, flowRate, true);
			if (!simulatedExtract.isEmpty()) {
				// Simulate insert to target
				FluidStack remainder = target.insert(0, simulatedExtract, true);
				long canTransfer = simulatedExtract.getAmount() - remainder.getAmount();
				if (canTransfer > 0) {
					// Perform actual transfer
					FluidStack actualExtract = source.extract(0, canTransfer, false);
					if (!actualExtract.isEmpty()) {
						target.insert(0, actualExtract, false);
					}
				}
			}
		}
	}
}
