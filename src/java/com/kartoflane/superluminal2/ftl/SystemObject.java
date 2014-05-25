package com.kartoflane.superluminal2.ftl;

import com.kartoflane.superluminal2.components.enums.Directions;
import com.kartoflane.superluminal2.components.enums.Systems;
import com.kartoflane.superluminal2.components.interfaces.Alias;
import com.kartoflane.superluminal2.core.Database;

public class SystemObject extends GameObject implements Alias {

	private final Systems id;

	private final int levelCap;
	private int levelStart = 0;
	private int levelMax = 0;
	private boolean availableAtStart = true;

	private RoomObject room;
	private StationObject station;
	private GlowSet glowSet;

	private String interiorNamespace = null;
	private String interiorPath = null;
	private String alias = "";

	public SystemObject(Systems systemId, ShipObject ship) {
		if (systemId == null)
			throw new NullPointerException("System id must not be null.");
		id = systemId;

		setDeletable(false);

		switch (systemId) {
			case BATTERY:
				levelCap = 2;
				break;
			case PILOT:
			case DOORS:
			case SENSORS:
			case CLOAKING:
			case MEDBAY:
			case CLONEBAY:
			case TELEPORTER:
			case OXYGEN:
			case HACKING:
			case MIND:
				levelCap = 3;
				break;
			case ARTILLERY:
				levelCap = 4;
				break;
			case SHIELDS:
			case WEAPONS:
			case ENGINES:
			case DRONES:
				if (ship.isPlayerShip()) {
					levelCap = 8;
				} else {
					levelCap = 10;
				}
				break;
			default:
				levelCap = 0;
		}

		if (canContainStation()) {
			station = new StationObject(this);
			station.setSlotId(getDefaultSlotId(systemId));
			station.setSlotDirection(getDefaultSlotDirection(systemId));
		}

		if (canContainInterior())
			setInteriorNamespace(getDefaultInteriorNamespace(systemId));

		if (canContainGlow()) {
			setGlowSet(Database.DEFAULT_GLOW_SET);
		}
	}

	public void update() {
		// Nothing to do here
	}

	public Systems getSystemId() {
		return id;
	}

	public void setRoom(RoomObject newRoom) {
		room = newRoom;
	}

	public RoomObject getRoom() {
		return room;
	}

	public StationObject getStation() {
		return station;
	}

	public void setGlowSet(GlowSet set) {
		if (set == null)
			throw new IllegalArgumentException("Glow set must not be null.");
		glowSet = set;
	}

	public GlowSet getGlowSet() {
		return glowSet;
	}

	public void setInteriorNamespace(String namespace) {
		interiorNamespace = namespace;
	}

	public String getInteriorNamespace() {
		return interiorNamespace;
	}

	/**
	 * Sets path to the interior image if it is not located in the game's archives.
	 * 
	 * @param path
	 *            path to the interior image in the filesystem, or null to use dat archive location
	 */
	public void setInteriorPath(String path) {
		interiorPath = path;
	}

	/**
	 * @return Path to the interior image, or null if not set.
	 */
	public String getInteriorPath() {
		return interiorPath;
	}

	public int getLevelCap() {
		return levelCap;
	}

	public void setLevelStart(int level) {
		levelStart = level;
	}

	public int getLevelStart() {
		return levelStart;
	}

	public void setLevelMax(int level) {
		levelMax = level;
	}

	public int getLevelMax() {
		return levelMax;
	}

	public void setAvailable(boolean available) {
		availableAtStart = available;
	}

	public boolean isAvailable() {
		return availableAtStart;
	}

	/** Returns true is the system is assigned to a room, false otherwise. */
	public boolean isAssigned() {
		return room != null;
	}

	/** Returns true if the system can have a station, false otherwise. */
	public boolean canContainStation() {
		return id.canContainStation();
	}

	/** Returns true if the system can have a interior image, false otherwise */
	public boolean canContainInterior() {
		return id.canContainInterior();
	}

	/** Returns true if the system can have a glow image, false otherwise. */
	public boolean canContainGlow() {
		return id.canContainGlow();
	}

	@Override
	public String toString() {
		return id.toString();
	}

	public static int getDefaultSlotId(Systems systemId) {
		return systemId.getDefaultSlotId();
	}

	public static Directions getDefaultSlotDirection(Systems systemId) {
		return systemId.getDefaultSlotDirection();
	}

	public static String getDefaultInteriorNamespace(Systems systemId) {
		return systemId.getDefaultInteriorNamespace();
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public void setAlias(String alias) {
		this.alias = alias;
	}
}
