package com.simibubi.create;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Create {
	public static final String ID = "create";
	public static final String NAME = "Create";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	public static void init() {
		LOGGER.info("Initializing Create Redux (Multiplatform)");
	}

	public static Identifier asResource(String path) {
		return Identifier.fromNamespaceAndPath(ID, path);
	}
}
