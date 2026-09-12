package com.simibubi.create.platform.transfer;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Objects;

/**
 * Loader-neutral representation of a fluid stack with millibucket units (1000 mB = 1 bucket).
 */
public class FluidStack {
	public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);

	private final Fluid fluid;
	private final long amount;
	private final DataComponentMap components;

	public FluidStack(Fluid fluid, long amount) {
		this(fluid, amount, DataComponentMap.EMPTY);
	}

	public FluidStack(Fluid fluid, long amount, DataComponentMap components) {
		this.fluid = Objects.requireNonNull(fluid, "fluid cannot be null");
		this.amount = Math.max(0, amount);
		this.components = Objects.requireNonNull(components, "components cannot be null");
	}

	public FluidStack(Fluid fluid, long amount, DataComponentPatch patch) {
		this(fluid, amount, PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch));
	}

	public Fluid getFluid() {
		return fluid;
	}

	public long getAmount() {
		return amount;
	}

	public DataComponentMap getComponents() {
		return components;
	}

	public DataComponentPatch getComponentsPatch() {
		if (components instanceof PatchedDataComponentMap patched) {
			return patched.asPatch();
		}
		return DataComponentPatch.EMPTY;
	}

	public boolean isEmpty() {
		return fluid == Fluids.EMPTY || amount <= 0;
	}

	public boolean isFluidEqual(FluidStack other) {
		return other != null && this.fluid == other.fluid && Objects.equals(this.components, other.components);
	}

	public FluidStack copyWithAmount(long newAmount) {
		if (newAmount <= 0) return EMPTY;
		return new FluidStack(fluid, newAmount, components);
	}
}
