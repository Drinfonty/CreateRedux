package com.drinfonty.create_redux.infrastructure.config;

public class AllConfigs {
	private static CKinetics serverKinetics = new CKinetics();

	public static CKinetics server() {
		return serverKinetics;
	}

	public static void setServerKinetics(CKinetics kinetics) {
		serverKinetics = kinetics;
	}
}
