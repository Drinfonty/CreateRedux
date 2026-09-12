package com.drinfonty.create_redux.content.logistics.chute;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import com.drinfonty.create_redux.content.kinetics.belt.BeltBlockEntity;
import com.drinfonty.create_redux.content.logistics.TransportedItemStack;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ChuteBlockEntity extends BlockEntity implements Container, WorldlyContainer, StorageProvider<ItemStack> {
	private TransportedItemStack heldItem;
	private float fallPosition = 0.0f;

	public ChuteBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.CHUTE.get(), pos, state);
	}

	public ChuteBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public @Nullable TransportedItemStack getHeldItem() {
		return heldItem;
	}

	public float getFallPosition() {
		return fallPosition;
	}

	public void notifyUpdate() {
		if (level != null && !level.isClientSide()) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2 | 16);
		}
	}

	public void tick() {
		if (level == null || level.isClientSide()) return;

		if (heldItem == null || heldItem.stack.isEmpty()) {
			// 1. Try to pull from storage above
			BlockPos abovePos = worldPosition.above();
			Optional<StorageProvider<ItemStack>> aboveStorage = TransferUtil.getItemStorage(level, abovePos, Direction.DOWN);
			if (aboveStorage.isPresent()) {
				ItemStack extracted = TransferUtil.extractItem(aboveStorage.get(), 16, false);
				if (!extracted.isEmpty()) {
					heldItem = new TransportedItemStack(extracted);
					fallPosition = 0.0f;
					setChanged();
					notifyUpdate();
					return;
				}
			}

			// 2. Pick up loose items floating into top
			AABB pickupBox = new AABB(worldPosition.above());
			List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class, pickupBox);
			for (ItemEntity ie : itemEntities) {
				if (ie.isAlive() && !ie.getItem().isEmpty()) {
					heldItem = new TransportedItemStack(ie.getItem().copy());
					fallPosition = 0.0f;
					ie.discard();
					setChanged();
					notifyUpdate();
					return;
				}
			}
		} else {
			// Item falling down chute
			fallPosition += 0.25f;
			if (fallPosition >= 1.0f) {
				BlockPos belowPos = worldPosition.below();
				BlockEntity belowBe = level.getBlockEntity(belowPos);

				if (belowBe instanceof ChuteBlockEntity belowChute) {
					if (belowChute.heldItem == null || belowChute.heldItem.stack.isEmpty()) {
						belowChute.heldItem = this.heldItem;
						belowChute.fallPosition = 0.0f;
						belowChute.setChanged();
						belowChute.notifyUpdate();
						this.heldItem = null;
						setChanged();
						notifyUpdate();
						return;
					}
				} else if (belowBe instanceof DepotBlockEntity depot) {
					ItemStack remainder = depot.insert(0, heldItem.stack, false);
					if (remainder.isEmpty()) {
						heldItem = null;
						setChanged();
						notifyUpdate();
						return;
					} else {
						heldItem.stack = remainder;
					}
				} else if (belowBe instanceof BeltBlockEntity belt) {
					if (belt.canAcceptItem(Direction.UP)) {
						belt.addItem(heldItem.stack, Direction.UP);
						heldItem = null;
						setChanged();
						notifyUpdate();
						return;
					}
				} else {
					Optional<StorageProvider<ItemStack>> belowStorage =
							TransferUtil.getItemStorage(level, belowPos, Direction.UP);
					if (belowStorage.isPresent()) {
						ItemStack remainder = TransferUtil.insertItem(belowStorage.get(), heldItem.stack, false);
						if (remainder.isEmpty()) {
							heldItem = null;
							setChanged();
							notifyUpdate();
							return;
						} else {
							heldItem.stack = remainder;
						}
					} else if (level.isEmptyBlock(belowPos)) {
						// Eject falling out of chute
						Vec3 dropPos = Vec3.atBottomCenterOf(worldPosition).add(0, -0.1, 0);
						ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, heldItem.stack.copy());
						entity.setDeltaMovement(0, -0.1, 0);
						level.addFreshEntity(entity);
						heldItem = null;
						setChanged();
						notifyUpdate();
						return;
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
		return slot == 0 && heldItem != null ? heldItem.stack : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (slot == 0 && heldItem != null && !heldItem.stack.isEmpty()) {
			ItemStack res = heldItem.stack.split(amount);
			if (heldItem.stack.isEmpty()) {
				heldItem = null;
			}
			setChanged();
			notifyUpdate();
			return res;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if (slot == 0 && heldItem != null) {
			ItemStack res = heldItem.stack;
			heldItem = null;
			setChanged();
			return res;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot == 0) {
			heldItem = stack.isEmpty() ? null : new TransportedItemStack(stack);
			fallPosition = 0.0f;
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
		return slot == 0 && (side == Direction.UP || side == null) && (heldItem == null || heldItem.stack.isEmpty());
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
		return slot == 0 && (side == Direction.DOWN || side == null) && heldItem != null && !heldItem.stack.isEmpty();
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
		return resource;
	}

	@Override
	public ItemStack extract(int slot, long maxAmount, boolean simulate) {
		if (slot != 0 || heldItem == null || heldItem.stack.isEmpty()) return ItemStack.EMPTY;
		int count = (int) Math.min(heldItem.stack.getCount(), maxAmount);
		ItemStack extracted = heldItem.stack.copyWithCount(count);
		if (!simulate) {
			removeItem(0, count);
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
			output.putFloat("FallPos", fallPosition);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		String id = input.getStringOr("ItemId", "");
		int count = input.getIntOr("ItemCount", 0);
		fallPosition = input.getFloatOr("FallPos", 0.0f);
		if (!id.isEmpty() && count > 0) {
			BuiltInRegistries.ITEM.get(Identifier.parse(id))
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
			tag.putFloat("FallPos", fallPosition);
		}
		return tag;
	}
}
