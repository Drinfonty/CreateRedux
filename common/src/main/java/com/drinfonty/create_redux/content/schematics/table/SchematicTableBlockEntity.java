package com.drinfonty.create_redux.content.schematics.table;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SchematicTableBlockEntity extends BlockEntity {
	private ItemStack schematic = ItemStack.EMPTY;
	private String schematicFile = "";

	public SchematicTableBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.SCHEMATIC_TABLE.get(), pos, state);
	}

	public ItemStack getSchematic() {
		return schematic;
	}

	public void setSchematic(ItemStack schematic) {
		this.schematic = schematic;
		setChanged();
	}

	public String getSchematicFile() {
		return schematicFile;
	}

	public void setSchematicFile(String schematicFile) {
		this.schematicFile = schematicFile;
		setChanged();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("SchematicFile", schematicFile);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.schematicFile = input.getStringOr("SchematicFile", "");
	}
}
