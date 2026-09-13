package com.drinfonty.create_redux.content.trains.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrainSchedule {
	public static class Entry {
		private final String stationName;
		private final int waitDurationTicks;

		public Entry(String stationName, int waitDurationTicks) {
			this.stationName = stationName;
			this.waitDurationTicks = waitDurationTicks;
		}

		public String getStationName() {
			return stationName;
		}

		public int getWaitDurationTicks() {
			return waitDurationTicks;
		}
	}

	private final List<Entry> entries = new ArrayList<>();
	private int currentIndex = 0;
	private int currentWaitTicks = 0;

	public List<Entry> getEntries() {
		return Collections.unmodifiableList(entries);
	}

	public void addEntry(String stationName, int waitDurationTicks) {
		entries.add(new Entry(stationName, waitDurationTicks));
	}

	public Entry getCurrentEntry() {
		if (entries.isEmpty()) return null;
		return entries.get(currentIndex % entries.size());
	}

	public void advance() {
		if (!entries.isEmpty()) {
			currentIndex = (currentIndex + 1) % entries.size();
			currentWaitTicks = 0;
		}
	}

	public boolean tickWait() {
		Entry current = getCurrentEntry();
		if (current == null) return true;

		currentWaitTicks++;
		if (currentWaitTicks >= current.getWaitDurationTicks()) {
			advance();
			return true;
		}
		return false;
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	public int getCurrentWaitTicks() {
		return currentWaitTicks;
	}

	public void reset() {
		currentIndex = 0;
		currentWaitTicks = 0;
	}
}
