package com.kartoflane.superluminal2.ftl;

import com.kartoflane.superluminal2.components.enums.Directions;
import com.kartoflane.superluminal2.components.enums.Systems;
import com.kartoflane.superluminal2.components.interfaces.Alias;
import com.kartoflane.superluminal2.components.interfaces.WeaponLike;
import com.kartoflane.superluminal2.db.Database;
import com.kartoflane.superluminal2.mvc.controllers.GlowController;


public class SystemObject extends GameObject implements Alias
{
	private final Systems id;

	private int levelCap;
	private int levelStart = 0;
	private int levelMax = 0;
	private boolean availableAtStart = true;

	private RoomObject room;
	/** Only used by systems with a mannable station */
	private StationObject station;
	/** Only used by systems with a mannable station */
	private GlowObject glowObject;
	/** Only used by artillery systems */
	private WeaponLike weapon = null;

	private String interiorNamespace = null;
	private String interiorPath = null;
	private String alias = "";

	/** Whether this player ship system is using the optional max level */
	private boolean usingMax = false;


	public SystemObject( Systems systemId )
	{
		if ( systemId == null )
			throw new IllegalArgumentException( "System id must not be null." );
		id = systemId;

		setDeletable( false );

		if ( canContainStation() ) {
			station = new StationObject( this );
			station.setSlotId( getDefaultSlotId( systemId ) );
			station.setSlotDirection( getDefaultSlotDirection( systemId ) );
		}

		if ( canContainInterior() )
			setInteriorNamespace( getDefaultInteriorNamespace( systemId ) );

		if ( systemId == Systems.CLOAKING ) {
			glowObject = new GlowObject( "cloaking" );
		}
		else if ( canContainGlow() ) {
			String glow = id.getDefaultInteriorNamespace();

			glow = glow.replace( "room_", "" );
			GlowObject obj = Database.getInstance().getGlow( glow );
			if ( obj != null ) {
				glowObject = obj;
			}
			else {
				glowObject = Database.DEFAULT_GLOW_OBJ;
			}
		}
		else if ( canContainStation() ) {
			glowObject = Database.DEFAULT_GLOW_OBJ;
		}

		Database db = Database.getInstance();
		if ( db != null && systemId == Systems.ARTILLERY )
			weapon = db.getWeapon( "ARTILLERY_FED" );
	}

	/**
	 * Creates a new system object from an archetype system object.
	 * Supply the archetype from database via <br> {@link Database#getSystem(Systems)}
	 * @param systemObject the system to be copied
	 */
	@SuppressWarnings( "CopyConstructorMissesField" )
	public SystemObject( SystemObject systemObject ) {
		this( systemObject.id );

		this.levelCap = systemObject.levelCap;
		this.levelStart = systemObject.levelStart;
	}

	public void update()
	{
		// Nothing to do here
	}

	public Systems getSystemId()
	{
		return id;
	}

	public void setRoom( RoomObject newRoom )
	{
		room = newRoom;
	}

	public RoomObject getRoom()
	{
		return room;
	}

	/**
	 * Sets the weapon associated with this system.<br>
	 * 
	 * Only used by {@link Systems#ARTILLERY artillery} systems.
	 */
	public void setWeapon( WeaponLike newWeapon )
	{
		weapon = newWeapon;
	}

	/**
	 * Only used by {@link Systems#ARTILLERY artillery} systems.
	 * 
	 * @return the weapon associated with this system.
	 */
	public WeaponLike getWeapon()
	{
		return weapon;
	}

	/**
	 * Only used by {@link Systems#canContainStation() mannable} systems.
	 * 
	 * @return the station associated with this system
	 */
	public StationObject getStation()
	{
		return station;
	}

	/**
	 * Only used by {@link Systems#canContainStation() mannable} systems.
	 * 
	 * @return the glow image associated with this system. Glow images appear when the station is manned.
	 */
	public GlowObject getGlow()
	{
		return glowObject;
	}

	/**
	 * Only used by {@link Systems#canContainGlow() systems that can contain glow}.<br>
	 * <br>
	 * <b>This method should ONLY be used during ship loading.</b> Use {@link GlowController#setGlowSet(GlowSet)} instead.
	 */
	public void setGlow( GlowObject glow )
	{
		if ( glow == null )
			throw new IllegalArgumentException( "Glow must not be null." );
		glowObject = glow;
	}

	public void setInteriorNamespace( String namespace )
	{
		interiorNamespace = namespace;
	}

	public String getInteriorNamespace()
	{
		return interiorNamespace;
	}

	/**
	 * Sets path to the interior image if it is not located in the game's archives.
	 * 
	 * @param path
	 *            path to the interior image in the filesystem, or null to use dat archive location
	 */
	public void setInteriorPath( String path )
	{
		interiorPath = path;
	}

	/**
	 * @return Path to the interior image, or null if not set.
	 */
	public String getInteriorPath()
	{
		return interiorPath;
	}

	public void setLevelCap( int level ) {
		levelCap = level;
	}

	public int getLevelCap()
	{
		return levelCap;
	}

	public void setLevelStart( int level )
	{
		levelStart = level;
	}

	public int getLevelStart()
	{
		return levelStart;
	}

	public void setLevelMax( int level )
	{
		levelMax = level;
	}

	public int getLevelMax()
	{
		return levelMax;
	}

	public void setAvailable( boolean available )
	{
		availableAtStart = available;
	}

	public boolean isAvailable()
	{
		return availableAtStart;
	}

	/** Returns true is the system is assigned to a room, false otherwise. */
	public boolean isAssigned()
	{
		return room != null && !room.isDeleted();
	}

	/** Returns true if the system can have a station, false otherwise. */
	public boolean canContainStation()
	{
		return id.canContainStation();
	}

	/** Returns true if the system can have a interior image, false otherwise */
	public boolean canContainInterior()
	{
		return id.canContainInterior();
	}

	/** Returns true if the system can have a glow image, false otherwise. */
	public boolean canContainGlow()
	{
		return id.canContainGlow();
	}

	@Override
	public String toString()
	{
		return id.toString();
	}

	public static int getDefaultSlotId( Systems systemId )
	{
		return systemId.getDefaultSlotId();
	}

	public static Directions getDefaultSlotDirection( Systems systemId )
	{
		return systemId.getDefaultSlotDirection();
	}

	public static String getDefaultInteriorNamespace( Systems systemId )
	{
		return systemId.getDefaultInteriorNamespace();
	}

	@Override
	public String getAlias()
	{
		return alias;
	}

	@Override
	public void setAlias( String alias )
	{
		this.alias = alias;
	}

	public boolean isUsingMax() {
		return usingMax;
	}

	public void setUsingMax( boolean usingMax ) {
		this.usingMax = usingMax;
	}
}
