package com.drinfonty.create_redux.content.logistics.depot;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import com.drinfonty.create_redux.content.kinetics.belt.BeltBlock;
import com.drinfonty.create_redux.content.kinetics.belt.BeltBlockEntity;
import com.drinfonty.create_redux.content.logistics.TransportedItemStack;
import com.drinfonty.create_redux.platform.transfer.StorageProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class DepotBlockEntity extends BlockEntity implements Container, WorldlyContainer, StorageProvider<ItemStack> {
	private TransportedItemStack heldItem;

	public DepotBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.DEPOT.get(), pos, state);
	}

	public DepotBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public @Nullable TransportedItemStack getHeldItem() {
		return heldItem;
	}

	public void setHeldItem(@Nullable TransportedItemStack heldItem) {
		this.heldItem = heldItem;
		setChanged();
		notifyUpdate();
	}

	public void notifyUpdate() {
		if (level != null && !level.isClientSide()) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2 | 16);
		}
	}

	public void tick() {
		if (level == null || level.isClientSide()) return;

		if (heldItem != null && !heldItem.stack.isEmpty() && !heldItem.locked) {
			// Check neighboring horizontal blocks for belts moving away
			for (Direction dir : Direction.Plane.HORIZONTAL) {
				BlockPos neighborPos = worldPosition.relative(dir);
				BlockEntity be = level.getBlockEntity(neighborPos);
				if (be instanceof BeltBlockEntity belt) {
					BlockState beltState = belt.getBlockState();
					if (beltState.hasProperty(BeltBlock.HORIZONTAL_FACING)) {
						Direction beltFacing = beltState.getValue(BeltBlock.HORIZONTAL_FACING);
						float speed = belt.getSpeed();
						Direction movementDir = speed > 0 ? beltFacing : beltFacing.getOpposite();
						// If belt is moving away from this depot and aligned
						if (movementDir == dir && belt.canAcceptItem(dir.getOpposite())) {
							belt.addItem(heldItem.stack.copy(), dir.getOpposite());
							heldItem = null;
							setChanged();
							notifyUpdate();
							break;
						}
					}
				}
			}
		}
	}

	// --- Container Implementation ---
	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return heldItem == null || heldItem.stack.isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		if (slot == 0 && heldItem != null) {
			return heldItem.stack;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (slot == 0 && heldItem != null && !heldItem.stack.isEmpty()) {
			ItemStack result = heldItem.stack.split(amount);
			if (heldItem.stack.isEmpty()) {
				heldItem = null;
			}
			setChanged();
			notifyUpdate();
			return result;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if (slot == 0 && heldItem != null) {
			ItemStack result = heldItem.stack;
			heldItem = null;
			setChanged();
			return result;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot == 0) {
			if (stack.isEmpty()) {
				heldItem = null;
			} else {
				heldItem = new TransportedItemStack(stack);
			}
			setChanged();
			notifyUpdate();
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		heldItem = null;
		setChanged();
		notifyUpdate();
	}

	// --- WorldlyContainer Implementation ---
	@Override
	public int[] getSlotsForFace(Direction side) {
		return new int[]{0};
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
		return slot == 0 && (heldItem == null || heldItem.stack.isEmpty() ||
				(ItemStack.isSameItemSameComponents(heldItem.stack, stack) &&
						heldItem.stack.getCount() + stack.getCount() <= heldItem.stack.getMaxStackSize()));
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
		return slot == 0 && heldItem != null && !heldItem.stack.isEmpty();
	}

	// --- StorageProvider<ItemStack> Implementation ---
	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return getItem(slot);
	}

	@Override
	public long getSlotCapacity(int slot) {
		if (slot == 0 && heldItem != null && !heldItem.stack.isEmpty()) {
			return Math.max(heldItem.stack.getItem().getDefaultMaxStackSize(), heldItem.stack.getMaxStackSize());
		}
		return 64;
	}

	@Override
	public ItemStack insert(int slot, ItemStack resource, boolean simulate) {
		if (slot != 0 || resource.isEmpty()) return resource;

		if (heldItem == null || heldItem.stack.isEmpty()) {
			if (!simulate) {
				setItem(0, resource.copy());
			}
			return ItemStack.EMPTY;
		}

		if (ItemStack.isSameItemSameComponents(heldItem.stack, resource)) {
			int max = Math.max(heldItem.stack.getItem().getDefaultMaxStackSize(), heldItem.stack.getMaxStackSize());
			int canAdd = max - heldItem.stack.getCount();
			if (canAdd <= 0) return resource;

			int added = Math.min(canAdd, resource.getCount());
			if (!simulate) {
				heldItem.stack.grow(added);
				setChanged();
				notifyUpdate();
			}
			if (added >= resource.getCount()) {
				return ItemStack.EMPTY;
			}
			ItemStack remainder = resource.copy();
			remainder.shrink(added);
			return remainder;
		}

		return resource;
	}

	@Override
	public ItemStack extract(int slot, long maxAmount, boolean simulate) {
		if (slot != 0 || heldItem == null || heldItem.stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		int toExtract = (int) Math.min(heldItem.stack.getCount(), maxAmount);
		if (toExtract <= 0) return ItemStack.EMPTY;

		ItemStack extracted = heldItem.stack.copyWithCount(toExtract);
		if (!simulate) {
			removeItem(0, toExtract);
		}
		return extracted;
	}

	// --- Serialization ---
	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (heldItem != null && !heldItem.stack.isEmpty()) {
			output.putString("ItemId", BuiltInRegistries.ITEM.getKey(heldItem.stack.getItem()).toString());
			output.putInt("ItemCount", heldItem.stack.getCount());
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		String itemId = input.getStringOr("ItemId", "");
		int count = input.getIntOr("ItemCount", 0);
		if (!itemId.isEmpty() && count > 0) {
			BuiltInRegistries.ITEM.get(Identifier.parse(itemId))
					.map(ref -> ref.value())
					.filter(item -> item != Items.AIR)
					.ifPresent(item -> this.heldItem = new TransportedItemStack(new ItemStack(item, count)));
		}
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		if (heldItem != null && !heldItem.stack.isEmpty()) {
			tag.putString("ItemId", BuiltInRegistries.ITEM.getKey(heldItem.stack.getItem()).toString());
			tag.putInt("ItemCount", heldItem.stack.getCount());
		}
		return tag;
	}
}
