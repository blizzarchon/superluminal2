package com.kartoflane.superluminal2.ftl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.kartoflane.superluminal2.components.enums.BoardingStrategies;
import com.kartoflane.superluminal2.components.enums.Images;
import com.kartoflane.superluminal2.components.enums.PlayerShipBlueprints;
import com.kartoflane.superluminal2.components.enums.Systems;
import com.kartoflane.superluminal2.components.interfaces.CrewLike;
import com.kartoflane.superluminal2.components.interfaces.DroneLike;
import com.kartoflane.superluminal2.components.interfaces.WeaponLike;
import com.kartoflane.superluminal2.db.Database;
import com.kartoflane.superluminal2.mvc.controllers.AbstractController;
import com.kartoflane.superluminal2.utils.Utils;


public class ShipObject extends GameObject
{
	private boolean isPlayer = false;
	private String blueprintName = PlayerShipBlueprints.HARD.toString();
	private String layout = "myship";
	private String layoutSlot = "A";
	private String img = "myship";

	private IDeferredText shipClass = new DefaultDeferredText("text_" + blueprintName + "_class", "Ship Class" );
	private IDeferredText shipName = new DefaultDeferredText( "text_" + blueprintName + "_name", "The Nameless One" );
	private IDeferredText shipDescription = new DefaultDeferredText( "text_" + blueprintName + "_desc", "This ship is completely devoid of any description whatsoever!" );

	private TreeSet<RoomObject> rooms;
	private HashSet<DoorObject> doors;
	private TreeSet<MountObject> mounts;
	private TreeSet<GibObject> gibs;

	private HashMap<Systems, ArrayList<SystemObject>> systemMap;
	private HashMap<Images, ImageObject> imageMap;
	private ArrayList<CrewLike> crewList;
	private int crewMin;
	private int crewMax;
	private CrewLike enemyCrew;

	private ArrayList<AugmentObject> augments;
	private ArrayList<AugmentObject> hiddenAugments;
	private ArrayList<WeaponLike> weapons;
	private ArrayList<DroneLike> drones;
	private boolean weaponByList = false;
	// TODO: private boolean listPerSlot = false;
	private boolean droneByList = false;
	private WeaponList weaponList = Database.DEFAULT_WEAPON_LIST;
	private DroneList droneList = Database.DEFAULT_DRONE_LIST;
	private BoardingStrategies boardingAI = BoardingStrategies.SABOTAGE;

	private int xOffset = 0;
	private int yOffset = 0;
	private int horizontal = 0;
	private int vertical = 0;
	private Rectangle ellipse = new Rectangle( 0, 0, 150, 150 );

	private Point hullOffset = new Point( 0, 0 );
	private Point hullSize = new Point( 0, 0 );
	private Point cloakOffset = new Point( 0, 0 );
	private Point floorOffset = new Point( 0, 0 );

	private int weaponSlots = 0;
	private int droneSlots = 0;
	private int hiddenAugmentsNumber = 0;
	private int missiles = 0;
	private int droneParts = 0;
	private int hullHealth = 0;
	private int maxPower = 0;
	private int crewCap = 8;
	private int systemCap = 8;

	private int minSector = 1;
	private int maxSector = 8;


	private ShipObject()
	{
		setDeletable( false );

		rooms = new TreeSet<RoomObject>();
		doors = new HashSet<DoorObject>();
		mounts = new TreeSet<MountObject>();
		gibs = new TreeSet<GibObject>();
		systemMap = new HashMap<Systems, ArrayList<SystemObject>>();
		imageMap = new HashMap<Images, ImageObject>();
		augments = new ArrayList<AugmentObject>();
		hiddenAugments = new ArrayList<AugmentObject>();
		crewList = new ArrayList<CrewLike>();
		crewMin = 0;
		crewMax = 0;

		weapons = new ArrayList<WeaponLike>();
		drones = new ArrayList<DroneLike>();

		for ( int i = 0; i < 8; i++ ) {
			weapons.add( Database.DEFAULT_WEAPON_OBJ );
			drones.add( Database.DEFAULT_DRONE_OBJ );
		}

		for ( Images image : Images.values() ) {
			ImageObject object = new ImageObject();
			object.setAlias( image.name().toLowerCase() );
			imageMap.put( image, object );
		}
	}

	public ShipObject( boolean isPlayer )
	{
		this();

		this.isPlayer = isPlayer;

		for ( int i = 0; i < 3; i++ ) {
			augments.add( Database.DEFAULT_AUGMENT_OBJ );
		}

		Database db = Database.getInstance();

		for ( Systems system : Systems.values() ) {
			ArrayList<SystemObject> list = new ArrayList<SystemObject>();
			systemMap.put( system, list );
			if ( system.equals( Systems.EMPTY ) ) {
				SystemObject empty = new SystemObject( Systems.EMPTY );
				empty.setLevelCap( 0 );
				add( empty );
			} else {
				SystemObject sys = db.getSystem( system );
				if ( sys != null ) {
					add( new SystemObject( sys ) );
				}
				else { // system not in database, so guess levels
					sys = new SystemObject( system );
					sys.setLevelStart( 1 );
					sys.setLevelCap( 3 );
					add( sys );
				}
			}
		}

		if ( !isPlayer ) {
			ImageObject shieldObject = imageMap.get( Images.SHIELD );
			shieldObject.setImagePath( "db:img/ship/enemy_shields.png" );
			blueprintName = "NEW_ENEMY_SHIP";
			weaponByList = true;
			droneByList = true;
			enemyCrew = Database.DEFAULT_CREW_OBJ;
		}
		else {
			for ( int i = 0; i < crewCap; i++ ) {
				crewList.add( Database.DEFAULT_CREW_OBJ );
			}
		}
		
		if ( blueprintName.endsWith( "_2" ) )
		{
			layoutSlot = "B";
		}
		else if ( blueprintName.endsWith( "_3" ) )
		{
			layoutSlot = "C";
		}

		update();
	}

	public void update()
	{
		shipClass = shipClass.derive( "text_" + blueprintName + "_class" );
		shipName = shipName.derive( "text_" + blueprintName + "_name" );
		shipDescription = shipDescription.derive( "text_" + blueprintName + "_desc" );
	}

	public boolean isPlayerShip()
	{
		return isPlayer;
	}

	public void setBlueprintName( String name )
	{
		if ( blueprintName == null )
			throw new IllegalArgumentException( "Blueprint name must not be null." );
		blueprintName = name;
	}

	public String getBlueprintName()
	{
		return blueprintName;
	}

	/**
	 * Sets the ship layout's namespace ('layout' attribute)
	 */
	public void setLayout( String layout )
	{
		if ( layout == null )
			throw new IllegalArgumentException( "Layout namespace must not be null." );
		this.layout = layout;
	}
	
	public void setLayoutSlot( String layoutSlot)
	{
		this.layoutSlot = layoutSlot;
	}

	/**
	 * @return ship layout's namespace ('layout' attribute)
	 */
	public String getLayout()
	{
		return layout;
	}
	
	public String getLayoutSlot()
	{
		return layoutSlot;
	}

	/**
	 * Returns a version of the ship's blueprint name where the suffix accurately reflects the given slot.
	 */
	public String getSlotResolvedName() {
		boolean wrongSuffixOnSlotA = layoutSlot.equals( "A" ) && ( blueprintName.endsWith( "_2" ) || blueprintName.endsWith( "_3" ) ) ;
		boolean    slotBLooksLikeC = layoutSlot.equals( "B" ) && blueprintName.endsWith( "_3" );
		boolean    slotCLooksLikeB = layoutSlot.equals( "C" ) && blueprintName.endsWith( "_2" );

		String name = blueprintName;
		if ( wrongSuffixOnSlotA || slotBLooksLikeC || slotCLooksLikeB ) {
			name = name.substring( 0, name.length() - 2 );
		}

		if ( layoutSlot.equals( "B" ) && !( name.endsWith( "_2" ) ) )
		{
			name += "_2";
		}
		else if ( layoutSlot.equals( "C" ) && !( name.endsWith( "_3" ) ) )
		{
			name += "_3";
		}
		return name;
	}

	/**
	 * @return dat-relative path to the ship's TXT layout file.
	 */
	public String getLayoutTXT()
	{
		return "data/" + layout + ".txt";
	}

	/**
	 * @return dat-relative path to the ship's XML layout file.
	 */
	public String getLayoutXML()
	{
		return "data/" + layout + ".xml";
	}

	/**
	 * Sets the image namespace of the ship ('img' attribute)
	 */
	public void setImageNamespace( String image )
	{
		if ( image == null )
			throw new IllegalArgumentException( "Image namespace must not be null." );
		img = image;
	}

	/**
	 * @return the image namespace of the ship ('img' attribute)
	 */
	public String getImageNamespace()
	{
		return img;
	}

	/**
	 * Sets the name of the ship's class, eg. "Kestrel Cruiser", "Auto-Scout"
	 */
	public void setShipClass( IDeferredText className )
	{
		if ( className == null )
			throw new IllegalArgumentException( "The ship class' name must not be null." );
		shipClass = className;
	}

	/**
	 * Convenience counterpart to {@link #setShipClass(IDeferredText)}, which
	 * automatically constructs a {@link IDeferredText} wrapper around the provided string.
	 * 
	 * @param className
	 *            the name of the ship's class
	 * 
	 * @see #setShipClass(IDeferredText)
	 */
	public void setShipClass( String className )
	{
		if ( className == null )
			throw new IllegalArgumentException( "The ship class' name must not be null." );
		if ( className.equals( getShipClass().getTextValue() ) )
			return;
		setShipClass( new DefaultDeferredText( "text_" + blueprintName + "_class", className ) );
	}

	/**
	 * @return name of the ship's class, eg. "Kestrel Cruiser", "Auto-Scout"
	 */
	public IDeferredText getShipClass()
	{
		return shipClass;
	}

	/**
	 * Sets the name of the ship, eg. "The Torus", "Gila Monster", etc.<br>
	 * Player ships only.
	 */
	public void setShipName( IDeferredText shipName )
	{
		if ( shipName == null )
			throw new IllegalArgumentException( "The ship's name must not be null." );
		this.shipName = shipName;
	}

	/**
	 * Convenience counterpart to {@link #setShipName(IDeferredText)}, which
	 * automatically constructs a {@link IDeferredText} wrapper around the provided string.
	 * 
	 * @param shipName
	 *            the name of the ship
	 * 
	 * @see #setShipName(IDeferredText)
	 */
	public void setShipName( String shipName )
	{
		if ( shipName == null )
			throw new IllegalArgumentException( "The ship's name must not be null." );
		if ( shipName.equals( getShipName().getTextValue() ) )
			return;
		setShipName( new DefaultDeferredText( "text_" + blueprintName + "_name", shipName ) );
	}

	/**
	 * @return the name of the ship, eg. "The Torus", "Gila Monster", etc.
	 */
	public IDeferredText getShipName()
	{
		return shipName;
	}

	/**
	 * Sets the ship's description. Length of the description should be shorter than
	 * approximately 255 characters to ensure that it fits inside container window in-game.
	 * Player ships only.
	 */
	public void setShipDescription( IDeferredText desc )
	{
		if ( desc == null )
			throw new IllegalArgumentException( "Description must not be null." );
		shipDescription = desc;
	}

	/**
	 * Convenience counterpart to {@link #setShipDescription(IDeferredText)}, which
	 * automatically constructs a {@link IDeferredText} wrapper around the provided string.
	 * 
	 * @param desc
	 *            the ship's description
	 * 
	 * @see #setShipDescription(IDeferredText)
	 */
	public void setShipDescription( String desc )
	{
		if ( desc == null )
			throw new IllegalArgumentException( "Description must not be null." );
		if ( desc.equals( getShipDescription().getTextValue() ) )
			return;
		setShipDescription( new DefaultDeferredText( "text_" + blueprintName + "_desc", desc ) );
	}

	/**
	 * @return the ship's description.
	 */
	public IDeferredText getShipDescription()
	{
		return shipDescription;
	}

	/**
	 * Sets the X offset of the ship's origin, in grid cells.
	 */
	public void setXOffset( int i )
	{
		xOffset = i;
	}

	/**
	 * @return grid cell component of the ship origin's offset on the X axis (X_OFFSET property)
	 */
	public int getXOffset()
	{
		return xOffset;
	}

	/**
	 * Sets the Y offset of the ship's origin, in grid cells.
	 */
	public void setYOffset( int i )
	{
		yOffset = i;
	}

	/**
	 * @return grid cell component of the ship origin's offset on the Y axis (Y_OFFSET property)
	 */
	public int getYOffset()
	{
		return yOffset;
	}

	public Point getOffsetThick()
	{
		return new Point( xOffset, yOffset );
	}

	/**
	 * Sets the X offset of the ship's origin, in pixels.
	 */
	public void setHorizontal( int i )
	{
		horizontal = i;
	}

	/**
	 * @return pixel component of the ship origin's offset on the X axis (HORIZONTAL property)
	 */
	public int getHorizontal()
	{
		return horizontal;
	}

	/**
	 * Sets the Y offset of the ship's origin, in pixels.
	 */
	public void setVertical( int i )
	{
		vertical = i;
	}

	/**
	 * @return pixel component of the ship origin's offset on the Y axis (VERTICAL property)
	 */
	public int getVertical()
	{
		return vertical;
	}

	public Point getOffsetFine()
	{
		return new Point( horizontal, vertical );
	}

	/**
	 * Sets the dimensions of the shield image.
	 * 
	 * @see {@link #setEllipse(int, int, int, int)}
	 */
	public void setEllipse( Rectangle rect )
	{
		if ( rect == null )
			throw new IllegalArgumentException( "Ellipse must not be null" );
		setEllipse( rect.x, rect.y, rect.width, rect.height );
	}

	/**
	 * Sets the dimensions of the shield image.
	 * 
	 * @param xOff
	 *            offset from the ship's center
	 * @param yOff
	 *            offset from the ship's center
	 * @param width
	 *            half of the image's width
	 * @param height
	 *            half of the image's height
	 */
	public void setEllipse( int xOff, int yOff, int width, int height )
	{
		ellipse.x = xOff;
		ellipse.y = yOff;
		ellipse.width = width;
		ellipse.height = height;
	}

	public void setEllipseX( int x )
	{
		ellipse.x = x;
	}

	public void setEllipseY( int y )
	{
		ellipse.y = y;
	}

	public void setEllipseWidth( int w )
	{
		ellipse.width = w;
	}

	public void setEllipseHeight( int h )
	{
		ellipse.height = h;
	}

	/**
	 * @return dimensions of the shield image.<br>
	 *         X and Y values represent offset from the ship's center (center of
	 *         smallest rectangle containing all rooms)<br>
	 *         Width and height are half of the actual image's size.
	 */
	public Rectangle getEllipse()
	{
		return Utils.copy( ellipse );
	}

	public int getEllipseX()
	{
		return ellipse.x;
	}

	public int getEllipseY()
	{
		return ellipse.y;
	}

	public int getEllipseWidth()
	{
		return ellipse.width;
	}

	public int getEllipseHeight()
	{
		return ellipse.height;
	}

	/**
	 * Sets the dimensions of the hull image.
	 * 
	 * @see {@link #setHullDimensions(int, int, int, int)}
	 */
	public void setHullDimensions( Rectangle rect )
	{
		if ( rect == null )
			throw new IllegalArgumentException( "Hull dimensions must not be null" );
		setHullDimensions( rect.x, rect.y, rect.width, rect.height );
	}

	/**
	 * Sets the dimensions of the hull image.
	 * 
	 * @param x
	 *            offset from the ship's origin
	 * @param y
	 *            offset from the ship's origin
	 * @param w
	 *            width of the image
	 * @param h
	 *            height of the image
	 */
	public void setHullDimensions( int x, int y, int w, int h )
	{
		hullOffset.x = x;
		hullOffset.y = y;
		hullSize.x = w;
		hullSize.y = h;
	}

	/**
	 * @return dimensions of the hull image.<br>
	 *         X and Y values represent the hull's offset from the ship's origin.
	 */
	public Rectangle getHullDimensions()
	{
		return new Rectangle( hullOffset.x, hullOffset.y, hullSize.x, hullSize.y );
	}

	/**
	 * @return offset of the hull image from the ship's origin.
	 */
	public Point getHullOffset()
	{
		return Utils.copy( hullOffset );
	}

	public Point getHullSize()
	{
		return Utils.copy( hullSize );
	}

	/**
	 * Sets the offset of the cloak image from the top-left corner of the hull image.
	 */
	public void setCloakOffset( Point p )
	{
		setCloakOffset( p.x, p.y );
	}

	/**
	 * Sets the offset of the cloak image from the top-left corner of the hull image.
	 */
	public void setCloakOffset( int x, int y )
	{
		cloakOffset.x = x;
		cloakOffset.y = y;
	}

	/**
	 * @return the offset of the cloak image from the top-left corner of the hull image.
	 */
	public Point getCloakOffset()
	{
		return Utils.copy( cloakOffset );
	}

	/**
	 * Sets the offset of the floor image from the top-left corner of the hull image.
	 */
	public void setFloorOffset( Point p )
	{
		setFloorOffset( p.x, p.y );
	}

	/**
	 * Sets the offset of the floor image from the top-left corner of the hull image.
	 */
	public void setFloorOffset( int x, int y )
	{
		floorOffset.x = x;
		floorOffset.y = y;
	}

	/**
	 * @return the offset of the floor image from the top-left corner of the hull image.
	 */
	public Point getFloorOffset()
	{
		return Utils.copy( floorOffset );
	}

	/**
	 * @return the first system object associated with the given system type.
	 */
	public SystemObject getSystem( Systems sys )
	{
		if ( sys == null )
			throw new IllegalArgumentException( "System type must not be null." );
		return systemMap.get( sys ).get( 0 );
	}

	public ArrayList<SystemObject> getSystems( Systems sys )
	{
		if ( sys == null )
			throw new IllegalArgumentException( "System type must not be null." );

		return systemMap.get( sys );
	}

	/**
	 * @return image object associated with the given image type.
	 */
	public ImageObject getImage( Images image )
	{
		if ( image == null )
			throw new IllegalArgumentException( "Image type must not be null." );
		return imageMap.get( image );
	}

	/**
	 * Sets the image used by the given image type.
	 */
	public void setImage( Images image, String path )
	{
		if ( image == null )
			throw new IllegalArgumentException( "Image type must not be null." );
		imageMap.get( image ).setImagePath( path );
	}

	public void setHealth( int hp )
	{
		if ( hp < 0 )
			throw new IllegalArgumentException( "Amount of health points must be non-negative." );
		hullHealth = hp;
	}

	public int getHealth()
	{
		return hullHealth;
	}

	/**
	 * Sets the amount of power bars available to the ship at the start of the game.
	 */
	public void setPower( int power )
	{
		if ( power < 0 )
			throw new IllegalArgumentException( "Power must be non-negative." );
		maxPower = power;
	}

	/**
	 * @return the amount of power bars available to the ship at the start of the game.
	 */
	public int getPower()
	{
		return maxPower;
	}

	public void setSystemCap( int systemCap )
	{
		if ( systemCap < 0 )
			throw new IllegalArgumentException( "System cap must be non-negative." );
		this.systemCap = systemCap;
	}

	public int getSystemCap() {
		return systemCap;
	}

	/**
	 * Sets the number of weapon slots that the ship has.<br>
	 * This determines how many weapons the ship can have active at once.
	 */
	public void setWeaponSlots( int slots )
	{
		if ( slots < 0 )
			throw new IllegalArgumentException( "Number of slots must be non-negative." );
		weaponSlots = slots;
		if ( weapons.size() > slots ) {
			weapons.subList( slots, weapons.size() ).clear();
		}
		else if ( weapons.size() < slots ) {
			for ( int i = weapons.size(); i <= slots; i++ )
				weapons.add( Database.DEFAULT_WEAPON_OBJ );
		}
	}

	/**
	 * @return the number of weapon slots that the ship has
	 */
	public int getWeaponSlots()
	{
		return weaponSlots;
	}

	public boolean getWeaponsByList()
	{
		return weaponByList;
	}

	public void setWeaponsByList( boolean byList )
	{
		weaponByList = byList;
	}

	/**
	 * Sets the weapon list that the ship will use as its loadout.<br>
	 * Enemy ships only.
	 */
	public void setWeaponList( WeaponList list )
	{
		if ( isPlayer )
			throw new IllegalStateException( "Not an enemy ship." );
		if ( list == null )
			throw new IllegalArgumentException( "List must not be null." );
		weaponList = list;
	}

	/**
	 * @return the weapon list that the ship uses as its loadout
	 */
	public WeaponList getWeaponList()
	{
		return weaponList;
	}

	public WeaponLike[] getWeapons()
	{
		return weapons.toArray( new WeaponLike[0] );
	}

	/**
	 * Puts the new weapon at the specified index in the weapon list.
	 */
	public void changeWeapon( int index, WeaponLike neu )
	{
		if ( index < 0 || index > weaponSlots )
			throw new IllegalArgumentException( "Index is out of bounds: " + index );
		if ( neu == null )
			throw new IllegalArgumentException( "New weapon must not be null." );
		weapons.set( index, neu );
		coalesceWeapons();
	}

	/**
	 * Removes the first occurrence of the old weapon, and puts the new weapon in its place.
	 * 
	 * @return index at which the new weapon was placed
	 */
	public int changeWeapon( WeaponLike old, WeaponLike neu )
	{
		if ( old == null )
			throw new IllegalArgumentException( "Old weapon must not be null." );
		if ( neu == null )
			throw new IllegalArgumentException( "New weapon must not be null." );

		int i = weapons.indexOf( old );
		if ( i == -1 )
			throw new IllegalArgumentException( "Old weapon not found." );
		weapons.set( i, neu );
		return i;
	}

	/**
	 * Coalesces the weapons, moving all dummy weapons to the end of the list, so that
	 * there are no gaps between 'real' weapons.
	 */
	private void coalesceWeapons()
	{
		for ( int i = 0; i < weapons.size(); i++ ) {
			WeaponLike weapon = weapons.get( i );
			if ( weapon == Database.DEFAULT_WEAPON_OBJ ) {
				weapons.remove( weapon );
				weapons.add( weapon );
			}
		}
	}

	/**
	 * Sets the number of drone slots that the ship has.<br>
	 * This determines how many drones the ship can have active at once.
	 */
	public void setDroneSlots( int slots )
	{
		if ( slots < 0 )
			throw new IllegalArgumentException( "Number of slots must be non-negative." );
		droneSlots = slots;
		if ( drones.size() > slots ) {
			drones.subList( slots, drones.size() ).clear();
		}
		else if ( drones.size() < slots ) {
			for ( int i = drones.size(); i <= slots; i++ )
				drones.add( Database.DEFAULT_DRONE_OBJ );
		}
	}

	/**
	 * @return the number of drone slots that the ship has
	 */
	public int getDroneSlots()
	{
		return droneSlots;
	}

	public boolean getDronesByList()
	{
		return droneByList;
	}

	public void setDronesByList( boolean byList )
	{
		droneByList = byList;
	}

	/**
	 * Sets the drone list that the ship will use as its loadout.<br>
	 * Enemy ships only.
	 */
	public void setDroneList( DroneList list )
	{
		if ( isPlayer )
			throw new IllegalStateException( "Not an enemy ship." );
		if ( list == null )
			throw new IllegalArgumentException( "List must not be null." );
		droneList = list;
	}

	/**
	 * @return the drone list that the ship uses as its loadout
	 */
	public DroneList getDroneList()
	{
		return droneList;
	}

	public DroneLike[] getDrones()
	{
		return drones.toArray( new DroneLike[0] );
	}

	/**
	 * Puts the new drone at the specified index in the drone list.
	 */
	public void changeDrone( int index, DroneLike neu )
	{
		if ( index < 0 || index > droneSlots )
			throw new IllegalArgumentException( "Index is out of bounds: " + index );
		if ( neu == null )
			throw new IllegalArgumentException( "New drone must not be null." );
		drones.set( index, neu );
		coalesceDrones();
	}

	/**
	 * Removes the first occurrence of the old drone, and puts the new drone in its place.
	 * 
	 * @return index at which the new drone was placed
	 */
	public int changeDrone( DroneLike old, DroneLike neu )
	{
		if ( old == null )
			throw new IllegalArgumentException( "Old drone must not be null." );
		if ( neu == null )
			throw new IllegalArgumentException( "New drone must not be null." );

		int i = drones.indexOf( old );
		if ( i == -1 )
			throw new IllegalArgumentException( "Old drone not found." );
		drones.set( i, neu );
		return i;
	}

	/**
	 * Coalesces drones, moving all dummy drones to the end of the list, so that
	 * there are no gaps between 'real' drones.
	 */
	private void coalesceDrones()
	{
		for ( int i = 0; i < drones.size(); i++ ) {
			DroneLike drone = drones.get( i );
			if ( drone == Database.DEFAULT_DRONE_OBJ ) {
				drones.remove( drone );
				drones.add( drone );
			}
		}
	}

	public void setMissilesAmount( int amount )
	{
		if ( amount < 0 )
			throw new IllegalArgumentException( "Amount must be non-negative." );
		missiles = amount;
	}

	public int getMissilesAmount()
	{
		return missiles;
	}

	public void setDronePartsAmount( int amount )
	{
		if ( amount < 0 )
			throw new IllegalArgumentException( "Amount must be non-negative." );
		droneParts = amount;
	}

	public int getDronePartsAmount()
	{
		return droneParts;
	}

	/**
	 * Sets the minimum sector in which the ship can appear, inclusive (?)<br>
	 * Enemy ships only.
	 */
	public void setMinSector( int min )
	{
		if ( min < 1 || min > 8 )
			throw new IllegalArgumentException( "Sector number must be within 1..8 range, inclusive: " + min );
		minSector = min;
	}

	/**
	 * Enemy ships only.
	 * 
	 * @return the minimum sector in which the ship can appear, inclusive (?)
	 */
	public int getMinSector()
	{
		return minSector;
	}

	/**
	 * Sets the maximum sector in which the ship can appear, inclusive (?)<br>
	 * Enemy ships only.
	 */
	public void setMaxSector( int max )
	{
		if ( max < 1 || max > 8 )
			throw new IllegalArgumentException( "Sector number must be within 1..8 range, inclusive: " + max );
		maxSector = max;
	}

	/**
	 * Enemy ships only.
	 * 
	 * @return the maximum sector in which the ship can appear, inclusive (?)
	 */
	public int getMaxSector()
	{
		return maxSector;
	}

	/**
	 * Sets the boarding strategy that the ship's crew is going to use when boarding.<br>
	 * Enemy ships only.
	 */
	public void setBoardingAI( BoardingStrategies ai )
	{
		if ( ai == null )
			throw new IllegalArgumentException( "Boarding AI must not be null." );
		boardingAI = ai;
	}

	/**
	 * Enemy ships only.
	 * 
	 * @return the boarding strategy that the ship's crew is going to use when boarding.
	 */
	public BoardingStrategies getBoardingAI()
	{
		return boardingAI;
	}

	/**
	 * Puts the new race at the specified index in the race list.<br>
	 * Player ships only.
	 */
	public void changeCrew( int index, CrewLike neu )
	{
		if ( neu == null )
			throw new IllegalArgumentException( "New crew must not be null." );
		if ( index < 0 )
			throw new IllegalArgumentException( "Index is out of bounds: " + index );
		crewList.set( index, neu );
		coalesceCrew();
	}

	/**
	 * Removes the first occurrence of the old race, and puts the new race in its place.<br>
	 * Player ships only.
	 * 
	 * @return index at which the new race was placed
	 */
	public int changeCrew( CrewLike old, CrewLike neu )
	{
		if ( old == null )
			throw new IllegalArgumentException( "Old crew must not be null." );
		if ( neu == null )
			throw new IllegalArgumentException( "New crew must not be null." );

		int i = crewList.indexOf( old );
		if ( i == -1 )
			throw new IllegalArgumentException( "Old crew not found." );
		crewList.set( i, neu );
		return i;
	}

	/**
	 * @return the number of crew members of the given race in the ship.
	 */
	public int getCrewCount( CrewObject r )
	{
		int result = 0;
		for ( CrewLike crew : crewList ) {
			if ( crew.equals(r) )
				result++;
		}
		return result;
	}

	public CrewLike[] getCrew()
	{
		return crewList.toArray( new CrewLike[0] );
	}

	/**
	 * Coalesces the crew list, moving all null entries to the end of the list, so that
	 * there are no gaps between real crew members.
	 */
	public void coalesceCrew()
	{
		for ( int i = 0; i < crewList.size(); i++ ) {
			CrewLike crew = crewList.get( i );
			if ( crew == Database.DEFAULT_CREW_OBJ || crew == Database.DEFAULT_CREW_LIST ) {
				crewList.remove( crew );
				crewList.add( crew );
			}
		}
	}

	/**
	 * Player ships only.
	 */
	public void setCrewCap(int amount)
	{
		for (int i = amount; i > crewCap; --i)
		{
			crewList.add( Database.DEFAULT_CREW_OBJ );
		}
		for (int i = amount; i < crewCap; ++i)
		{
			crewList.remove(crewList.size() - 1);
		}
		crewCap = amount;
	}
	
	public int getCrewCap()
	{
		return crewCap;
	}

	/**
	 * Sets the singular race or blueprint list that the ship can have.
	 * Enemy ships only.
	 */
	public void setEnemyCrew( CrewLike crew )
	{
		if ( crew == null )
			throw new IllegalArgumentException( "New crew must not be null." );
		enemyCrew = crew;
	}

	/**
	 * Enemy ships only.
	 *
	 * @return the singular race or blueprint list that the ship can have.
	 */
	public CrewLike getEnemyCrew()
	{
		return enemyCrew;
	}

	/**
	 * Sets the minimum amount of crew members of the race that the ship can have.<br>
	 * Enemy ships only.
	 */
	public void setCrewMin( int amount )
	{
		if ( amount < 0 )
			throw new IllegalArgumentException( "Amount must be non-negative." );
		crewMin = amount;
	}

	/**
	 * Enemy ships only.
	 * 
	 * @return minimum amount of crew members of the race that the ship can have
	 */
	public int getCrewMin()
	{
		return crewMin;
	}

	/**
	 * Sets the maximum amount of crew members of the race that the ship can have.<br>
	 * Enemy ships only.
	 */
	public void setCrewMax( int amount )
	{
		if ( amount < 0 )
			throw new IllegalArgumentException( "Amount must be non-negative." );
		crewMax = amount;
	}

	/**
	 * Enemy ships only.
	 * 
	 * @return maximum amount of crew members of the race that the ship can have
	 */
	public int getCrewMax()
	{
		return crewMax;
	}

	/**
	 * Sets the number of hidden augments that the ship has.
	 */
	public void setHiddenAugmentsNumber( int number )
	{
		if ( number < 0 )
			throw new IllegalArgumentException( "Number of hidden augments must be non-negative." );
		hiddenAugmentsNumber = number;
		if ( hiddenAugments.size() > number ) {
			hiddenAugments.subList( number, hiddenAugments.size() ).clear();
		}
		else if ( hiddenAugments.size() < number ) {
			for ( int i = hiddenAugments.size(); i <= number; i++ )
				hiddenAugments.add( Database.DEFAULT_AUGMENT_OBJ );
		}
	}

	public int getHiddenAugmentsNumber() {
		return hiddenAugmentsNumber;
	}

	/**
	 * Modifying the array doesn't change the order of elements in the ship.
	 * 
	 * @return an array of all augments in this ship
	 */
	public AugmentObject[] getAugments()
	{
		return augments.toArray( new AugmentObject[0] );
	}

	public AugmentObject[] getHiddenAugments() {
		return hiddenAugments.toArray( new AugmentObject[0] );
	}

	/**
	 * Puts the new augment at the specified index in the augment list.
	 */
	public void changeAugment( int index, AugmentObject neu )
	{
		if ( index < 0 )
			throw new IllegalArgumentException( "Index is out of bounds: " + index );
		if ( neu == null )
			throw new IllegalArgumentException( "New augment must not be null." );
		augments.set( index, neu );
		coalesceAugments();
	}

	/**
	 * Removes the first occurrence of the old augment, and puts the new augment in its place.
	 * 
	 * @return index at which the new augment was placed
	 */
	public int changeAugment( AugmentObject old, AugmentObject neu )
	{
		if ( old == null )
			throw new IllegalArgumentException( "Old augment must not be null." );
		if ( neu == null )
			throw new IllegalArgumentException( "New augment must not be null." );

		int i = augments.indexOf( old );
		if ( i == -1 )
			throw new IllegalArgumentException( "Old augment not found." );
		augments.set( i, neu );
		return i;
	}

	/**
	 * Coalesces augments, moving all dummy augments to the end of the list, so that
	 * there are no gaps between 'real' augments.
	 */
	private void coalesceAugments()
	{
		for ( int i = 0; i < augments.size(); i++ ) {
			AugmentObject augment = augments.get( i );
			if ( augment == Database.DEFAULT_AUGMENT_OBJ) {
				augments.remove( augment );
				augments.add( augment );
			}
		}
	}

	/**
	 * Puts the new augment at the specified index in the augment list.
	 */
	public void changeHiddenAugment( int index, AugmentObject neu )
	{
		if ( index < 0 )
			throw new IllegalArgumentException( "Index is out of bounds: " + index );
		if ( neu == null )
			throw new IllegalArgumentException( "New augment must not be null." );
		hiddenAugments.set( index, neu );
		coalesceHiddenAugments();
	}

	/**
	 * Just adds the new augment to the augment list. Only meant for constructing the list initially.
	 * Not for adding augments through the menu, use {@link ShipObject#changeHiddenAugment(int, AugmentObject) } instead.
	 */
	public void addHiddenAugment( AugmentObject augmentObject ) {
		hiddenAugments.add( augmentObject );
	}

	/**
	 * Removes the first occurrence of the old augment, and puts the new augment in its place.
	 *
	 * @return index at which the new augment was placed
	 */
	public int changeHiddenAugment( AugmentObject old, AugmentObject neu )
	{
		if ( old == null )
			throw new IllegalArgumentException( "Old augment must not be null." );
		if ( neu == null )
			throw new IllegalArgumentException( "New augment must not be null." );

		int i = hiddenAugments.indexOf( old );
		if ( i == -1 )
			throw new IllegalArgumentException( "Old augment not found." );
		hiddenAugments.set( i, neu );
		return i;
	}

	/**
	 * Coalesces augments, moving all dummy augments to the end of the list, so that
	 * there are no gaps between 'real' augments.
	 */
	private void coalesceHiddenAugments()
	{
		for ( int i = 0; i < hiddenAugments.size(); i++ ) {
			AugmentObject augment = hiddenAugments.get( i );
			if ( augment == Database.DEFAULT_AUGMENT_OBJ) {
				hiddenAugments.remove( augment );
				hiddenAugments.add( augment );
			}
		}
	}

	/**
	 * Modifying the array doesn't change the order of elements in the ship.
	 * 
	 * @return an array of all rooms in this ship
	 */
	public RoomObject[] getRooms()
	{
		return rooms.toArray( new RoomObject[0] );
	}

	/**
	 * Modifying the array doesn't change the order of elements in the ship.
	 * 
	 * @return an array of all doors in this ship
	 */
	public DoorObject[] getDoors()
	{
		return doors.toArray( new DoorObject[0] );
	}

	/**
	 * Modifying the array doesn't change the order of elements in the ship.
	 * 
	 * @return an array of all mounts in this ship
	 */
	public MountObject[] getMounts()
	{
		return mounts.toArray( new MountObject[0] );
	}

	/**
	 * Modifying the array doesn't change the order of elements in the ship.
	 * 
	 * @return an array of all gibs in this ship
	 */
	public GibObject[] getGibs()
	{
		return gibs.toArray( new GibObject[0] );
	}

	/**
	 * @param id
	 *            ID number of the sought room
	 * @return room with the given ID, or null if none was found
	 */
	public RoomObject getRoomById( int id )
	{
		if ( id == -1 )
			return Database.AIRLOCK_OBJECT;
		RoomObject[] roomz = getRooms();
		try {
			return roomz[binarySearch( roomz, id, 0, roomz.length )];
		}
		catch ( IndexOutOfBoundsException e ) {
			return null;
		}
	}

	/**
	 * @param id
	 *            ID number of the sought mount
	 * @return mount with the given ID, or null if none was found
	 */
	public MountObject getMountById( int id )
	{
		for ( MountObject mount : mounts ) {
			if ( mount.getId() == id )
				return mount;
		}
		return null;
	}

	/**
	 * @param id
	 *            ID number of the sought gib
	 * @return gib with the given ID, or null if none was found
	 */
	public GibObject getGibById( int id )
	{
		for ( GibObject gib : gibs ) {
			if ( gib.getId() == id )
				return gib;
		}
		return null;
	}

	/**
	 * Adds the game object to the ship, storing it in the appropriate list
	 * depending on its type.<br>
	 * <br>
	 * This method should not be called directly.
	 * Use {@link com.kartoflane.superluminal2.ui.ShipContainer#add(AbstractController)
	 * ShipContainer.add()} instead
	 * 
	 * @param object
	 *            object that is to be added
	 */
	public void add( GameObject object )
	{
		if ( object == null )
			throw new IllegalArgumentException( "Object must not be null." );

		if ( object instanceof RoomObject ) {
			rooms.add( (RoomObject)object );
		}
		else if ( object instanceof DoorObject ) {
			doors.add( (DoorObject)object );
		}
		else if ( object instanceof MountObject ) {
			mounts.add( (MountObject)object );
		}
		else if ( object instanceof GibObject ) {
			gibs.add( (GibObject)object );
		}
		else if ( object instanceof AugmentObject ) {
			if ( ((AugmentObject) object).isHidden() ) {
				hiddenAugments.add( (AugmentObject)object );
			}
			else {
				augments.add( (AugmentObject)object );
			}
		}
		else if ( object instanceof SystemObject ) {
			SystemObject system = (SystemObject)object;
			systemMap.get( system.getSystemId() ).add( system );
		}
		else {
			throw new IllegalArgumentException( "Game object was of unexpected type: " + object.getClass().getSimpleName() );
		}
	}

	/**
	 * Removes the game object from the ship.<br>
	 * <br>
	 * This method should not be called directly.
	 * Use {@link com.kartoflane.superluminal2.ui.ShipContainer#remove(AbstractController)
	 * ShipContainer.remove()} instead
	 * 
	 * @param object
	 *            object that is to be removed
	 */
	public void remove( GameObject object )
	{
		if ( object instanceof RoomObject ) {
			rooms.remove( object );
		}
		else if ( object instanceof DoorObject ) {
			doors.remove( object );
		}
		else if ( object instanceof MountObject ) {
			mounts.remove( object );
		}
		else if ( object instanceof GibObject ) {
			gibs.remove( object );
		}
		else if ( object instanceof AugmentObject ) {
			if ( ((AugmentObject) object).isHidden() ) {
				hiddenAugments.remove( object );
			}
			else {
				augments.remove( object );
			}
		}
		else if ( object instanceof SystemObject ) {
			SystemObject system = (SystemObject)object;
			systemMap.get( system.getSystemId() ).remove( system );
		}
		else {
			throw new IllegalArgumentException( "Game object was of unexpected type: " + object.getClass().getSimpleName() );
		}
	}

	/**
	 * Coalesces the rooms, removing gaps in room numbering.
	 */
	public void coalesceRooms()
	{
		int id = 0;
		RoomObject[] roomArray = getRooms();
		rooms.clear();
		for ( RoomObject room : roomArray ) {
			room.setId( id++ );
			rooms.add( room );
		}
	}

	/**
	 * Coalesces the gibs, removing gaps in gib numbering.
	 */
	public void coalesceGibs()
	{
		// Gibs are 1-relative
		int id = 1;
		GibObject[] gibArray = getGibs();
		gibs.clear();
		for ( GibObject gib : gibArray ) {
			gib.setId( id++ );
			gibs.add( gib );
		}
	}

	public boolean hasOverlappingRooms()
	{
		boolean result = false;
		for ( RoomObject r : rooms ) {
			Rectangle b = r.getBounds();
			for ( RoomObject o : rooms ) {
				if ( r != o && b.intersects( o.getBounds() ) ) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Automatically links doors to adjacent rooms.
	 */
	public void linkDoors()
	{
		for ( DoorObject door : doors ) {
			door.verifyLinks();

			if ( door.isHorizontal() ) {
				if ( door.getLeftRoom() == null )
					door.setLeftRoom( getRoomAt( door.getX(), door.getY() - 1 ) );
				if ( door.getRightRoom() == null )
					door.setRightRoom( getRoomAt( door.getX(), door.getY() ) );
			}
			else {
				if ( door.getLeftRoom() == null )
					door.setLeftRoom( getRoomAt( door.getX() - 1, door.getY() ) );
				if ( door.getRightRoom() == null )
					door.setRightRoom( getRoomAt( door.getX(), door.getY() ) );
			}

			// When linking to airlocks, the airlock has to be linked as the right "room"
			if ( door.getLeftRoom() == Database.AIRLOCK_OBJECT && door.getRightRoom() != Database.AIRLOCK_OBJECT ) {
				door.setLeftRoom( door.getRightRoom() );
				door.setRightRoom( Database.AIRLOCK_OBJECT );
			}
		}
	}

	/**
	 * Resets door links to null, so that they will be automatically linked.
	 */
	public void resetDoorLinks()
	{
		for ( DoorObject door : doors ) {
			door.setLeftRoom( null );
			door.setRightRoom( null );
		}
	}

	private RoomObject getRoomAt( int x, int y )
	{
		for ( RoomObject room : rooms ) {
			if ( room.getBounds().contains( x, y ) )
				return room;
		}

		return Database.AIRLOCK_OBJECT;
	}

	public void sort()
	{
		// Reinsert all objects to sort the sets

		Object[] array = rooms.toArray( new Object[0] );
		for ( Object o : array )
			rooms.remove( o );
		for ( Object o : array )
			rooms.add( (RoomObject)o );

		array = mounts.toArray( new Object[0] );
		for ( Object o : array )
			mounts.remove( o );
		for ( Object o : array )
			mounts.add( (MountObject)o );

		array = gibs.toArray( new Object[0] );
		for ( Object o : array )
			gibs.remove( o );
		for ( Object o : array )
			gibs.add( (GibObject)o );
	}

	/**
	 * @return the next unused ID that can be assigned to a new room
	 */
	public int getNextRoomId()
	{
		int id = 0;
		for ( RoomObject object : rooms ) {
			if ( id == object.getId() )
				id++;
		}
		return id;
	}

	/**
	 * @return the next unused ID that can be assigned to a new mount
	 */
	public int getNextMountId()
	{
		int id = 0;
		for ( MountObject object : mounts ) {
			if ( id == object.getId() )
				id++;
		}
		return id;
	}

	private static int binarySearch( RoomObject[] array, int id, int min, int max )
	{
		if ( min > max )
			return -1;
		int mid = ( min + max ) / 2;
		if ( array[mid].getId() < id )
			return binarySearch( array, id, mid + 1, max );
		else if ( array[mid].getId() > id )
			return binarySearch( array, id, min, mid - 1 );
		else
			return mid;
	}
}
