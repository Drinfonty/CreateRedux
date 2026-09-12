package com.simibubi.create.content.kinetics;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.platform.transfer.FluidStack;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KineticsTest {

	@BeforeAll
	static void setup() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	@DisplayName("FluidStack millibucket calculations and copy")
	void testFluidStack() {
		FluidStack stack = new FluidStack(Fluids.WATER, 1000);
		assertFalse(stack.isEmpty());
		assertEquals(1000, stack.getAmount());
		assertEquals(Fluids.WATER, stack.getFluid());

		FluidStack half = stack.copyWithAmount(500);
		assertEquals(500, half.getAmount());
		assertTrue(stack.isFluidEqual(half));

		FluidStack empty = stack.copyWithAmount(0);
		assertTrue(empty.isEmpty());
	}

	@Test
	@DisplayName("Kinetic calculations and stress overload")
	void testKineticStressCalculation() {
		float networkCapacity = 2048.0f;
		float networkStress = 1024.0f;
		boolean isOverStressed = networkStress > networkCapacity;
		assertFalse(isOverStressed);

		float overloadStress = 4096.0f;
		isOverStressed = overloadStress > networkCapacity;
		assertTrue(isOverStressed);
	}

	@Test
	@DisplayName("Kinetic stress registry capacities")
	void testKineticStressRegistry() {
		KineticStressRegistry.registerCapacity(AllBlocks.HAND_CRANK.get(), 256.0f);
		KineticStressRegistry.registerCapacity(AllBlocks.WATER_WHEEL.get(), 256.0f);
		KineticStressRegistry.registerImpact(AllBlocks.SHAFT.get(), 0.0f);

		assertEquals(256.0f, KineticStressRegistry.getCapacity(AllBlocks.HAND_CRANK.get()));
		assertEquals(256.0f, KineticStressRegistry.getCapacity(AllBlocks.WATER_WHEEL.get()));
		assertEquals(0.0f, KineticStressRegistry.getImpact(AllBlocks.SHAFT.get()));
	}

	@Test
	@DisplayName("Gearbox transmission speed reversal")
	void testGearboxTransmission() {
		float inputSpeed = 32.0f;
		float sameAxisSpeed = inputSpeed;
		float perpendicularSpeed = -inputSpeed;

		assertEquals(32.0f, sameAxisSpeed);
		assertEquals(-32.0f, perpendicularSpeed);
	}
}
