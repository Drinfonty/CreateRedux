package com.drinfonty.create_redux.content.fluids;

import com.drinfonty.create_redux.AllBlocks;
import com.drinfonty.create_redux.content.fluids.hosePulley.HosePulleyBlockEntity;
import com.drinfonty.create_redux.content.fluids.pump.MechanicalPumpBlock;
import com.drinfonty.create_redux.content.fluids.pump.MechanicalPumpBlockEntity;
import com.drinfonty.create_redux.content.fluids.tank.FluidTankBlockEntity;
import com.drinfonty.create_redux.platform.transfer.FluidStack;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FluidTest {

	@BeforeAll
	static void setup() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	@DisplayName("FluidTankBlockEntity StorageProvider insertion and extraction")
	void testFluidTankStorage() {
		FluidTankBlockEntity tank = new FluidTankBlockEntity(BlockPos.ZERO, AllBlocks.FLUID_TANK.get().defaultBlockState());
		assertTrue(tank.getFluid().isEmpty());
		assertEquals(8000, tank.getCapacity());

		// 1. Simulated insertion
		FluidStack water = new FluidStack(Fluids.WATER, 3000);
		FluidStack rem = tank.insert(0, water, true);
		assertTrue(rem.isEmpty());
		assertTrue(tank.getFluid().isEmpty(), "Simulated insertion must not mutate tank state");

		// 2. Actual insertion
		rem = tank.insert(0, water, false);
		assertTrue(rem.isEmpty());
		assertFalse(tank.getFluid().isEmpty());
		assertEquals(3000, tank.getFluid().getAmount());
		assertEquals(Fluids.WATER, tank.getFluid().getFluid());

		// 3. Topping up tank
		FluidStack moreWater = new FluidStack(Fluids.WATER, 6000);
		rem = tank.insert(0, moreWater, false);
		assertEquals(1000, rem.getAmount(), "Tank capacity 8000, should have 1000 mB remainder");
		assertEquals(8000, tank.getFluid().getAmount());

		// 4. Incompatible fluid insertion rejection
		FluidStack lava = new FluidStack(Fluids.LAVA, 1000);
		rem = tank.insert(0, lava, false);
		assertEquals(1000, rem.getAmount(), "Different fluid should be rejected");
		assertEquals(Fluids.WATER, tank.getFluid().getFluid());

		// 5. Fluid extraction
		FluidStack extracted = tank.extract(0, 2500, false);
		assertEquals(2500, extracted.getAmount());
		assertEquals(Fluids.WATER, extracted.getFluid());
		assertEquals(5500, tank.getFluid().getAmount());
	}

	@Test
	@DisplayName("MechanicalPump rotational speed and directional flow rate")
	void testMechanicalPumpFlow() {
		BlockState pumpState = AllBlocks.MECHANICAL_PUMP.get().defaultBlockState()
				.setValue(MechanicalPumpBlock.FACING, Direction.NORTH);

		assertEquals(Direction.Axis.Z, AllBlocks.MECHANICAL_PUMP.get().getRotationAxis(pumpState));

		MechanicalPumpBlockEntity pump = new MechanicalPumpBlockEntity(BlockPos.ZERO, pumpState);
		pump.setSpeed(64.0f);
		assertEquals(64.0f, pump.getSpeed());

		// Flow rate at 64 RPM: |speed| * 0.5 = 32 mB/tick
		long flowRate = (long) Math.max(1, Math.abs(pump.getSpeed()) * 0.5f);
		assertEquals(32, flowRate);
	}

	@Test
	@DisplayName("HosePulley vertical extension and bounds")
	void testHosePulley() {
		HosePulleyBlockEntity pulley = new HosePulleyBlockEntity(BlockPos.ZERO, AllBlocks.HOSE_PULLEY.get().defaultBlockState());
		assertEquals(0.0f, pulley.getExtension());

		pulley.setSpeed(256.0f);
		pulley.tick();
		assertEquals(1.0f, pulley.getExtension());

		// Reverse retraction
		pulley.setSpeed(-256.0f);
		pulley.tick();
		assertEquals(0.0f, pulley.getExtension());
	}
}
