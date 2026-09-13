package com.drinfonty.create_redux.content.schematics.cannon;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import com.drinfonty.create_redux.content.schematics.Schematic;
import com.drinfonty.create_redux.content.schematics.SchematicPrinter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class SchematicannonBlockEntity extends BlockEntity {
	public enum State implements StringRepresentable {
		IDLE("idle"),
		RUNNING("running"),
		PAUSED("paused"),
		MISSING_GUNPOWDER("missing_gunpowder"),
		FINISHED("finished");

		private final String name;

		State(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}

	private State state = State.IDLE;
	private float gunpowderFuel = 0.0f;
	private static final float MAX_FUEL = 100.0f;
	private static final float FUEL_PER_BLOCK = 0.05f;

	private @Nullable SchematicPrinter printer = null;
	private BlockPos targetAnchor = BlockPos.ZERO;

	private float cannonYaw = 0.0f;
	private float cannonPitch = 0.0f;

	public SchematicannonBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.SCHEMATICANNON.get(), pos, state);
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
		setChanged();
	}

	public float getGunpowderFuel() {
		return gunpowderFuel;
	}

	public void addGunpowder(float amount) {
		this.gunpowderFuel = Math.min(MAX_FUEL, this.gunpowderFuel + amount);
		if (state == State.MISSING_GUNPOWDER && this.gunpowderFuel >= FUEL_PER_BLOCK) {
			state = State.RUNNING;
		}
		setChanged();
	}

	public @Nullable SchematicPrinter getPrinter() {
		return printer;
	}

	public void setPrinter(@Nullable SchematicPrinter printer, BlockPos targetAnchor) {
		this.printer = printer;
		this.targetAnchor = targetAnchor.immutable();
		this.state = printer != null ? State.RUNNING : State.IDLE;
		setChanged();
	}

	public BlockPos getTargetAnchor() {
		return targetAnchor;
	}

	public float getCannonYaw() {
		return cannonYaw;
	}

	public float getCannonPitch() {
		return cannonPitch;
	}

	/**
	 * Autonomous block placement tick.
	 */
	public void tick(@Nullable ServerLevel level) {
		if (printer == null || state != State.RUNNING) return;

		if (gunpowderFuel < FUEL_PER_BLOCK) {
			state = State.MISSING_GUNPOWDER;
			setChanged();
			return;
		}

		if (printer.isFinished()) {
			state = State.FINISHED;
			setChanged();
			return;
		}

		BlockPos offset = printer.getCurrentTargetOffset();
		BlockState toPlace = printer.getCurrentState();

		// Aim cannon towards target
		BlockPos worldTarget = targetAnchor.offset(offset);
		double dx = worldTarget.getX() - getBlockPos().getX();
		double dy = worldTarget.getY() - getBlockPos().getY();
		double dz = worldTarget.getZ() - getBlockPos().getZ();
		double distHorizontal = Math.sqrt(dx * dx + dz * dz);

		this.cannonYaw = (float) (Math.toDegrees(Math.atan2(-dx, dz)));
		this.cannonPitch = (float) (Math.toDegrees(Math.atan2(-dy, distHorizontal)));

		if (level != null) {
			if (!toPlace.isAir()) {
				level.setBlock(worldTarget, toPlace, 3);
			}
		}

		gunpowderFuel -= FUEL_PER_BLOCK;

		if (!printer.advance()) {
			state = State.FINISHED;
		}
		setChanged();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("CannonState", state.getSerializedName());
		output.putFloat("Gunpowder", gunpowderFuel);
		output.putLong("TargetAnchor", targetAnchor.asLong());
		output.putFloat("Yaw", cannonYaw);
		output.putFloat("Pitch", cannonPitch);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		String stateName = input.getStringOr("CannonState", "idle");
		try {
			this.state = State.valueOf(stateName.toUpperCase());
		} catch (IllegalArgumentException e) {
			this.state = State.IDLE;
		}
		this.gunpowderFuel = input.getFloatOr("Gunpowder", 0.0f);
		long anchorPacked = input.getLongOr("TargetAnchor", 0L);
		if (anchorPacked != 0L) {
			this.targetAnchor = BlockPos.of(anchorPacked);
		}
		this.cannonYaw = input.getFloatOr("Yaw", 0.0f);
		this.cannonPitch = input.getFloatOr("Pitch", 0.0f);
	}
}
