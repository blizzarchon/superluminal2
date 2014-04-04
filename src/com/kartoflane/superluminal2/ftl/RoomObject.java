package com.kartoflane.superluminal2.ftl;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.kartoflane.superluminal2.components.interfaces.Alias;
import com.kartoflane.superluminal2.components.interfaces.Movable;
import com.kartoflane.superluminal2.components.interfaces.Resizable;
import com.kartoflane.superluminal2.ftl.SystemObject.Systems;
import com.kartoflane.superluminal2.ui.ShipContainer;

public class RoomObject extends GameObject implements Alias, Movable, Resizable, Comparable<RoomObject> {

	private static final long serialVersionUID = -3093852547910315659L;

	private int id = -2;
	private int locX = 0;
	private int locY = 0;
	private int sizeW = 0;
	private int sizeH = 0;

	private Systems system = Systems.EMPTY;
	private String alias = null;

	public RoomObject() {
		setDeletable(true);
	}

	public RoomObject(int id) {
		this();
		this.id = id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setSystem(Systems system) {
		this.system = system;
	}

	public Systems getSystem() {
		return system;
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * Sets the distance from the ship anchor, in grid cells.
	 */
	@Override
	public boolean setLocation(int x, int y) {
		locX = x;
		locY = y;
		return true;
	}

	/**
	 * Moves the room by the specified number of grid cells.
	 */
	@Override
	public boolean translate(int dx, int dy) {
		locX += dx;
		locY += dy;
		return true;
	}

	/**
	 * @return the distance from the ship anchor, in grid cells.
	 */
	@Override
	public Point getLocation() {
		return new Point(locX, locY);
	}

	@Override
	public int getX() {
		return locX;
	}

	@Override
	public int getY() {
		return locY;
	}

	/**
	 * Sets size of the room, in grid cells.
	 */
	@Override
	public boolean setSize(int w, int h) {
		sizeW = w;
		sizeH = h;
		return true;
	}

	/**
	 * @return size of the room, in grid cells.
	 */
	@Override
	public Point getSize() {
		return new Point(sizeW, sizeH);
	}

	@Override
	public int getW() {
		return sizeW;
	}

	@Override
	public int getH() {
		return sizeH;
	}

	/** Returns the slot id of the tile at the given coordinates (relative to the canvas). */
	public int getSlotId(int x, int y) {
		Rectangle bounds = getBounds();
		if (!bounds.contains(x, y))
			return -1;

		x = (x - bounds.x) / ShipContainer.CELL_SIZE;
		y = (y - bounds.y) / ShipContainer.CELL_SIZE;

		return x + (y * (bounds.width / ShipContainer.CELL_SIZE));
	}

	/**
	 * @param id
	 *            the slot id that is to be checked
	 * @return true if the room can contain station at the given slot, false otherwise.
	 */
	public boolean canContainSlotId(int slotId) {
		Rectangle bounds = getBounds();
		int w = bounds.width / ShipContainer.CELL_SIZE;
		int h = bounds.height / ShipContainer.CELL_SIZE;
		return w + h * w >= slotId && slotId >= 0;
	}

	public Point getSlotLocation(int slotId) {
		Point size = getSize();
		int w = size.x / ShipContainer.CELL_SIZE;
		int h = size.y / ShipContainer.CELL_SIZE;

		// can't contain
		if (w + (h - 1) * w < slotId || slotId < 0)
			return null;

		int x = slotId % w;
		int y = slotId / w;

		return new Point(x * ShipContainer.CELL_SIZE + ShipContainer.CELL_SIZE / 2 + 1,
				y * ShipContainer.CELL_SIZE + ShipContainer.CELL_SIZE / 2 + 1);
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(locX - sizeW / 2, locY - sizeH / 2,
				locX + sizeW / 2, locY + sizeH / 2);
	}

	@Override
	public boolean contains(int x, int y) {
		return getBounds().contains(x, y);
	}

	@Override
	public boolean intersects(Rectangle rect) {
		return getBounds().intersects(rect);
	}

	@Override
	public int compareTo(RoomObject o) {
		return id - o.id;
	}
}
