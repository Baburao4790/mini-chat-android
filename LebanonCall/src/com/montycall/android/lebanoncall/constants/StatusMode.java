package com.montycall.android.lebanoncall.constants;



public enum StatusMode {
	offline,
	dnd,
	xa,
	away,
	available,
	chat,
	unknown;

	StatusMode() {
		
	}

	public String toString() {
		return name();
	}

	public static StatusMode fromString(String status) {
		return StatusMode.valueOf(status);
	}

}
