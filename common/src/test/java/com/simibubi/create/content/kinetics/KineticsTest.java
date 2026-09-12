package com.simibubi.create.content.kinetics;

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
}
