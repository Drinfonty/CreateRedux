package com.drinfonty.create_redux.platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.lang.reflect.Method;

/**
 * Cross-version utility bridging CompoundTag and ListTag changes between
 * Minecraft 1.21.x (direct return types) and 26.x+ (Optional / getOr variants).
 */
public final class NbtCompat {
	private static final Method GET_STRING_OR_METHOD;
	private static final Method GET_STRING_METHOD;

	private static final Method GET_INT_OR_METHOD;
	private static final Method GET_INT_METHOD;

	private static final Method GET_LONG_OR_METHOD;
	private static final Method GET_LONG_METHOD;

	private static final Method GET_LIST_OR_EMPTY_METHOD;
	private static final Method GET_LIST_METHOD;

	private static final Method GET_COMPOUND_OR_EMPTY_METHOD;
	private static final Method GET_COMPOUND_METHOD;

	private static final Method LIST_GET_COMPOUND_OR_EMPTY_METHOD;
	private static final Method LIST_GET_COMPOUND_METHOD;

	static {
		Method getStringOr = null;
		Method getString = null;
		Method getIntOr = null;
		Method getInt = null;
		Method getLongOr = null;
		Method getLong = null;
		Method getListOrEmpty = null;
		Method getList = null;
		Method getCompoundOrEmpty = null;
		Method getCompound = null;

		for (Method m : CompoundTag.class.getMethods()) {
			if (m.getName().equals("getStringOr") && m.getParameterCount() == 2) getStringOr = m;
			if (m.getName().equals("getString") && m.getParameterCount() == 1 && m.getReturnType() == String.class) getString = m;
			if (m.getName().equals("getIntOr") && m.getParameterCount() == 2) getIntOr = m;
			if (m.getName().equals("getInt") && m.getParameterCount() == 1 && m.getReturnType() == int.class) getInt = m;
			if (m.getName().equals("getLongOr") && m.getParameterCount() == 2) getLongOr = m;
			if (m.getName().equals("getLong") && m.getParameterCount() == 1 && m.getReturnType() == long.class) getLong = m;
			if (m.getName().equals("getListOrEmpty") && m.getParameterCount() == 1) getListOrEmpty = m;
			if (m.getName().equals("getList") && m.getParameterCount() == 2 && m.getReturnType() == ListTag.class) getList = m;
			if (m.getName().equals("getCompoundOrEmpty") && m.getParameterCount() == 1) getCompoundOrEmpty = m;
			if (m.getName().equals("getCompound") && m.getParameterCount() == 1 && m.getReturnType() == CompoundTag.class) getCompound = m;
		}

		Method listGetCompoundOrEmpty = null;
		Method listGetCompound = null;
		for (Method m : ListTag.class.getMethods()) {
			if (m.getName().equals("getCompoundOrEmpty") && m.getParameterCount() == 1) listGetCompoundOrEmpty = m;
			if (m.getName().equals("getCompound") && m.getParameterCount() == 1 && m.getReturnType() == CompoundTag.class) listGetCompound = m;
		}

		GET_STRING_OR_METHOD = getStringOr;
		GET_STRING_METHOD = getString;
		GET_INT_OR_METHOD = getIntOr;
		GET_INT_METHOD = getInt;
		GET_LONG_OR_METHOD = getLongOr;
		GET_LONG_METHOD = getLong;
		GET_LIST_OR_EMPTY_METHOD = getListOrEmpty;
		GET_LIST_METHOD = getList;
		GET_COMPOUND_OR_EMPTY_METHOD = getCompoundOrEmpty;
		GET_COMPOUND_METHOD = getCompound;
		LIST_GET_COMPOUND_OR_EMPTY_METHOD = listGetCompoundOrEmpty;
		LIST_GET_COMPOUND_METHOD = listGetCompound;
	}

	private NbtCompat() {}

	public static String getString(CompoundTag tag, String key, String defaultValue) {
		if (tag == null) return defaultValue;
		if (GET_STRING_OR_METHOD != null) {
			try {
				return (String) GET_STRING_OR_METHOD.invoke(tag, key, defaultValue);
			} catch (Exception ignored) {}
		}
		if (GET_STRING_METHOD != null) {
			try {
				String s = (String) GET_STRING_METHOD.invoke(tag, key);
				return s != null && !s.isEmpty() ? s : defaultValue;
			} catch (Exception ignored) {}
		}
		return defaultValue;
	}

	public static int getInt(CompoundTag tag, String key, int defaultValue) {
		if (tag == null) return defaultValue;
		if (GET_INT_OR_METHOD != null) {
			try {
				return (int) GET_INT_OR_METHOD.invoke(tag, key, defaultValue);
			} catch (Exception ignored) {}
		}
		if (GET_INT_METHOD != null) {
			try {
				return (int) GET_INT_METHOD.invoke(tag, key);
			} catch (Exception ignored) {}
		}
		return defaultValue;
	}

	public static long getLong(CompoundTag tag, String key, long defaultValue) {
		if (tag == null) return defaultValue;
		if (GET_LONG_OR_METHOD != null) {
			try {
				return (long) GET_LONG_OR_METHOD.invoke(tag, key, defaultValue);
			} catch (Exception ignored) {}
		}
		if (GET_LONG_METHOD != null) {
			try {
				return (long) GET_LONG_METHOD.invoke(tag, key);
			} catch (Exception ignored) {}
		}
		return defaultValue;
	}

	public static ListTag getList(CompoundTag tag, String key) {
		if (tag == null) return new ListTag();
		if (GET_LIST_OR_EMPTY_METHOD != null) {
			try {
				return (ListTag) GET_LIST_OR_EMPTY_METHOD.invoke(tag, key);
			} catch (Exception ignored) {}
		}
		if (GET_LIST_METHOD != null) {
			try {
				return (ListTag) GET_LIST_METHOD.invoke(tag, key, (byte) 10);
			} catch (Exception ignored) {}
		}
		return new ListTag();
	}

	public static CompoundTag getCompound(CompoundTag tag, String key) {
		if (tag == null) return new CompoundTag();
		if (GET_COMPOUND_OR_EMPTY_METHOD != null) {
			try {
				return (CompoundTag) GET_COMPOUND_OR_EMPTY_METHOD.invoke(tag, key);
			} catch (Exception ignored) {}
		}
		if (GET_COMPOUND_METHOD != null) {
			try {
				CompoundTag c = (CompoundTag) GET_COMPOUND_METHOD.invoke(tag, key);
				return c != null ? c : new CompoundTag();
			} catch (Exception ignored) {}
		}
		return new CompoundTag();
	}

	public static CompoundTag getCompoundAt(ListTag list, int index) {
		if (list == null || index < 0 || index >= list.size()) return new CompoundTag();
		if (LIST_GET_COMPOUND_OR_EMPTY_METHOD != null) {
			try {
				return (CompoundTag) LIST_GET_COMPOUND_OR_EMPTY_METHOD.invoke(list, index);
			} catch (Exception ignored) {}
		}
		if (LIST_GET_COMPOUND_METHOD != null) {
			try {
				CompoundTag c = (CompoundTag) LIST_GET_COMPOUND_METHOD.invoke(list, index);
				return c != null ? c : new CompoundTag();
			} catch (Exception ignored) {}
		}
		return new CompoundTag();
	}
}
