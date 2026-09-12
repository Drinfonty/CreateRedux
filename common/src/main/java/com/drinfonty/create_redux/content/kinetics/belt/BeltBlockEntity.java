package com.drinfonty.create_redux.content.kinetics.belt;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import com.drinfonty.create_redux.content.kinetics.KineticBlockEntity;
import com.drinfonty.create_redux.content.logistics.TransportedItemStack;
import com.drinfonty.create_redux.content.logistics.depot.DepotBlockEntity;
import com.drinfonty.create_redux.platform.transfer.StorageProvider;
import com.drinfonty.create_redux.platform.transfer.TransferUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class BeltBlockEntity extends KineticBlockEntity implements Container, WorldlyContainer, StorageProvider<ItemStack> {
	private final List<TransportedItemStack> items = new ArrayList<>();
	private static final int MAX_ITEMS = 4;

	public BeltBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.BELT.get(), pos, state);
	}

	public BeltBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public List<TransportedItemStack> getItems() {
		return items;
	}

	public boolean canAcceptItem(Direction from) {
		return items.size() < MAX_ITEMS;
	}

	public boolean addItem(ItemStack stack, Direction from) {
		if (stack.isEmpty() || items.size() >= MAX_ITEMS) return false;
		TransportedItemStack transported = new TransportedItemStack(stack);
		transported.beltPosition = 0.0f;
		transported.prevBeltPosition = 0.0f;
		transported.insertedFrom = from;
		items.add(transported);
		setChanged();
		notifyUpdate();
		return true;
	}

	public void notifyUpdate() {
		if (level != null && !level.isClientSide()) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2 | 16);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null || level.isClientSide()) return;

		float spd = getSpeed();
		if (spd == 0.0f || items.isEmpty()) return;

		BlockState state = getBlockState();
		if (!state.hasProperty(BeltBlock.HORIZONTAL_FACING)) return;

		Direction facing = state.getValue(BeltBlock.HORIZONTAL_FACING);
		Direction movementDir = spd > 0 ? facing : facing.getOpposite();
		float delta = Math.abs(spd) / (16.0f * 20.0f);

		Iterator<TransportedItemStack> it = items.iterator();
		while (it.hasNext()) {
			TransportedItemStack item = it.next();
			item.prevBeltPosition = item.beltPosition;

			if (!item.locked) {
				item.beltPosition += delta;
			}

			if (item.beltPosition >= 1.0f) {
				// Hand off to adjacent block in movement direction
				BlockPos targetPos = worldPosition.relative(movementDir);
				BlockEntity targetBe = level.getBlockEntity(targetPos);

				boolean transferred = false;

				if (targetBe instanceof BeltBlockEntity nextBelt) {
					if (nextBelt.canAcceptItem(movementDir.getOpposite())) {
						nextBelt.addItem(item.stack, movementDir.getOpposite());
						transferred = true;
					}
				} else if (targetBe instanceof DepotBlockEntity depot) {
					ItemStack remainder = depot.insert(0, item.stack, false);
					if (remainder.isEmpty()) {
						transferred = true;
					} else {
						item.stack = remainder;
						item.beltPosition = 0.99f;
					}
				} else {
					// Check for inventory via TransferUtil
					Optional<StorageProvider<ItemStack>> storageOpt =
							TransferUtil.getItemStorage(level, targetPos, movementDir.getOpposite());
					if (storageOpt.isPresent()) {
						ItemStack remainder = TransferUtil.insertItem(storageOpt.get(), item.stack, false);
						if (remainder.isEmpty()) {
							transferred = true;
						} else {
							item.stack = remainder;
							item.beltPosition = 0.99f;
						}
					} else {
						// Eject as ItemEntity into world
						Vec3 ejectPos = Vec3.atCenterOf(worldPosition).add(
								movementDir.getStepX() * 0.6,
								0.2,
								movementDir.getStepZ() * 0.6
						);
						ItemEntity entity = new ItemEntity(level, ejectPos.x, ejectPos.y, ejectPos.z, item.stack.copy());
						entity.setDeltaMovement(
								movementDir.getStepX() * 0.15,
								0.1,
								movementDir.getStepZ() * 0.15
						);
						level.addFreshEntity(entity);
						transferred = true;
					}
				}

				if (transferred) {
					it.remove();
					setChanged();
					notifyUpdate();
				}
			}
		}
	}

	// --- Container Implementation ---
	@Override
	public int getContainerSize() {
		return MAX_ITEMS;
	}

	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		if (slot >= 0 && slot < items.size()) {
			return items.get(slot).stack;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (slot >= 0 && slot < items.size()) {
			TransportedItemStack item = items.get(slot);
			ItemStack result = item.stack.split(amount);
			if (item.stack.isEmpty()) {
				items.remove(slot);
			}
			setChanged();
			notifyUpdate();
			return result;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if (slot >= 0 && slot < items.size()) {
			ItemStack result = items.remove(slot).stack;
			setChanged();
			return result;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (stack.isEmpty()) {
			if (slot >= 0 && slot < items.size()) {
				items.remove(slot);
				setChanged();
				notifyUpdate();
			}
		} else {
			if (slot >= 0 && slot < items.size()) {
				items.get(slot).stack = stack;
			} else if (items.size() < MAX_ITEMS) {
				items.add(new TransportedItemStack(stack));
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
		items.clear();
		setChanged();
		notifyUpdate();
	}

	// --- WorldlyContainer Implementation ---
	@Override
	public int[] getSlotsForFace(Direction side) {
		int[] slots = new int[items.size() < MAX_ITEMS ? items.size() + 1 : items.size()];
		for (int i = 0; i < slots.length; i++) {
			slots[i] = i;
		}
		return slots;
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
		return items.size() < MAX_ITEMS;
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
		return slot >= 0 && slot < items.size();
	}

	// --- StorageProvider<ItemStack> Implementation ---
	@Override
	public int getSlots() {
		return MAX_ITEMS;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return getItem(slot);
	}

	@Override
	public long getSlotCapacity(int slot) {
		return 64;
	}

	@Override
	public ItemStack insert(int slot, ItemStack resource, boolean simulate) {
		if (resource.isEmpty()) return ItemStack.EMPTY;
		if (items.size() >= MAX_ITEMS) return resource;

		if (!simulate) {
			addItem(resource.copy(), Direction.UP);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack extract(int slot, long maxAmount, boolean simulate) {
		if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
		TransportedItemStack item = items.get(slot);
		int count = (int) Math.min(item.stack.getCount(), maxAmount);
		ItemStack extracted = item.stack.copyWithCount(count);
		if (!simulate) {
			removeItem(slot, count);
		}
		return extracted;
	}

	// --- Serialization ---
	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("ItemCountTotal", items.size());
		for (int i = 0; i < items.size(); i++) {
			TransportedItemStack it = items.get(i);
			output.putString("Item_" + i + "_Id", BuiltInRegistries.ITEM.getKey(it.stack.getItem()).toString());
			output.putInt("Item_" + i + "_Count", it.stack.getCount());
			output.putFloat("Item_" + i + "_Pos", it.beltPosition);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		items.clear();
		int count = input.getIntOr("ItemCountTotal", 0);
		for (int i = 0; i < count; i++) {
			String id = input.getStringOr("Item_" + i + "_Id", "");
			int itemCount = input.getIntOr("Item_" + i + "_Count", 0);
			float pos = input.getFloatOr("Item_" + i + "_Pos", 0.0f);
			if (!id.isEmpty() && itemCount > 0) {
				BuiltInRegistries.ITEM.get(Identifier.parse(id))
						.map(ref -> ref.value())
						.filter(item -> item != Items.AIR)
						.ifPresent(item -> {
							TransportedItemStack tis = new TransportedItemStack(new ItemStack(item, itemCount));
							tis.beltPosition = pos;
							tis.prevBeltPosition = pos;
							items.add(tis);
						});
			}
		}
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		ListTag list = new ListTag();
		for (TransportedItemStack it : items) {
			CompoundTag itemTag = new CompoundTag();
			itemTag.putString("Id", BuiltInRegistries.ITEM.getKey(it.stack.getItem()).toString());
			itemTag.putInt("Count", it.stack.getCount());
			itemTag.putFloat("Pos", it.beltPosition);
			list.add(itemTag);
		}
		tag.put("Items", list);
		return tag;
	}
}
