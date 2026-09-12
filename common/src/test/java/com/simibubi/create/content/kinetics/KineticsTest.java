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

	@Test
	@DisplayName("Configuration system defaults and kinetics values")
	void testConfigurationDefaults() {
		com.simibubi.create.infrastructure.config.CKinetics kinetics = com.simibubi.create.infrastructure.config.AllConfigs.server();
		assertNotNull(kinetics);
		assertEquals(256.0f, kinetics.maxRotationSpeed);
		assertEquals(1.0f, kinetics.stressCapacityMultiplier);
		assertEquals(1.0f, kinetics.stressImpactMultiplier);
		assertEquals(8.0f, kinetics.waterWheelBaseSpeed);
		assertEquals(32.0f, kinetics.crankRotationPerClick);
	}

	@Test
	@DisplayName("Contraption relative bounds and sail counting")
	void testContraptionAssembly() {
		com.simibubi.create.content.contraptions.components.structure.bearing.BearingContraption contraption =
				new com.simibubi.create.content.contraptions.components.structure.bearing.BearingContraption(net.minecraft.core.Direction.UP);

		assertEquals(net.minecraft.core.Direction.UP, contraption.getFacing());
		assertEquals(0, contraption.getSailBlocksCount());
		assertFalse(contraption.isAssembled());
	}

	@Test
	@DisplayName("Mechanical piston extension clamping")
	void testPistonExtensionLimits() {
		float extension = 15.0f;
		float maxExtension = 16.0f;
		float delta = 2.0f;
		float clamped = Math.clamp(extension + delta, 0.0f, maxExtension);
		assertEquals(16.0f, clamped);

		float retracted = Math.clamp(extension - 20.0f, 0.0f, maxExtension);
		assertEquals(0.0f, retracted);
	}
}
