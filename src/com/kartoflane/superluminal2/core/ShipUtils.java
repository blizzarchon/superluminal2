package com.kartoflane.superluminal2.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

import net.vhati.ftldat.FTLDat.FTLPack;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.JDOMParseException;

import com.kartoflane.superluminal2.components.Directions;
import com.kartoflane.superluminal2.components.Images;
import com.kartoflane.superluminal2.components.Races;
import com.kartoflane.superluminal2.components.Systems;
import com.kartoflane.superluminal2.ftl.AugmentObject;
import com.kartoflane.superluminal2.ftl.DoorObject;
import com.kartoflane.superluminal2.ftl.DroneObject;
import com.kartoflane.superluminal2.ftl.GibObject;
import com.kartoflane.superluminal2.ftl.MountObject;
import com.kartoflane.superluminal2.ftl.RoomObject;
import com.kartoflane.superluminal2.ftl.ShipObject;
import com.kartoflane.superluminal2.ftl.StationObject;
import com.kartoflane.superluminal2.ftl.SystemObject;
import com.kartoflane.superluminal2.ftl.WeaponObject;
import com.kartoflane.superluminal2.ui.ShipContainer;

public class ShipUtils {

	private enum LayoutObjects {
		X_OFFSET,
		Y_OFFSET,
		HORIZONTAL,
		VERTICAL,
		ELLIPSE,
		ROOM,
		DOOR
	}

	/**
	 * ------------------------------------------------------ TODO documentation
	 * 
	 * @param e
	 * @return
	 * @throws IllegalArgumentException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JDOMParseException
	 */
	public static ShipObject loadShipXML(Element e)
			throws IllegalArgumentException, FileNotFoundException, IOException, NumberFormatException, JDOMParseException {
		if (e == null)
			throw new IllegalArgumentException("Element must not be null.");

		Database db = Database.getInstance();
		FTLPack data = Database.getInstance().getDataDat();
		FTLPack resource = Database.getInstance().getResourceDat();

		String attr = null;
		Element child = null;

		// Get the blueprint name of the ship first, to determine whether it is a player ship
		attr = e.getAttributeValue("name");
		if (attr == null)
			throw new IllegalArgumentException("Missing 'name' attribute.");
		String blueprintName = attr;

		// Create the ship object
		boolean isPlayer = db.isPlayerShip(blueprintName);
		ShipObject ship = new ShipObject(isPlayer);
		ship.setBlueprintName(blueprintName);

		// Load the TXT layout of the ship (rooms, doors, offsets, shield)
		attr = e.getAttributeValue("layout");
		if (attr == null)
			throw new IllegalArgumentException("Missing 'layout' attribute.");
		ship.setLayout(attr);

		if (!data.contains(ship.getLayoutTXT()))
			throw new FileNotFoundException("TXT layout file could not be found in game's archives: " + ship.getLayoutTXT());

		InputStream is = data.getInputStream(ship.getLayoutTXT());
		loadLayoutTXT(ship, is, ship.getLayoutTXT());

		// Load ship's images
		attr = e.getAttributeValue("img");
		if (attr == null)
			throw new IllegalArgumentException("Missing 'img' attribute.");
		ship.setImageNamespace(attr);

		String namespace = attr;
		// Ship images can be located in either ship/, ships_glow/ or ships_noglow/
		String[] prefixes = { "rdat:img/ship/", "rdat:img/ships_glow/", "rdat:img/ships_noglow/" };

		// Load the hull image
		ship.setImage(Images.HULL, firstExisting(prefixes, namespace + "_base.png", resource));

		// Load the cloak image, check for override
		child = e.getChild("cloakImage");
		if (child != null)
			namespace = child.getValue();
		ship.setImage(Images.CLOAK, firstExisting(prefixes, namespace + "_cloak.png", resource));
		namespace = attr;

		// Floor and shield images are exclusive to player ships.
		// Enemies use a common shield image that has its dimensions
		// defined by the ELLIPSE layout object.
		if (isPlayer) {
			// Load the floor image, check for override
			child = e.getChild("floorImage");
			if (child != null)
				namespace = child.getValue();
			ship.setImage(Images.FLOOR, firstExisting(prefixes, namespace + "_floor.png", resource));
			namespace = attr;

			// Load the shield image, check for override
			child = e.getChild("shieldImage");
			if (child != null)
				namespace = child.getValue();
			ship.setImage(Images.SHIELD, firstExisting(prefixes, namespace + "_shields1.png", resource));
			namespace = attr;

			// Load the thumbnail/miniship image path (not represented in the editor)
			ship.setImage(Images.THUMBNAIL, "rdat:img/customizeUI/miniship_" + namespace + ".png");
		}

		// Load the XML layout of the ship (images' offsets, gibs, weapon mounts)
		if (!data.contains(ship.getLayoutXML()))
			throw new FileNotFoundException("XML layout file could not be found in game's archives: " + ship.getLayoutXML());

		is = data.getInputStream(ship.getLayoutXML());
		loadLayoutXML(ship, is, ship.getLayoutXML());

		// Get the class of the ship
		child = e.getChild("class");
		if (child == null)
			throw new IllegalArgumentException("Missing <class> tag.");
		ship.setShipClass(child.getValue());

		// Get the name of the ship
		// Exclusive to player ships
		if (isPlayer) {
			child = e.getChild("name");
			if (child == null)
				throw new IllegalArgumentException("Missing <name> tag.");
			ship.setShipName(child.getValue());
		}

		// Get the description of the ship
		// Exclusive to player ships
		if (isPlayer) {
			child = e.getChild("desc");
			if (child == null)
				ship.setShipDescription("<desc> tag was missing!");
			else
				ship.setShipDescription(child.getValue());
		}

		// Get the list of systems installed on the ship
		child = e.getChild("systemList");
		if (child == null)
			throw new IllegalArgumentException("Missing <systemList> tag.");

		for (Systems sys : Systems.getSystems()) {
			Element sysEl = child.getChild(sys.name().toLowerCase());

			if (sysEl != null) {
				SystemObject system = ship.getSystem(sys);

				// Get the min level the system can have, or the starting level of the system
				attr = sysEl.getAttributeValue("power");
				if (attr == null)
					throw new IllegalArgumentException(sys.toString() + " is missing 'power' attribute.");
				system.setLevelStart(Integer.valueOf(attr));

				// Get the max level the system can have
				// Exclusive to enemy ships
				if (!isPlayer) {
					attr = sysEl.getAttributeValue("max");
					if (attr != null) {
						system.setLevelMax(Integer.valueOf(attr));
					} else {
						// Some ships (mostly BOSS) are missing the 'max' attribute
						// Guess-default to the system's level cap
						system.setLevelMax(system.getLevelCap());
					}
				}

				// Get the room to which the system is assigned
				attr = sysEl.getAttributeValue("room");
				if (attr == null)
					throw new IllegalArgumentException(sys.toString() + " is missing 'room' attribute.");
				int id = Integer.valueOf(attr);
				system.setRoom(ship.getRoomById(id));

				// Whether the ship starts with the system installed or not
				// Optional
				attr = sysEl.getAttributeValue("start");
				if (attr != null) {
					system.setAvailable(Boolean.valueOf(attr));
				} else {
					// Default to true
					system.setAvailable(true);
				}

				// Get the interior image used for this system
				// System objects are assigned default interior images on their creation
				// Optional
				attr = sysEl.getAttributeValue("img");
				if (attr != null) {
					system.setInteriorNamespace(attr);
					// TODO what happens with custom-named interior images?
				} else if (!isPlayer) {
					// Enemy ships' systems don't use interior images
					system.setInteriorNamespace(null);
				}

				// Get the weapon used by this system
				// Exclusive to artillery
				if (sys == Systems.ARTILLERY) {
					attr = sysEl.getAttributeValue("weapon");
					if (attr == null)
						throw new IllegalArgumentException("Artillery is missing 'weapon' attribute.");
					String artilleryWeapon = attr;
					// TODO ??????
				}

				// Load station position and direction for this system
				if (system.canContainStation()) {
					Element stEl = sysEl.getChild("slot");

					// Station objects are instantiated with default values for their system
					// Optional
					if (stEl != null) {
						StationObject station = system.getStation();

						Element slotEl = stEl.getChild("number");
						if (slotEl != null)
							station.setSlotId(Integer.valueOf(slotEl.getValue()));

						Element dirEl = stEl.getChild("direction");
						if (dirEl != null)
							station.setSlotDirection(Directions.valueOf(dirEl.getValue().toUpperCase()));
					} else if (!isPlayer) {
						// Enemy ships' stations are placed and rotated mostly randomly
						// No reason to try to emulate this, so just hide the station
						StationObject station = system.getStation();
						station.setSlotId(-2);
					}
				}
			}
		}

		child = e.getChild("weaponSlots");
		if (child == null) {
			ship.setWeaponSlots(4); // Default
		} else {
			ship.setWeaponSlots(Integer.valueOf(child.getValue()));
		}

		child = e.getChild("droneSlots");
		if (child == null) {
			ship.setDroneSlots(2); // Default
		} else {
			ship.setDroneSlots(Integer.valueOf(child.getValue()));
		}

		child = e.getChild("weaponList");
		if (child == null) {
			ship.setMissilesAmount(0);
		} else {
			attr = child.getAttributeValue("missiles");
			if (attr == null)
				ship.setMissilesAmount(0);
			else
				ship.setMissilesAmount(Integer.valueOf(attr));

			attr = child.getAttributeValue("count");
			int count = -1; // Default - load all
			if (attr != null)
				count = Integer.valueOf(attr);

			int loaded = 0;
			MountObject[] mounts = ship.getMounts();
			for (Element weapon : child.getChildren("weapon")) {
				if (count != -1 && loaded >= count)
					break;

				// Artillery always uses fifth weapon mount TODO what if no artillery system?
				MountObject mount = mounts[loaded >= 4 ? loaded + 1 : loaded];
				attr = weapon.getAttributeValue("name");
				if (attr == null)
					throw new IllegalArgumentException("A weapon in <weaponList> is missing 'name' attribute.");

				WeaponObject weaponObject = db.getWeapon(attr);
				if (weaponObject == null)
					throw new IllegalArgumentException("WeaponBlueprint not found: " + attr);

				mount.setWeapon(weaponObject);

				loaded++;
			}
		}

		child = e.getChild("droneList");
		if (child == null) {
			ship.setDronePartsAmount(0);
		} else {
			attr = child.getAttributeValue("drones");
			if (attr == null)
				ship.setDronePartsAmount(0);
			else
				ship.setDronePartsAmount(Integer.valueOf(attr));

			attr = child.getAttributeValue("count");
			int count = -1;
			if (attr != null)
				count = Integer.valueOf(attr);

			int loaded = 0;
			for (Element drone : child.getChildren("drone")) {
				if (count != -1 && loaded >= count)
					break;

				attr = drone.getAttributeValue("name");
				if (attr == null)
					throw new IllegalArgumentException("A drone in <droneList> is missing 'name' attribute.");

				DroneObject droneObject = db.getDrone(attr);
				if (droneObject == null)
					throw new IllegalArgumentException("DroneBlueprint not found: " + attr);

				ship.add(droneObject);

				loaded++;
			}
		}

		child = e.getChild("health");
		if (child == null)
			throw new IllegalArgumentException("Missing <health> tag");
		attr = child.getAttributeValue("amount");
		if (attr == null)
			throw new IllegalArgumentException("<health> tag is missing 'amount' attribute.");
		ship.setHealth(Integer.valueOf(attr));

		child = e.getChild("maxPower");
		if (child == null)
			throw new IllegalArgumentException("Missing <maxPower> tag");
		attr = child.getAttributeValue("amount");
		if (attr == null)
			throw new IllegalArgumentException("<maxPower> tag is missing 'amount' attribute.");
		ship.setPower(Integer.valueOf(attr));

		if (!ship.isPlayerShip()) {
			child = e.getChild("minSector");
			if (child == null)
				ship.setMinSector(1);
			else {
				attr = child.getValue();
				ship.setMinSector(Integer.valueOf(attr));
			}

			child = e.getChild("maxSector");
			if (child == null)
				ship.setMaxSector(8);
			else {
				attr = child.getValue();
				ship.setMaxSector(Integer.valueOf(attr));
			}
		}

		for (Element crew : e.getChildren("crewCount")) {
			attr = crew.getAttributeValue("class");
			if (attr == null)
				throw new IllegalArgumentException("<crewCount> tag is missing 'class' attribute.");
			Races race = null;
			try {
				race = Races.valueOf(attr.toUpperCase());
			} catch (IllegalArgumentException ex) {
				throw new IllegalArgumentException("Race class not recognised: " + attr);
			}

			attr = crew.getAttributeValue("amount");
			if (attr == null)
				throw new IllegalArgumentException("<crewCount> tag is missing 'amount' attribute.");
			ship.setCrewCount(race, Integer.valueOf(attr));

			if (!ship.isPlayerShip()) {
				attr = crew.getAttributeValue("max");
				if (attr == null)
					throw new IllegalArgumentException("<crewCount> tag is missing 'max' attribute.");
				ship.setCrewMax(race, Integer.valueOf(attr));
			}
		}

		for (Element aug : e.getChildren("aug")) {
			attr = aug.getAttributeValue("name");
			if (attr == null)
				throw new IllegalArgumentException("An augment is missing 'name' attribute.");

			AugmentObject augmentObject = db.getAugment(attr);
			if (augmentObject == null)
				throw new IllegalArgumentException("AugBlueprint not found: " + attr);

			ship.add(augmentObject);
		}

		return ship;
	}

	public static void saveShipXML(File destination, ShipContainer container) throws IllegalArgumentException, IOException {
		if (container == null)
			throw new IllegalArgumentException("ShipContainer must not be null.");

		ShipObject ship = container.getShipController().getGameObject();

		if (ship == null)
			throw new IllegalArgumentException("Ship object must not be null.");
		if (destination == null)
			throw new IllegalArgumentException("Destination file must not be null.");
		if (!destination.isDirectory())
			throw new IllegalArgumentException("Not a directory: " + destination.getName());

		container.updateGameObjects();
		ship.coalesceRooms();
		// TODO automatically link doors

		Document doc = new Document();
		Element root = new Element("wrapper");
		Element e = null;
		String attr = null;

		// Prepare the document
		Element shipBlueprint = new Element("shipBlueprint");
		attr = ship.getBlueprintName();
		shipBlueprint.setAttribute("name", attr);
		attr = ship.getLayout();
		shipBlueprint.setAttribute("layout", attr);
		attr = ship.getImageNamespace();
		shipBlueprint.setAttribute("img", attr);

		e = new Element("class");
		attr = ship.getShipClass();
		e.setText(attr == null ? "" : attr);
		shipBlueprint.addContent(e);

		e = new Element("name");
		attr = ship.getShipName();
		e.setText(attr == null ? "" : attr);
		shipBlueprint.addContent(e);

		e = new Element("desc");
		attr = ship.getShipDescription();
		e.setText(attr == null ? "" : attr);
		shipBlueprint.addContent(e);

		Element systemList = new Element("systemList");
		for (Systems sys : Systems.getSystems()) {
			SystemObject system = ship.getSystem(sys);

			if (system.isAssigned()) {
				Element sysEl = new Element(sys.toString().toLowerCase());

				sysEl.setAttribute("power", "" + system.getLevelStart());

				// Enemy ships' system have a 'max' attribute which determines the max level of the system
				if (!ship.isPlayerShip())
					sysEl.setAttribute("max", "" + system.getLevelMax());

				sysEl.setAttribute("room", "" + system.getRoom().getId());

				sysEl.setAttribute("start", "" + system.isAvailable());

				// Artillery has a special 'weapon' attribute to determine which weapon is used as artillery weapon
				if (sys == Systems.ARTILLERY)
					sysEl.setAttribute("weapon", ""); // TODO artillery weapon, default to ARTILLERY_FED

				if (system.canContainInterior() && ship.isPlayerShip() && system.getInteriorNamespace() != null)
					sysEl.setAttribute("img", system.getInteriorNamespace());

				StationObject station = system.getStation();

				if (sys.canContainStation() && station.getSlotId() != -2) {
					Element slotEl = new Element("slot");

					// Medbay and Clonebay slots don't have a direction - they're always NONE
					if (sys != Systems.MEDBAY && sys != Systems.CLONEBAY) {
						e = new Element("direction");
						e.setText(station.getSlotDirection().toString());
						slotEl.addContent(e);
					}

					e = new Element("number");
					e.setText("" + station.getSlotId());
					slotEl.addContent(e);

					sysEl.addContent(slotEl);
				}

				systemList.addContent(sysEl);
			}
		}
		shipBlueprint.addContent(systemList);

		e = new Element("weaponSlots");
		e.setText("" + ship.getWeaponSlots());
		shipBlueprint.addContent(e);

		e = new Element("droneSlots");
		e.setText("" + ship.getDroneSlots());
		shipBlueprint.addContent(e);

		e = new Element("weaponList");
		e.setAttribute("missiles", "" + ship.getMissilesAmount());

		// Player ships' weapons have to be declared explicitly, ie. listed by name
		// Only the first 'count' weapons are loaded in-game
		if (ship.isPlayerShip()) {
			e.setAttribute("count", "4"); // TODO weapon count
		}
		// Enemy ships' weapons are randomly drafted from a list of weapons
		// 'count' determines how many weapons are drafted
		else {
			// TODO load="" + count
		}
		shipBlueprint.addContent(e);

		e = new Element("droneList");
		e.setAttribute("drones", "" + ship.getDronePartsAmount());

		// Player ships' drones have to be declared explicitly, ie. listed by name
		// Only the first 'count' drones are loaded in-game
		if (ship.isPlayerShip()) {
			e.setAttribute("count", "4"); // TODO drone count
		}
		// Enemy ships' drones are randomly drafted from a list of drones
		// 'count' determines how many drones are drafted
		else {
			// TODO load="" + count
		}
		shipBlueprint.addContent(e);

		e = new Element("health");
		e.setAttribute("amount", "" + ship.getHealth());
		shipBlueprint.addContent(e);

		e = new Element("maxPower");
		e.setAttribute("amount", "" + ship.getPower());
		shipBlueprint.addContent(e);

		// Sector tags
		// Enemy exclusive
		if (!ship.isPlayerShip()) {
			e = new Element("minSector");
			e.setText("" + ship.getMinSector());
			shipBlueprint.addContent(e);

			e = new Element("maxSector");
			e.setText("" + ship.getMaxSector());
			shipBlueprint.addContent(e);
		}

		for (Races race : Races.values()) {
			int amount = ship.getCrewCount(race);
			int max = ship.getCrewMax(race);

			e = new Element("crewCount");
			e.setAttribute("amount", "" + amount);

			if (!ship.isPlayerShip())
				e.setAttribute("max", "" + max);

			e.setAttribute("class", race.toString().toLowerCase());

			// Don't print an empty tag
			if (amount > 0 && (ship.isPlayerShip() || max > 0))
				shipBlueprint.addContent(e);
		}

		for (AugmentObject aug : ship.getAugments()) {
			e = new Element("aug");
			e.setAttribute("name", aug.getBlueprintName());
			shipBlueprint.addContent(e);
		}

		root.addContent(shipBlueprint);
		doc.setRootElement(root);

		// Write the files
		File blueprints = new File(destination.getAbsolutePath() + "/" +
				Database.getInstance().getAssociatedFile(ship.getBlueprintName()) + ".append");
		Utils.writeFileXML(doc, blueprints);

		File fileTXT = new File(destination.getAbsolutePath() + "/" + ship.getLayout() + ".txt");
		saveLayoutTXT(ship, fileTXT);

		File fileXML = new File(destination.getAbsolutePath() + "/" + ship.getLayout() + ".xml");
		saveLayoutXML(ship, fileXML);
	}

	/**
	 * Loads the layout from the given stream, and adds it to the given ship object.
	 * 
	 * @param ship
	 *            the ship object in which the loaded data will be saved
	 * @param is
	 *            input stream from which the data is read. The stream is always closed by this method.
	 * @param fileName
	 *            how error messages will refer to the stream
	 * @throws IllegalArgumentException
	 *             when the file is wrongly formatted
	 */
	public static void loadLayoutTXT(ShipObject ship, InputStream is, String fileName) throws IllegalArgumentException {
		if (ship == null)
			throw new IllegalArgumentException("Ship object must not be null.");
		if (is == null)
			throw new IllegalArgumentException("Stream must not be null.");

		Scanner sc = new Scanner(is);

		HashMap<DoorObject, Integer> leftMap = new HashMap<DoorObject, Integer>();
		HashMap<DoorObject, Integer> rightMap = new HashMap<DoorObject, Integer>();

		try {
			while (sc.hasNext()) {
				String line = sc.nextLine();

				if (line == null || line.trim().equals(""))
					continue;

				LayoutObjects layoutObject = null;
				try {
					layoutObject = LayoutObjects.valueOf(line);
				} catch (IllegalArgumentException e) {
					try {
						Integer.parseInt(line);
					} catch (NumberFormatException ex) {
						// Not a number
						throw new IllegalArgumentException(fileName + " contained an unknown layout object: " + line);
					}
				}
				switch (layoutObject) {
					case X_OFFSET:
						ship.setXOffset(sc.nextInt());
						break;
					case Y_OFFSET:
						ship.setYOffset(sc.nextInt());
						break;
					case HORIZONTAL:
						ship.setHorizontal(sc.nextInt());
						break;
					case VERTICAL:
						ship.setVertical(sc.nextInt());
						break;
					case ELLIPSE:
						Rectangle ellipse = new Rectangle(0, 0, 0, 0);
						ellipse.width = sc.nextInt();
						ellipse.height = sc.nextInt();
						ellipse.x = sc.nextInt();
						ellipse.y = sc.nextInt();
						ship.setEllipse(ellipse);
						break;
					case ROOM:
						RoomObject room = new RoomObject();
						room.setId(sc.nextInt());
						room.setLocation(sc.nextInt(), sc.nextInt());
						room.setSize(sc.nextInt(), sc.nextInt());
						ship.add(room);
						break;
					case DOOR:
						DoorObject door = new DoorObject();
						door.setLocation(sc.nextInt(), sc.nextInt());
						leftMap.put(door, sc.nextInt());
						rightMap.put(door, sc.nextInt());
						door.setHorizontal(sc.nextInt() == 0);
						ship.add(door);
						break;
					default:
						throw new IllegalArgumentException("Unrecognised layout object: " + layoutObject);
				}
			}

			// Link doors to rooms
			for (DoorObject door : ship.getDoors()) {
				door.setLeftRoom(ship.getRoomById(leftMap.get(door)));
				door.setRightRoom(ship.getRoomById(rightMap.get(door)));
			}
		} finally {
			sc.close();
		}
	}

	/**
	 * Loads the layout from the given file, and adds it to the given ship object.
	 * 
	 * @param ship
	 *            the ship object in which the loaded data will be saved
	 * @param f
	 *            file from which the data is read
	 * @throws IllegalArgumentException
	 *             when the file is wrongly formatted
	 * @throws IOException
	 *             when a general IO error occurs
	 */
	public static void loadLayoutTXT(ShipObject ship, File f) throws IllegalArgumentException, IOException {
		if (ship == null)
			throw new IllegalArgumentException("Ship object must not be null.");
		if (f == null)
			throw new IllegalArgumentException("File must not be null.");
		loadLayoutTXT(ship, new FileInputStream(f), f.getName());
	}

	public static void saveLayoutTXT(ShipObject ship, File f) throws FileNotFoundException, IOException {
		if (ship == null)
			throw new IllegalArgumentException("Ship object must not be null.");
		if (f == null)
			throw new IllegalArgumentException("File must not be null.");

		FileWriter writer = null;

		StringBuilder buf = new StringBuilder();

		buf.append(LayoutObjects.X_OFFSET);
		buf.append("\r\n");
		buf.append("" + ship.getXOffset());
		buf.append("\r\n");

		buf.append(LayoutObjects.Y_OFFSET);
		buf.append("\r\n");
		buf.append("" + ship.getYOffset());
		buf.append("\r\n");

		buf.append(LayoutObjects.HORIZONTAL);
		buf.append("\r\n");
		buf.append("" + ship.getHorizontal());
		buf.append("\r\n");

		buf.append(LayoutObjects.VERTICAL);
		buf.append("\r\n");
		buf.append("" + ship.getVertical());
		buf.append("\r\n");

		buf.append(LayoutObjects.ELLIPSE);
		buf.append("\r\n");
		Rectangle ellipse = ship.getEllipse();
		buf.append("" + ellipse.width);
		buf.append("\r\n");
		buf.append("" + ellipse.height);
		buf.append("\r\n");
		buf.append("" + ellipse.x);
		buf.append("\r\n");
		buf.append("" + ellipse.y);
		buf.append("\r\n");

		for (RoomObject room : ship.getRooms()) {
			buf.append(LayoutObjects.ROOM);
			buf.append("\r\n");
			buf.append("" + room.getId());
			buf.append("\r\n");
			buf.append("" + room.getX());
			buf.append("\r\n");
			buf.append("" + room.getY());
			buf.append("\r\n");
			buf.append("" + room.getW());
			buf.append("\r\n");
			buf.append("" + room.getH());
			buf.append("\r\n");
		}

		RoomObject linked = null;
		for (DoorObject door : ship.getDoors()) {
			buf.append(LayoutObjects.DOOR);
			buf.append("\r\n");
			buf.append("" + door.getX());
			buf.append("\r\n");
			buf.append("" + door.getY());
			buf.append("\r\n");
			linked = door.getLeftRoom();
			buf.append(linked == null ? "-1" : linked.getId());
			buf.append("\r\n");
			linked = door.getRightRoom();
			buf.append(linked == null ? "-1" : linked.getId());
			buf.append("\r\n");
			buf.append("" + (door.isHorizontal() ? "0" : "1"));
			buf.append("\r\n");
		}

		try {
			writer = new FileWriter(f);
			writer.write(buf.toString());
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	/**
	 * Loads the XML layout from the stream.
	 * 
	 * @param ship
	 *            ship object in which the loaded data will be saved
	 * @param is
	 *            stream from which the data is read. The stream is always closed by this method.
	 * @param fileName
	 *            how error messages will refer to the stream
	 * @throws IllegalArgumentException
	 *             when the file is wrongly formatted - a tag or an attribute is missing
	 * @throws JDOMParseException
	 *             when a parsing error occurs
	 * @throws IOException
	 *             when a general IO error occurs
	 */
	public static void loadLayoutXML(ShipObject ship, InputStream is, String fileName)
			throws IllegalArgumentException, JDOMParseException, IOException {
		if (ship == null)
			throw new IllegalArgumentException("Ship object must not be null.");
		if (is == null)
			throw new IllegalArgumentException("Stream must not be null.");

		try {
			Document doc = Utils.readStreamXML(is, fileName);

			Element root = doc.getRootElement();
			Element child = null;
			String attr = null;

			// Load the total offset of the image set
			child = root.getChild("img");
			if (child == null)
				throw new IllegalArgumentException("Missing <img> tag");

			Rectangle hullDimensions = new Rectangle(0, 0, 0, 0);
			attr = child.getAttributeValue("x");
			if (attr == null)
				throw new IllegalArgumentException("Img missing 'x' attribute");
			hullDimensions.x = Integer.valueOf(attr);

			attr = child.getAttributeValue("y");
			if (attr == null)
				throw new IllegalArgumentException("Img missing 'y' attribute");
			hullDimensions.y = Integer.valueOf(attr);

			attr = child.getAttributeValue("w");
			if (attr == null)
				throw new IllegalArgumentException("Img missing 'w' attribute");
			hullDimensions.width = Integer.valueOf(attr);

			attr = child.getAttributeValue("h");
			if (attr == null)
				throw new IllegalArgumentException("Img missing 'h' attribute");
			hullDimensions.height = Integer.valueOf(attr);

			ship.setHullDimensions(hullDimensions);

			// Ignore <glowOffset> - only concerns iPad version of FTL

			// Load additional offsets for other images
			Point offset = new Point(0, 0);
			Element offsets = root.getChild("offsets");

			if (offsets != null) {
				child = offsets.getChild("cloak");
				if (child != null) {
					attr = child.getAttributeValue("x");
					if (attr == null)
						throw new IllegalArgumentException("Cloak missing 'x' attribute");
					offset.x = Integer.valueOf(attr);

					attr = child.getAttributeValue("y");
					if (attr == null)
						throw new IllegalArgumentException("Cloak missing 'y' attribute");
					offset.y = Integer.valueOf(attr);

					ship.setCloakOffset(offset);
				}

				child = offsets.getChild("floor");
				if (child != null) {
					attr = child.getAttributeValue("x");
					if (attr == null)
						throw new IllegalArgumentException("Floor missing 'x' attribute");
					offset.x = Integer.valueOf(attr);

					attr = child.getAttributeValue("y");
					if (attr == null)
						throw new IllegalArgumentException("Floor missing 'y' attribute");
					offset.y = Integer.valueOf(attr);

					ship.setFloorOffset(offset);
				}
			}

			HashMap<MountObject, Integer> gibMap = new HashMap<MountObject, Integer>();

			// Load weapon mounts
			child = root.getChild("weaponMounts");
			if (child == null)
				throw new IllegalArgumentException("Missing <weaponMounts> tag");

			int id = 0;
			for (Element mountEl : child.getChildren("mount")) {
				MountObject mount = new MountObject();
				mount.setId(id++);

				attr = mountEl.getAttributeValue("x");
				if (attr == null)
					throw new IllegalArgumentException("Mount missing 'x' attribute");
				offset.x = Integer.valueOf(attr);

				attr = mountEl.getAttributeValue("y");
				if (attr == null)
					throw new IllegalArgumentException("Mount missing 'y' attribute");
				offset.y = Integer.valueOf(attr);

				mount.setLocation(offset.x, offset.y);

				attr = mountEl.getAttributeValue("rotate");
				if (attr == null)
					throw new IllegalArgumentException("Mount missing 'rotate' attribute");
				mount.setRotated(Boolean.valueOf(attr));

				attr = mountEl.getAttributeValue("mirror");
				if (attr == null)
					throw new IllegalArgumentException("Moumt missing 'mirror' attribute");
				mount.setMirrored(Boolean.valueOf(attr));

				attr = mountEl.getAttributeValue("gib");
				if (attr == null)
					throw new IllegalArgumentException("Moumt missing 'gib' attribute");
				gibMap.put(mount, Integer.valueOf(attr));

				attr = mountEl.getAttributeValue("slide");
				if (attr != null)
					mount.setDirection(Directions.parseDir(attr.toUpperCase()));
				else
					mount.setDirection(Directions.NONE);

				ship.add(mount);
			}

			// Load gibs
			child = root.getChild("explosion");
			if (child == null)
				throw new IllegalArgumentException("Missing <explosion> tag");

			for (Element gibEl : child.getChildren()) {
				if (gibEl.getName().startsWith("gib")) {
					GibObject gib = new GibObject();

					attr = gibEl.getName().substring(3);
					gib.setId(Integer.valueOf(attr));

					child = gibEl.getChild("x");
					if (child == null)
						throw new IllegalArgumentException("Gib missing <x> tag");
					offset.x = Integer.valueOf(child.getValue());

					child = gibEl.getChild("y");
					if (child == null)
						throw new IllegalArgumentException("Gib missing <y> tag");
					offset.y = Integer.valueOf(child.getValue());

					gib.setOffset(offset.x, offset.y);

					child = gibEl.getChild("velocity");
					if (child == null)
						throw new IllegalArgumentException("Gib missing <velocity> tag");

					attr = child.getAttributeValue("min");
					if (attr == null)
						throw new IllegalArgumentException("Velocity missing 'min' attribute");
					gib.setVelocityMin(Float.valueOf(attr));

					attr = child.getAttributeValue("max");
					if (attr == null)
						throw new IllegalArgumentException("Velocity missing 'max' attribute");
					gib.setVelocityMax(Float.valueOf(attr));

					child = gibEl.getChild("direction");
					if (child == null)
						throw new IllegalArgumentException("Missing <direction> tag");

					attr = child.getAttributeValue("min");
					if (attr == null)
						throw new IllegalArgumentException("Direction missing 'min' attribute");
					gib.setDirectionMin(Integer.valueOf(attr));

					attr = child.getAttributeValue("max");
					if (attr == null)
						throw new IllegalArgumentException("Direction missing 'max' attribute");
					gib.setDirectionMax(Integer.valueOf(attr));

					child = gibEl.getChild("velocity");
					if (child == null)
						throw new IllegalArgumentException("Missing <angular> tag");

					attr = child.getAttributeValue("min");
					if (attr == null)
						throw new IllegalArgumentException("Angular missing 'min' attribute");
					gib.setAngularMin(Float.valueOf(attr));

					attr = child.getAttributeValue("max");
					if (attr == null)
						throw new IllegalArgumentException("Angular missing 'max' attribute");
					gib.setAngularMax(Float.valueOf(attr));

					ship.add(gib);
				}
			}

			// Link mounts to gibs
			for (MountObject mount : gibMap.keySet())
				mount.setGib(ship.getGibById(gibMap.get(mount)));

			gibMap.clear();
		} finally {
			is.close();
		}
	}

	/**
	 * 
	 * @param ship
	 *            ship object in which the loaded data will be saved
	 * @param f
	 *            file from which the data is read
	 * @throws IllegalArgumentException
	 *             when the file is wrongly formatted - a tag or an attribute is missing
	 * @throws JDOMParseException
	 *             when a parsing error occurs
	 * @throws IOException
	 *             when a general IO error occurs
	 */
	public static void loadLayoutXML(ShipObject ship, File f) throws IllegalArgumentException, JDOMParseException, IOException {
		if (ship == null)
			throw new IllegalArgumentException("Ship object must not be null.");
		if (f == null)
			throw new IllegalArgumentException("File must not be null.");
		loadLayoutXML(ship, new FileInputStream(f), f.getName());
	}

	public static void saveLayoutXML(ShipObject ship, File f) throws IllegalArgumentException, IOException {
		if (ship == null)
			throw new IllegalArgumentException("Ship object must not be null.");
		if (f == null)
			throw new IllegalArgumentException("File must not be null.");

		Document doc = new Document();
		Element root = new Element("wrapper");
		Element e = null;

		Comment c = new Comment("Copyright (c) 2012 by Subset Games. All rights reserved.");
		root.addContent(c);

		e = new Element("img");
		Rectangle hullDimensions = ship.getHullDimensions();
		e.setAttribute("x", "" + hullDimensions.x);
		e.setAttribute("y", "" + hullDimensions.y);
		e.setAttribute("w", "" + hullDimensions.width);
		e.setAttribute("h", "" + hullDimensions.height);
		root.addContent(e);

		Element offsets = new Element("offsets");

		e = new Element("floor");
		e.setAttribute("x", "" + ship.getFloorOffset().x);
		e.setAttribute("y", "" + ship.getFloorOffset().y);
		offsets.addContent(e);

		e = new Element("cloak");
		e.setAttribute("x", "" + ship.getCloakOffset().x);
		e.setAttribute("y", "" + ship.getCloakOffset().y);
		offsets.addContent(e);

		root.addContent(offsets);

		Element weaponMounts = new Element("weaponMounts");

		MountObject[] mounts = ship.getMounts();
		for (int i = 0; i < mounts.length; i++) {
			e = new Element("mount");
			e.setAttribute("x", "" + mounts[i].getX());
			e.setAttribute("y", "" + mounts[i].getY());
			e.setAttribute("rotate", "" + mounts[i].isRotated());
			e.setAttribute("mirror", "" + mounts[i].isMirrored());
			e.setAttribute("gib", "" + mounts[i].getGib().getId());
			e.setAttribute("slide", "" + mounts[i].getDirection().toString());
			weaponMounts.addContent(e);
		}
		root.addContent(weaponMounts);

		Element explosion = new Element("explosion");

		DecimalFormat decimal = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.ENGLISH));
		GibObject[] gibs = ship.getGibs();
		for (int i = 0; i < gibs.length; i++) {
			Element gib = new Element("gib" + (i + 1));

			e = new Element("velocity");
			e.setAttribute("min", "" + decimal.format(gibs[i].getVelocityMin()));
			e.setAttribute("max", "" + decimal.format(gibs[i].getVelocityMax()));
			gib.addContent(e);

			e = new Element("angular");
			e.setAttribute("min", "" + decimal.format(gibs[i].getAngularMin()));
			e.setAttribute("max", "" + decimal.format(gibs[i].getAngularMax()));
			gib.addContent(e);

			e = new Element("direction");
			e.setAttribute("min", "" + gibs[i].getDirectionMin());
			e.setAttribute("max", "" + gibs[i].getDirectionMax());
			gib.addContent(e);

			e = new Element("x");
			e.setText("" + gibs[i].getX());
			gib.addContent(e);

			e = new Element("y");
			e.setText("" + gibs[i].getY());
			gib.addContent(e);

			explosion.addContent(gib);
		}
		root.addContent(explosion);

		doc.setRootElement(root);

		Utils.writeFileXML(doc, f);
	}

	private static String firstExisting(String[] prefixes, String suffix, FTLPack archive) {
		for (String prefix : prefixes) {
			if (archive.contains(Utils.trimProtocol(prefix) + suffix))
				return prefix + suffix;
		}
		return null;
	}
}