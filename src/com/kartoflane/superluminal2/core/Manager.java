package com.kartoflane.superluminal2.core;

import java.util.HashMap;
import java.util.LinkedList;

import com.kartoflane.superluminal2.components.Hotkey;
import com.kartoflane.superluminal2.components.Hotkey.Hotkeys;
import com.kartoflane.superluminal2.components.interfaces.Deletable;
import com.kartoflane.superluminal2.ftl.ShipObject;
import com.kartoflane.superluminal2.mvc.controllers.AbstractController;
import com.kartoflane.superluminal2.mvc.controllers.ShipController;
import com.kartoflane.superluminal2.tools.Tool;
import com.kartoflane.superluminal2.tools.Tool.Tools;
import com.kartoflane.superluminal2.ui.EditorWindow;
import com.kartoflane.superluminal2.ui.ShipContainer;
import com.kartoflane.superluminal2.ui.sidebar.ManipulationToolComposite;

/**
 * Manager class to manage the current ship, interface flags, selection.
 * 
 * @author kartoFlane
 * 
 */
public class Manager {

	public static final HashMap<Tools, Tool> TOOL_MAP = new HashMap<Tools, Tool>();
	public static final LinkedList<Deletable> DELETED_LIST = new LinkedList<Deletable>();
	public static final HashMap<Hotkeys, Hotkey> HOTKEY_MAP = new HashMap<Hotkeys, Hotkey>();

	public static boolean leftMouseDown = false;
	public static boolean rightMouseDown = false;
	public static boolean modShift = false;
	public static boolean modAlt = false;
	public static boolean modCtrl = false;

	private static AbstractController selectedController;
	private static ShipContainer currentShip = null;
	private static Tools selectedTool = null;

	/**
	 * Selects the given box, deselecting the previous one if it was already selected.<br>
	 * 
	 * @param box
	 *            the box that is to be selected. Can be null to clear selection.
	 */
	public static void setSelected(AbstractController controller) {
		if (controller != null && !controller.isSelectable())
			throw new IllegalArgumentException("Controller is not selectable.");

		if (selectedController != null)
			selectedController.deselect();
		selectedController = null;

		if (controller != null) {
			controller.select();
			selectedController = controller;
		}

		if (selectedTool == Tools.POINTER)
			((ManipulationToolComposite) EditorWindow.getInstance().getSidebarContent()).setController(controller);
	}

	/** Returns the currently selected controller. */
	public static AbstractController getSelected() {
		return selectedController;
	}

	public static void createNewShip() {
		closeShip();

		currentShip = new ShipContainer(new ShipObject(true)); // TODO dialog prompt to choose if player ship?
		currentShip.getShipController().reposition(3 * ShipContainer.CELL_SIZE, 3 * ShipContainer.CELL_SIZE);

		EditorWindow.getInstance().enableTools(true);
		// select the manipulation tool by default
		selectTool(Tools.POINTER);
	}

	public static void loadShip(ShipObject ship) {
		closeShip();

		currentShip = new ShipContainer(ship);
		ShipController sc = currentShip.getShipController();
		sc.select();
		sc.reposition(3 * ShipContainer.CELL_SIZE, 3 * ShipContainer.CELL_SIZE);
		sc.deselect();

		currentShip.updateBoundingArea();
		currentShip.updateChildBoundingAreas();

		EditorWindow.getInstance().enableTools(true);
		// select the manipulation tool by default
		selectTool(Tools.POINTER);

		// TODO load the ship, anything else to do?
	}

	public static void setCurrenetShip(ShipContainer container) {
		// TODO allows tabs of open ships?
	}

	public static void closeShip() {
		// TODO UI prompts and shit

		// if (saved) {
		closeShipForce();
		// } else {
		// prompt if sure
	}

	public static void closeShipForce() {
		if (currentShip != null)
			currentShip.dispose();

		setSelected(null);
		currentShip = null;

		EditorWindow.getInstance().enableTools(false);
		EditorWindow.getInstance().canvasRedraw();
	}

	/** Returns the currently loaded ship. */
	public static ShipContainer getCurrentShip() {
		return currentShip;
	}

	/**
	 * Select the given tool item, also triggering the tools' {@link Tool#select() select()} and {@link Tool#deselect() deselect()} methods, as needed.
	 */
	public static void selectTool(Tools tool) {
		// deny trying to select the same tool twice
		if (selectedTool != null && selectedTool == tool)
			return;

		if (selectedTool != null)
			TOOL_MAP.get(selectedTool).deselect();

		selectedTool = tool;
		if (tool != null) {
			TOOL_MAP.get(tool).select();
			MouseInputDispatcher.getInstance().setCurrentTool(TOOL_MAP.get(selectedTool));
			EditorWindow.getInstance().selectTool(tool);
		}
	}

	public static Tools getSelectedToolId() {
		return selectedTool;
	}

	public static Tool getSelectedTool() {
		return TOOL_MAP.get(selectedTool);
	}

	public static Tool getTool(Tools tool) {
		return TOOL_MAP.get(tool);
	}

	public static Hotkey getHotkey(Hotkeys key) {
		return HOTKEY_MAP.get(key);
	}
}
