package com.drinfonty.create_redux.content.trains.track;

import com.drinfonty.create_redux.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrackBlockEntity extends BlockEntity {
	private final List<BlockPos> bezierConnections = new ArrayList<>();

	public TrackBlockEntity(BlockPos pos, BlockState state) {
		super(AllBlockEntityTypes.TRACK.get(), pos, state);
	}

	public List<BlockPos> getBezierConnections() {
		return Collections.unmodifiableList(bezierConnections);
	}

	public void addBezierConnection(BlockPos target) {
		if (!bezierConnections.contains(target)) {
			bezierConnections.add(target.immutable());
			setChanged();
		}
	}

	public void removeBezierConnection(BlockPos target) {
		if (bezierConnections.remove(target)) {
			setChanged();
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("BezierCount", bezierConnections.size());
		for (int i = 0; i < bezierConnections.size(); i++) {
			BlockPos target = bezierConnections.get(i);
			output.putLong("BezierTarget_" + i, target.asLong());
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		bezierConnections.clear();
		int count = input.getIntOr("BezierCount", 0);
		for (int i = 0; i < count; i++) {
			long packed = input.getLongOr("BezierTarget_" + i, 0L);
			if (packed != 0L) {
				bezierConnections.add(BlockPos.of(packed));
			}
		}
	}
}
