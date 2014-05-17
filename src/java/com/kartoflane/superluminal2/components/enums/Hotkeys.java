package com.kartoflane.superluminal2.components.enums;

public enum Hotkeys {
	// Tools
	POINTER_TOOL,
	CREATE_TOOL,
	GIB_TOOL,
	PROPERTIES_TOOL,
	IMAGES_TOOL,
	OVERVIEW_TOOL,
	ROOM_TOOL,
	DOOR_TOOL,
	MOUNT_TOOL,
	STATION_TOOL,

	// Commands
	DELETE,
	PIN,
	NEW_SHIP,
	LOAD_SHIP,
	SAVE_SHIP,
	CLOSE_SHIP,
	MANAGE_MOD,
	SETTINGS,
	UNDO,
	REDO,
	CLOAK,

	// View
	TOGGLE_GRID,
	SHOW_ANCHOR,
	SHOW_MOUNTS,
	SHOW_ROOMS,
	SHOW_DOORS,
	SHOW_STATIONS,
	SHOW_HULL,
	SHOW_FLOOR,
	SHOW_SHIELD;

	@Override
	public String toString() {
		return name();
	}
}