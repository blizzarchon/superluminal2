package com.kartoflane.superluminal2.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.JDOMParseException;

import com.kartoflane.superluminal2.components.Tuple;
import com.kartoflane.superluminal2.components.enums.Images;
import com.kartoflane.superluminal2.components.enums.LayoutObjects;
import com.kartoflane.superluminal2.components.enums.Races;
import com.kartoflane.superluminal2.components.enums.Systems;
import com.kartoflane.superluminal2.core.Manager;
import com.kartoflane.superluminal2.db.Database;
import com.kartoflane.superluminal2.ftl.AugmentObject;
import com.kartoflane.superluminal2.ftl.DoorObject;
import com.kartoflane.superluminal2.ftl.DroneList;
import com.kartoflane.superluminal2.ftl.DroneObject;
import com.kartoflane.superluminal2.ftl.GibObject;
import com.kartoflane.superluminal2.ftl.GlowObject;
import com.kartoflane.superluminal2.ftl.GlowSet;
import com.kartoflane.superluminal2.ftl.GlowSet.Glows;
import com.kartoflane.superluminal2.ftl.IDeferredText;
import com.kartoflane.superluminal2.ftl.ImageObject;
import com.kartoflane.superluminal2.ftl.MountObject;
import com.kartoflane.superluminal2.ftl.RoomObject;
import com.kartoflane.superluminal2.ftl.ShipObject;
import com.kartoflane.superluminal2.ftl.StationObject;
import com.kartoflane.superluminal2.ftl.SystemObject;
import com.kartoflane.superluminal2.ftl.WeaponList;
import com.kartoflane.superluminal2.ftl.WeaponObject;
import com.kartoflane.superluminal2.ui.SaveOptionsDialog.SaveOptions;
import com.kartoflane.superluminal2.ui.ShipContainer;


/**
 * This class contains utility methods used to save a ship in a form that mirrors
 * the game's own files.
 * 
 * @author kartoFlane
 * 
 */
public class ShipSaveUtils
{
	private static final Logger log = LogManager.getLogger( ShipSaveUtils.class );


	/**
	 * Saves the ship within the context of the specified database entry, as the specified file.
	 * 
	 * @param destination
	 *            the output file.
	 * @param container
	 *            the ShipContainer to be saved.
	 */
	public static void saveShipFTL( File destination, ShipContainer container )
		throws IllegalArgumentException, IOException, JDOMParseException
	{
		if ( destination == null )
			throw new IllegalArgumentException( "Destination file must not be null." );
		if ( destination.isDirectory() )
			throw new IllegalArgumentException( "Not a file: " + destination.getName() );

		SaveOptions so = container.getSaveOptions();

		HashMap<String, byte[]> fileMap = null;
		if ( so.mod == null ) {
			fileMap = saveShip( container );
		}
		else {
			fileMap = IOUtils.readEntry( so.mod );
			IOUtils.merge( fileMap, container );
		}

		IOUtils.writeZip( fileMap, destination );
	}

	public static void saveShipXML( File destination, ShipContainer container )
		throws IllegalArgumentException, IOException, JDOMParseException
	{
		if ( destination == null )
			throw new IllegalArgumentException( "Destination file must not be null." );
		if ( !destination.isDirectory() )
			throw new IllegalArgumentException( "Not a directory: " + destination.getName() );

		SaveOptions so = container.getSaveOptions();

		HashMap<String, byte[]> fileMap = null;

		if ( so.mod == null ) {
			fileMap = saveShip( container );
		}
		else {
			fileMap = IOUtils.readEntry( so.mod );
			IOUtils.merge( fileMap, container );
		}

		IOUtils.writeDir( fileMap, destination );
	}

	/**
	 * Saves the given ship as a HashMap, with keys denoting the inner pth and file name, and
	 * values being the file's contents as bytes.
	 * 
	 * @param container
	 *            the ship to be saved
	 * @return a HashMap that is a complete representation of the ship in the file system
	 * 
	 * @throws IOException
	 */
	public static HashMap<String, byte[]> saveShip( ShipContainer container ) throws IOException
	{
		if ( container == null )
			throw new IllegalArgumentException( "ShipContainer must not be null." );

		ShipObject ship = container.getShipController().getGameObject();

		if ( ship == null )
			throw new IllegalArgumentException( "Ship object must not be null." );

		container.updateGameObjects();
		ship.coalesceRooms();
		ship.coalesceGibs();

		// Remember door links and recover them later -- linking doors automatically persists after saving
		// is completed, which can cause bugs when the user moves the doors/rooms around and saves again
		HashMap<DoorObject, Tuple<RoomObject, RoomObject>> doorLinkMap = new HashMap<DoorObject, Tuple<RoomObject, RoomObject>>();
		for ( DoorObject d : ship.getDoors() )
			doorLinkMap.put( d, new Tuple<RoomObject, RoomObject>( d.getLeftRoom(), d.getRightRoom() ) );
		ship.linkDoors();

		HashMap<String, byte[]> fileMap = new HashMap<String, byte[]>();
		String fileName = null;
		byte[] bytes = null;

		Charset utf8 = Charset.forName( "UTF-8" );

		// Create the files in memory
		fileName = "data/text_blueprints.xml.append";
		bytes = IOUtils.readDocument( generateTextXML( ship ) ).getBytes( utf8 );
		fileMap.put( fileName, bytes );
		
		if (ship.isPlayerShip())
		{
			fileName = "data/hyperspace.xml.append";
			bytes = IOUtils.readDocument( generateHyperspaceXML( ship ) ).getBytes( utf8 );
			fileMap.put( fileName, bytes );
		}

		fileName = "data/" + Database.getInstance().getAssociatedFile( ship.getBlueprintName() ) + ".append";
		bytes = IOUtils.readDocument( generateBlueprintXML( ship ) ).getBytes( utf8 );
		fileMap.put( fileName, bytes );

		fileName = "data/" + ship.getLayout() + ".txt";
		bytes = generateLayoutTXT( ship ).getBytes( utf8 );
		fileMap.put( fileName, bytes );

		fileName = "data/" + ship.getLayout() + ".xml";
		bytes = IOUtils.readDocument( generateLayoutXML( ship ) ).getBytes( utf8 );
		fileMap.put( fileName, bytes );

		if ( ship.isPlayerShip() ) {
			fileName = "data/rooms.xml.append";
			bytes = IOUtils.readDocument( generateRoomsXML( ship ) ).getBytes( utf8 );
			fileMap.put( fileName, bytes );
		}

		// Recover door links
		for ( DoorObject d : ship.getDoors() ) {
			d.setLeftRoom( doorLinkMap.get( d ).getKey() );
			d.setRightRoom( doorLinkMap.get( d ).getValue() );
		}

		// Copy images
		for ( Images img : Images.getShipImages() ) {
			if ( !img.shouldSave( ship ) )
				continue;

			ImageObject object = ship.getImage( img );
			String path = object.getImagePath();

			if ( path != null ) {
				if ( isImageCorrupt( path ) )
					continue;

				InputStream is = null;
				try {
					is = Manager.getInputStream( path );
					fileName = img.getDatRelativePath( ship ) + img.getPrefix() + ship.getImageNamespace() + img.getSuffix() + ".png";
					fileMap.put( fileName, IOUtils.readStream( is ) );
				}
				catch ( FileNotFoundException e ) {
					log.warn( String.format( "File for %s image could not be found: %s", img, path ) );
				}
				finally {
					if ( is != null )
						is.close();
				}
			}
		}

		if ( ship.isPlayerShip() ) {
			for ( Systems sys : Systems.getSystems() ) {
				for ( SystemObject system : ship.getSystems( sys ) ) {
					String path = system.getInteriorPath();

					if ( path != null && system.isAssigned() ) {
						if ( isImageCorrupt( path ) )
							continue;

						InputStream is = null;
						try {
							is = Manager.getInputStream( path );
							fileName = "img/ship/interior/" + system.getInteriorNamespace() + ".png";
							fileMap.put( fileName, IOUtils.readStream( is ) );
						}
						catch ( FileNotFoundException e ) {
							log.warn( String.format( "File for %s interior image could not be found: %s", sys, path ) );
						}
						finally {
							if ( is != null )
								is.close();
						}
					}

					if ( sys.canContainGlow() && sys.canContainStation() ) {
						GlowObject glow = system.getGlow();
						GlowSet set = glow.getGlowSet();

						for ( Glows glowId : Glows.getGlows() ) {
							path = set.getImage( glowId );

							if ( path != null ) {
								if ( isImageCorrupt( path ) )
									continue;

								InputStream is = null;
								try {
									is = Manager.getInputStream( path );
									fileName = "img/ship/interior/" + set.getIdentifier() + glowId.getSuffix() + ".png";
									fileMap.put( fileName, IOUtils.readStream( is ) );
								}
								catch ( FileNotFoundException e ) {
									log.warn(
										String.format( "File for %s's %s glow image could not be found: %s", glow.getIdentifier(), glowId, path )
									);
								}
								finally {
									if ( is != null )
										is.close();
								}
							}
						}
					}
				}
			}

			SystemObject cloaking = ship.getSystem( Systems.CLOAKING );
			GlowSet cloakSet = cloaking.getGlow().getGlowSet();
			if ( cloakSet != Database.DEFAULT_GLOW_SET ) {
				String path = cloakSet.getImage( Glows.CLOAK );

				if ( path != null && !isImageCorrupt( path ) ) {
					InputStream is = null;
					try {
						is = Manager.getInputStream( path );
						fileName = "img/ship/interior/" + cloaking.getInteriorNamespace() + Glows.CLOAK.getSuffix() + ".png";
						fileMap.put( fileName, IOUtils.readStream( is ) );
					}
					catch ( FileNotFoundException e ) {
						log.warn( "File for cloaking glow image could not be found: " + path );
					}
					finally {
						if ( is != null )
							is.close();
					}
				}
			}
		}

		for ( int i = 1; i <= ship.getGibs().length; i++ ) {
			GibObject gib = ship.getGibById( i );
			if ( gib == null )
				throw new IllegalStateException( "Missing gib object with id " + i );

			String path = gib.getImagePath();
			if ( path != null ) {
				if ( isImageCorrupt( path ) )
					continue;

				InputStream is = null;
				try {
					is = Manager.getInputStream( path );
					String datRelativePath = ship.isPlayerShip() ? "img/ship/" : "img/ships_glow/";
					fileName = datRelativePath + ship.getImageNamespace() + "_gib" + gib.getId() + ".png";
					fileMap.put( fileName, IOUtils.readStream( is ) );
				}
				catch ( FileNotFoundException e ) {
					log.warn( String.format( "File for gib #%s could not be found: %s", gib.getId(), path ) );
				}
				finally {
					if ( is != null )
						is.close();
				}
			}
		}

		return fileMap;
	}

	public static void saveLayoutTXT( ShipObject ship, File f ) throws FileNotFoundException, IOException
	{
		if ( ship == null )
			throw new IllegalArgumentException( "Ship object must not be null." );
		if ( f == null )
			throw new IllegalArgumentException( "File must not be null." );

		FileWriter writer = null;

		try {
			writer = new FileWriter( f );
			writer.write( generateLayoutTXT( ship ) );
		}
		finally {
			if ( writer != null )
				writer.close();
		}
	}

	public static void saveLayoutXML( ShipObject ship, File f ) throws IllegalArgumentException, IOException
	{
		if ( ship == null )
			throw new IllegalArgumentException( "Ship object must not be null." );
		if ( f == null )
			throw new IllegalArgumentException( "File must not be null." );

		IOUtils.writeFileXML( generateLayoutXML( ship ), f );
	}
	
	public static void saveHyperspaceXML( ShipObject ship, File f ) throws IllegalArgumentException, IOException
	{
		if ( ship == null )
			throw new IllegalArgumentException( "Ship object must not be null." );
		if ( f == null )
			throw new IllegalArgumentException( "File must not be null." );

		IOUtils.writeFileXML( generateHyperspaceXML( ship ), f );
	}

	private static void outputNamedTextReferenceElement( Element destElement, IDeferredText text )
	{
		if ( destElement == null )
			throw new IllegalArgumentException( "Element must not be null." );
		if ( text == null )
			throw new IllegalArgumentException( "Text must not be null." );

		destElement.setText( text.getTextValue() );
	}

	private static Element createNamedTextSourceElement( IDeferredText text )
	{
		if ( text == null )
			throw new IllegalArgumentException( "Text must not be null." );

		Element e = new Element( "text" );
		e.setText( text.getTextValue() );
		e.setAttribute( "name", text.getTextId() );

		return e;
	}

	protected static Document generateTextXML( ShipObject ship )
	{
		Document doc = new Document();
		Element root = new Element( "wrapper" );

		root.addContent( createNamedTextSourceElement( ship.getShipClass() ) );

		if ( ship.isPlayerShip() ) {
			root.addContent( createNamedTextSourceElement( ship.getShipName() ) );
			root.addContent( createNamedTextSourceElement( ship.getShipDescription() ) );
		}

		doc.setRootElement( root );

		return doc;
	}

	/**
	 * This method generates the shipBlueprint tag that describes the ship passed as argument.
	 * 
	 * @param ship
	 *            the ship to be saved
	 * @return the XML document containing the shipBlueprint tag
	 */
	public static Document generateBlueprintXML( ShipObject ship )
	{
		Document doc = new Document();
		Element root = new Element( "wrapper" );
		Element e = null;
		String value = null;

		Element shipBlueprint = new Element( "shipBlueprint" );
		value = ship.getBlueprintName();
		if (ship.getLayoutSlot().equals("B") && !(ship.getBlueprintName().endsWith("_2")))
		{
			value += "_2";
		}
		else if (ship.getLayoutSlot().equals("C") && !(ship.getBlueprintName().endsWith("_3")))
		{
			value += "_3";
		}
		shipBlueprint.setAttribute( "name", value == null ? "" : value );
		value = ship.getLayout();
		shipBlueprint.setAttribute( "layout", value == null ? "" : value );
		value = ship.getImageNamespace();
		shipBlueprint.setAttribute( "img", value == null ? "" : value );

		// The ship's class name, used for flavor only on player ships,
		// but on enemy ships it is used as the enemy ship's name
		e = new Element( "class" );
		outputNamedTextReferenceElement( e, ship.getShipClass() );
		shipBlueprint.addContent( e );

		// Name and description only affect player ships
		if ( ship.isPlayerShip() ) {
			e = new Element( "name" );
			outputNamedTextReferenceElement( e, ship.getShipName() );
			shipBlueprint.addContent( e );

			e = new Element( "desc" );
			outputNamedTextReferenceElement( e, ship.getShipDescription() );
			shipBlueprint.addContent( e );
		}
		// Sector tags
		// Enemy exclusive
		else {
			e = new Element( "minSector" );
			e.setText( "" + ship.getMinSector() );
			shipBlueprint.addContent( e );

			e = new Element( "maxSector" );
			e.setText( "" + ship.getMaxSector() );
			shipBlueprint.addContent( e );
		}

		Element systemList = new Element( "systemList" );
		for ( Systems sys : Systems.getSystems() ) {
			for ( SystemObject system : ship.getSystems( sys ) ) {
				if ( system.isAssigned() ) {
					Element sysEl = new Element( sys.toString().toLowerCase() );

					sysEl.setAttribute( "power", "" + system.getLevelStart() );

					// Enemy ships' system have a 'max' attribute which determines the max level of the system
					if ( !ship.isPlayerShip() )
						sysEl.setAttribute( "max", "" + system.getLevelMax() );

					sysEl.setAttribute( "room", "" + system.getRoom().getId() );

					sysEl.setAttribute( "start", "" + system.isAvailable() );

					// Artillery has a special 'weapon' attribute to determine which weapon is used as artillery weapon
					if ( sys == Systems.ARTILLERY ) {
						WeaponObject weapon = system.getWeapon();

						// If none set, default to ARTILLERY_FED
						if ( weapon == Database.DEFAULT_WEAPON_OBJ ) {
							sysEl.setAttribute( "weapon", "ARTILLERY_FED" );
						}
						else {
							sysEl.setAttribute( "weapon", weapon.getBlueprintName() );
						}
					}

					if ( system.canContainInterior() && ship.isPlayerShip() && system.getInteriorNamespace() != null )
						sysEl.setAttribute( "img", system.getInteriorNamespace() );

					StationObject station = system.getStation();

					if ( sys.canContainStation() && ship.isPlayerShip() ) {
						Element slotEl = new Element( "slot" );

						// Medbay and Clonebay slots don't have a direction - they're always NONE
						if ( sys != Systems.MEDBAY && sys != Systems.CLONEBAY ) {
							e = new Element( "direction" );
							e.setText( station.getSlotDirection().toString() );
							slotEl.addContent( e ); // Add <direction> to <slot>
						}

						e = new Element( "number" );
						e.setText( "" + station.getSlotId() );
						slotEl.addContent( e ); // Add <number> to <slot>

						sysEl.addContent( slotEl );
					}

					systemList.addContent( sysEl );
				}
			}
		}
		shipBlueprint.addContent( systemList );

		e = new Element( "weaponSlots" );
		e.setText( "" + ship.getWeaponSlots() );
		shipBlueprint.addContent( e );

		e = new Element( "droneSlots" );
		e.setText( "" + ship.getDroneSlots() );
		shipBlueprint.addContent( e );

		Element weaponList = new Element( "weaponList" );
		weaponList.setAttribute( "missiles", "" + ship.getMissilesAmount() );
		weaponList.setAttribute( "count", "" + ship.getWeaponSlots() );

		if ( ship.getWeaponsByList() ) {
			// Weapons are randomly drafted from a list of weapons
			// 'count' determines how many weapons are drafted
			WeaponList list = ship.getWeaponList();
			if ( list != Database.DEFAULT_WEAPON_LIST )
				weaponList.setAttribute( "load", list.getBlueprintName() );
		}
		else {
			// Weapons declared explicitly, ie. listed by name
			// Only the first 'count' weapons are loaded in-game
			for ( WeaponObject weapon : ship.getWeapons() ) {
				if ( weapon == Database.DEFAULT_WEAPON_OBJ )
					continue;
				e = new Element( "weapon" );
				e.setAttribute( "name", weapon.getBlueprintName() );
				weaponList.addContent( e );
			}
		}
		shipBlueprint.addContent( weaponList );

		Element droneList = new Element( "droneList" );
		droneList.setAttribute( "drones", "" + ship.getDronePartsAmount() );
		droneList.setAttribute( "count", "" + ship.getDroneSlots() );

		if ( ship.getDronesByList() ) {
			// Drones are randomly drafted from a list of drones
			// 'count' determines how many drones are drafted
			DroneList list = ship.getDroneList();
			if ( list != Database.DEFAULT_DRONE_LIST )
				droneList.setAttribute( "load", list.getBlueprintName() );
		}
		else {
			// Drones declared explicitly, ie. listed by name
			// Only the first 'count' drones are loaded in-game
			for ( DroneObject drone : ship.getDrones() ) {
				if ( drone == Database.DEFAULT_DRONE_OBJ )
					continue;
				e = new Element( "drone" );
				e.setAttribute( "name", drone.getBlueprintName() );
				droneList.addContent( e );
			}
		}
		shipBlueprint.addContent( droneList );

		// Defines the ship's health points
		e = new Element( "health" );
		e.setAttribute( "amount", "" + ship.getHealth() );
		shipBlueprint.addContent( e );

		// Defines the amount of power the ship starts with
		e = new Element( "maxPower" );
		e.setAttribute( "amount", "" + ship.getPower() );
		shipBlueprint.addContent( e );

		if ( ship.isPlayerShip() ) {
			// List every crew member individually to allow ordering of crew
			for ( String race : ship.getCrew() ) {
				if ( race.equals("no_crew") )
					continue;
				e = new Element( "crewCount" );
				e.setAttribute( "amount", "1" );
				e.setAttribute( "class", race.toLowerCase() );

				shipBlueprint.addContent( e );
			}
		}
		else {
			for ( String race : Races.getRaces() ) {
				int amount = ship.getCrewMin( race );
				int max = ship.getCrewMax( race );

				e = new Element( "crewCount" );
				e.setAttribute( "amount", "" + amount );
				e.setAttribute( "max", "" + max );
				e.setAttribute( "class", race.toLowerCase() );

				// Don't print an empty tag
				if ( amount > 0 && ( ship.isPlayerShip() || max > 0 ) )
					shipBlueprint.addContent( e );
			}

			// <boardingAI> tag, enemy exclusive
			e = new Element( "boardingAI" );
			e.setText( ship.getBoardingAI().toString().toLowerCase() );
			shipBlueprint.addContent( e );
		}

		for ( AugmentObject aug : ship.getAugments() ) {
			if ( aug == Database.DEFAULT_AUGMENT_OBJ || aug.isHidden)
				continue;
			e = new Element( "aug" );
			e.setAttribute( "name", aug.getBlueprintName() );
			shipBlueprint.addContent( e );
		}

		root.addContent( shipBlueprint );
		doc.setRootElement( root );

		return doc;
	}

	public static String generateLayoutTXT( ShipObject ship )
	{
		StringBuilder buf = new StringBuilder();

		buf.append( LayoutObjects.X_OFFSET );
		buf.append( "\r\n" );
		buf.append( "" + ship.getXOffset() );
		buf.append( "\r\n" );

		buf.append( LayoutObjects.Y_OFFSET );
		buf.append( "\r\n" );
		buf.append( "" + ship.getYOffset() );
		buf.append( "\r\n" );

		buf.append( LayoutObjects.HORIZONTAL );
		buf.append( "\r\n" );
		buf.append( "" + ship.getHorizontal() );
		buf.append( "\r\n" );

		buf.append( LayoutObjects.VERTICAL );
		buf.append( "\r\n" );
		buf.append( "" + ship.getVertical() );
		buf.append( "\r\n" );

		buf.append( LayoutObjects.ELLIPSE );
		buf.append( "\r\n" );
		Rectangle ellipse = ship.getEllipse();
		buf.append( "" + ellipse.width );
		buf.append( "\r\n" );
		buf.append( "" + ellipse.height );
		buf.append( "\r\n" );
		buf.append( "" + ellipse.x );
		buf.append( "\r\n" );
		buf.append( "" + ( ellipse.y - ( ship.isPlayerShip() ? 0 : Database.ENEMY_SHIELD_Y_OFFSET ) ) );
		buf.append( "\r\n" );

		for ( RoomObject room : ship.getRooms() ) {
			buf.append( LayoutObjects.ROOM );
			buf.append( "\r\n" );
			buf.append( "" + room.getId() );
			buf.append( "\r\n" );
			buf.append( "" + room.getX() );
			buf.append( "\r\n" );
			buf.append( "" + room.getY() );
			buf.append( "\r\n" );
			buf.append( "" + room.getW() );
			buf.append( "\r\n" );
			buf.append( "" + room.getH() );
			buf.append( "\r\n" );
		}

		RoomObject linked = null;
		for ( DoorObject door : ship.getDoors() ) {
			buf.append( LayoutObjects.DOOR );
			buf.append( "\r\n" );
			buf.append( "" + door.getX() );
			buf.append( "\r\n" );
			buf.append( "" + door.getY() );
			buf.append( "\r\n" );
			linked = door.getLeftRoom();
			buf.append( linked == null ? "-1" : linked.getId() );
			buf.append( "\r\n" );
			linked = door.getRightRoom();
			buf.append( linked == null ? "-1" : linked.getId() );
			buf.append( "\r\n" );
			buf.append( "" + ( door.isHorizontal() ? "0" : "1" ) );
			buf.append( "\r\n" );
		}

		return buf.toString();
	}

	public static Document generateLayoutXML( ShipObject ship )
	{
		Document doc = new Document();
		Element root = new Element( "wrapper" );
		Element e = null;

		Comment c = new Comment( "Copyright (c) 2012 by Subset Games. All rights reserved." );
		root.addContent( c );

		e = new Element( "img" );
		Rectangle hullDimensions = ship.getHullDimensions();
		e.setAttribute( "x", "" + hullDimensions.x );
		e.setAttribute( "y", "" + hullDimensions.y );
		e.setAttribute( "w", "" + hullDimensions.width );
		e.setAttribute( "h", "" + hullDimensions.height );
		root.addContent( e );

		Element offsets = new Element( "offsets" );

		e = new Element( "floor" );
		e.setAttribute( "x", "" + ship.getFloorOffset().x );
		e.setAttribute( "y", "" + ship.getFloorOffset().y );
		offsets.addContent( e );

		e = new Element( "cloak" );
		e.setAttribute( "x", "" + ship.getCloakOffset().x );
		e.setAttribute( "y", "" + ship.getCloakOffset().y );
		offsets.addContent( e );

		root.addContent( offsets );

		Element weaponMounts = new Element( "weaponMounts" );

		MountObject[] mounts = ship.getMounts();
		for ( int i = 0; i < mounts.length; i++ ) {
			e = new Element( "mount" );
			e.setAttribute( "x", "" + mounts[i].getX() );
			e.setAttribute( "y", "" + mounts[i].getY() );
			e.setAttribute( "rotate", "" + mounts[i].isRotated() );
			e.setAttribute( "mirror", "" + mounts[i].isMirrored() );
			e.setAttribute( "gib", "" + mounts[i].getGib().getId() );
			e.setAttribute( "slide", "" + mounts[i].getDirection().toString() );
			weaponMounts.addContent( e );
		}
		root.addContent( weaponMounts );

		Element explosion = new Element( "explosion" );

		DecimalFormat decimal = new DecimalFormat( "0.00", new DecimalFormatSymbols( Locale.ENGLISH ) );
		GibObject[] gibs = ship.getGibs();
		for ( int i = 0; i < gibs.length; i++ ) {
			Element gib = new Element( "gib" + ( i + 1 ) );

			e = new Element( "velocity" );
			e.setAttribute( "min", "" + decimal.format( gibs[i].getVelocityMin() ) );
			e.setAttribute( "max", "" + decimal.format( gibs[i].getVelocityMax() ) );
			gib.addContent( e );

			e = new Element( "direction" );
			e.setAttribute( "min", "" + gibs[i].getDirectionMin() );
			e.setAttribute( "max", "" + gibs[i].getDirectionMax() );
			gib.addContent( e );

			e = new Element( "angular" );
			e.setAttribute( "min", "" + decimal.format( gibs[i].getAngularMin() ) );
			e.setAttribute( "max", "" + decimal.format( gibs[i].getAngularMax() ) );
			gib.addContent( e );

			e = new Element( "x" );
			e.setText( "" + gibs[i].getOffsetX() );
			gib.addContent( e );

			e = new Element( "y" );
			e.setText( "" + gibs[i].getOffsetY() );
			gib.addContent( e );

			explosion.addContent( gib );
		}

		root.addContent( explosion );
		doc.setRootElement( root );

		return doc;
	}

	public static Document generateRoomsXML( ShipObject ship )
	{
		Document doc = new Document();
		Element root = new Element( "wrapper" );

		for ( Systems sys : Systems.getSystems() ) {
			SystemObject system = ship.getSystem( sys );
			if ( system.isAssigned() && sys.canContainGlow() && sys.canContainStation() ) {
				GlowObject glow = system.getGlow();

				Element glowEl = new Element( "roomLayout" );
				String namespace = system.getInteriorNamespace();
				if ( namespace.startsWith( "room_" ) )
					namespace = namespace.replace( "room_", "" );
				glowEl.setAttribute( "name", namespace );

				Element e = new Element( "computerGlow" );
				if ( !glow.getGlowSet().getIdentifier().equals( "glow" ) )
					e.setAttribute( "name", glow.getGlowSet().getIdentifier() );
				e.setAttribute( "x", "" + glow.getX() );
				e.setAttribute( "y", "" + glow.getY() );
				e.setAttribute( "dir", "" + glow.getDirection().name() );

				glowEl.addContent( e );
				root.addContent( glowEl );
			}
		}

		doc.setRootElement( root );
		return doc;
	}
	
	public static Document generateHyperspaceXML( ShipObject ship )
	{
		Document doc = new Document();
		Element root = new Element( "wrapper" );

		Element ftl = new Element( "FTL" );

		Element starter = new Element("findLike", Namespace.getNamespace("mod", "e"));
		starter.setAttribute("type", "ships");
		
		Element shipTag = new Element("ship", Namespace.getNamespace("mod-append", "e"));
		
		shipTag.setAttribute("name", ship.getBlueprintName());
		if (ship.getLayoutSlot().equals("A"))
		{
			shipTag.setAttribute("name", ship.getBlueprintName());
		}
//		else if (ship.getLayoutSlot().equals("B"))
//		{
//			if (ship.getBlueprintName().endsWith("_2"))
//			{
//				shipTag.setAttribute("name", ship.getBlueprintName());
//			}
//			else
//			{
//				shipTag.setAttribute("name", ship.getBlueprintName() + "_2");
//			}
//		}
//		else
//		{
//			if (ship.getBlueprintName().endsWith("_3"))
//			{
//				shipTag.setAttribute("name", ship.getBlueprintName());
//			}
//			else
//			{
//				shipTag.setAttribute("name", ship.getBlueprintName() + "_3");
//			}
//		}
		
		if (ship.getLayoutSlot().equals("B"))
		{
			shipTag.setAttribute("b", "true", Namespace.NO_NAMESPACE);
		}
		else
		{
			shipTag.setAttribute("b", "false", Namespace.NO_NAMESPACE);
		}
		if (ship.getLayoutSlot().equals("C"))
		{
			shipTag.setAttribute("c", "true", Namespace.NO_NAMESPACE);
		}
		else
		{
			shipTag.setAttribute("c", "false", Namespace.NO_NAMESPACE);
		}
		
		starter.addContent(shipTag);
		ftl.addContent(starter);
		
		Element customShip = new Element("customShip", Namespace.getNamespace("mod-append", "e"));
		if (ship.getLayoutSlot().equals("A"))
		{
			customShip.setAttribute("name", ship.getBlueprintName());
		}
		else if (ship.getLayoutSlot().equals("B"))
		{
			if (ship.getBlueprintName().endsWith("_2"))
			{
				customShip.setAttribute("name", ship.getBlueprintName());
			}
			else
			{
				customShip.setAttribute("name", ship.getBlueprintName() + "_2");
			}
		}
		else
		{
			if (ship.getBlueprintName().endsWith("_3"))
			{
				customShip.setAttribute("name", ship.getBlueprintName());
			}
			else
			{
				customShip.setAttribute("name", ship.getBlueprintName() + "_3");
			}
		}

		if (ship.isPlayerShip())
		{
			ArrayList<AugmentObject> list = new ArrayList<AugmentObject>();
			for (AugmentObject aug : ship.getAugments())
			{
				if (aug.isHidden)
				{
					list.add(aug);
				}
			}
			ArrayList<Element> hiddenAugs = new ArrayList<Element>();
			for (int i = 0; i < list.size(); ++i)
			{
				hiddenAugs.add(new Element("hiddenAug").setText(list.get(i).getIdentifier()));
			}
			for(int i = 0; i < hiddenAugs.size(); ++i)
			customShip.addContent(hiddenAugs.get(i));
			customShip.addContent(new Element("crewLimit").setText(ship.getCrewCap() + ""));
		}
		
//		Element hanger = new Element("ships");
//		hanger.addContent(customShip);
		starter.addContent(customShip);
//		ftl.addContent(hanger);
		root.addContent(ftl);

		doc.setRootElement( root );
		return doc;
	}

	/**
	 * Checks whether the image is corrupt.
	 * 
	 * @param path
	 *            path to the image
	 * @return true if the image is corrupt and should not be exported, false otherwise.
	 */
	private static boolean isImageCorrupt( String path )
	{
		Image image = null;
		try {
			image = new Image( UIUtils.getDisplay(), path );
		}
		catch ( SWTException e ) {
			if ( e.getCause() instanceof FileNotFoundException == false ) {
				log.warn( String.format( "Image '%s' is corrupt and will not be exported.", path ) );
				return true;
			}
		}
		finally {
			if ( image != null )
				image.dispose();
		}
		return false;
	}
}
