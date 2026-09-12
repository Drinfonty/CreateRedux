package com.drinfonty.create_redux.content.logistics.funnel;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import com.drinfonty.create_redux.content.kinetics.belt.BeltBlockEntity;
import com.drinfonty.create_redux.content.logistics.chute.ChuteBlockEntity;
import com.drinfonty.create_redux.content.logistics.depot.DepotBlockEntity;
import com.drinfonty.create_redux.platform.transfer.StorageProvider;
import com.drinfonty.create_redux.platform.transfer.TransferUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class FunnelBlockEntity extends BlockEntity {
	private ItemStack filter = ItemStack.EMPTY;
	private int extractionLimit = 1;
	private int cooldown = 0;

	public FunnelBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.FUNNEL.get(), pos, state);
		if (state.getBlock() instanceof BrassFunnelBlock) {
			this.extractionLimit = 64;
		}
	}

	public FunnelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		if (state.getBlock() instanceof BrassFunnelBlock) {
			this.extractionLimit = 64;
		}
	}

	public ItemStack getFilter() {
		return filter;
	}

	public void setFilter(ItemStack filter) {
		this.filter = filter == null ? ItemStack.EMPTY : filter.copy();
		setChanged();
		notifyUpdate();
	}

	public int getExtractionLimit() {
		return extractionLimit;
	}

	public void setExtractionLimit(int extractionLimit) {
		this.extractionLimit = Math.max(1, Math.min(64, extractionLimit));
		setChanged();
	}

	public void notifyUpdate() {
		if (level != null && !level.isClientSide()) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2 | 16);
		}
	}

	public void tick() {
		if (level == null || level.isClientSide()) return;

		BlockState state = getBlockState();
		if (!state.hasProperty(FunnelBlock.FACING) || !state.hasProperty(FunnelBlock.EXTRACTING)) return;

		if (state.hasProperty(FunnelBlock.POWERED) && state.getValue(FunnelBlock.POWERED)) {
			return;
		}

		if (cooldown > 0) {
			cooldown--;
			return;
		}
		cooldown = 10;

		Direction facing = state.getValue(FunnelBlock.FACING);
		boolean extracting = state.getValue(FunnelBlock.EXTRACTING);

		BlockPos behindPos = worldPosition.relative(facing.getOpposite());
		BlockPos frontPos = worldPosition.relative(facing);

		if (extracting) {
			// Extract from behind, push to front
			Optional<StorageProvider<ItemStack>> behindStorage =
					TransferUtil.getItemStorage(level, behindPos, facing);
			if (behindStorage.isEmpty()) return;

			StorageProvider<ItemStack> source = behindStorage.get();
			for (int slot = 0; slot < source.getSlots(); slot++) {
				ItemStack stack = source.getStackInSlot(slot);
				if (!stack.isEmpty() && matchesFilter(stack)) {
					int toExtract = Math.min(extractionLimit, stack.getCount());
					ItemStack extracted = source.extract(slot, toExtract, false);
					if (!extracted.isEmpty()) {
						handoffExtracted(extracted, frontPos, facing);
						setChanged();
						return;
					}
				}
			}
		} else {
			// Accept from front, insert into behind
			Optional<StorageProvider<ItemStack>> behindStorage =
					TransferUtil.getItemStorage(level, behindPos, facing);
			if (behindStorage.isEmpty()) return;

			BlockEntity frontBe = level.getBlockEntity(frontPos);
			if (frontBe instanceof DepotBlockEntity depot) {
				ItemStack depotItem = depot.getItem(0);
				if (!depotItem.isEmpty() && matchesFilter(depotItem)) {
					int toMove = Math.min(extractionLimit, depotItem.getCount());
					ItemStack testInsert = TransferUtil.insertItem(behindStorage.get(), depotItem.copyWithCount(toMove), false);
					int inserted = toMove - testInsert.getCount();
					if (inserted > 0) {
						depot.removeItem(0, inserted);
						setChanged();
					}
				}
			}
		}
	}

	private boolean matchesFilter(ItemStack stack) {
		if (filter.isEmpty()) return true;
		return ItemStack.isSameItemSameComponents(filter, stack);
	}

	private void handoffExtracted(ItemStack stack, BlockPos frontPos, Direction facing) {
		BlockEntity frontBe = level.getBlockEntity(frontPos);

		if (frontBe instanceof BeltBlockEntity belt) {
			if (belt.canAcceptItem(facing.getOpposite())) {
				belt.addItem(stack, facing.getOpposite());
				return;
			}
		} else if (frontBe instanceof DepotBlockEntity depot) {
			ItemStack remainder = depot.insert(0, stack, false);
			if (remainder.isEmpty()) return;
			stack = remainder;
		} else if (frontBe instanceof ChuteBlockEntity chute) {
			ItemStack remainder = chute.insert(0, stack, false);
			if (remainder.isEmpty()) return;
			stack = remainder;
		}

		// Otherwise drop into world at front
		Vec3 dropPos = Vec3.atCenterOf(worldPosition).add(
				facing.getStepX() * 0.6,
				facing.getStepY() * 0.6,
				facing.getStepZ() * 0.6
		);
		ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, stack);
		entity.setDeltaMovement(
				facing.getStepX() * 0.1,
				0.05,
				facing.getStepZ() * 0.1
		);
		level.addFreshEntity(entity);
	}

	// --- Serialization ---
	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("ExtractionLimit", extractionLimit);
		if (!filter.isEmpty()) {
			output.putString("FilterId", BuiltInRegistries.ITEM.getKey(filter.getItem()).toString());
			output.putInt("FilterCount", filter.getCount());
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		extractionLimit = input.getIntOr("ExtractionLimit", 1);
		String filterId = input.getStringOr("FilterId", "");
		int filterCount = input.getIntOr("FilterCount", 1);
		if (!filterId.isEmpty()) {
			BuiltInRegistries.ITEM.get(Identifier.parse(filterId))
					.map(ref -> ref.value())
					.filter(item -> item != Items.AIR)
					.ifPresent(item -> this.filter = new ItemStack(item, filterCount));
		}
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		tag.putInt("ExtractionLimit", extractionLimit);
		if (!filter.isEmpty()) {
			tag.putString("FilterId", BuiltInRegistries.ITEM.getKey(filter.getItem()).toString());
		}
		return tag;
	}
}
