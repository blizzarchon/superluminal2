package com.kartoflane.superluminal2.utils;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import com.kartoflane.superluminal2.components.Polygon;
import com.kartoflane.superluminal2.components.interfaces.Identifiable;

public class Utils {

	public static int distance(Point p1, Point p2) {
		return (int) Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
	}

	public static int distance(int x1, int y1, int x2, int y2) {
		return (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}

	public static Point center(Point p1, Point p2) {
		return center(p1.x, p1.y, p2.x, p2.y);
	}

	public static Point center(int x1, int y1, int x2, int y2) {
		return new Point((x1 + x2) / 2, (y1 + y2) / 2);
	}

	public static Point add(Point p1, Point p2) {
		return new Point(p1.x + p2.x, p1.y + p2.y);
	}

	public static Rectangle copy(Rectangle r) {
		return new Rectangle(r.x, r.y, r.width, r.height);
	}

	public static Point copy(Point p) {
		return new Point(p.x, p.y);
	}

	public static RGB copy(RGB rgb) {
		return new RGB(rgb.red, rgb.green, rgb.blue);
	}

	public static int min(int a, int b, int c) {
		return Math.min(a, Math.min(b, c));
	}

	public static int[] toArray(Rectangle rect) {
		return new int[] {
				rect.x, rect.y,
				rect.x + rect.width, rect.y,
				rect.x, rect.y + rect.height,
				rect.x + rect.width, rect.y + rect.height
		};
	}

	/**
	 * Rotates the rectangle around its center by the given angle.
	 * 
	 * @param b
	 *            the rectangle to be rotated
	 * @param rotation
	 *            angle in degrees
	 * @return the rotated rectangle
	 */
	public static Rectangle rotate(Rectangle r, float rotation) {
		Rectangle b = copy(r);
		if (rotation % 180 == 0) {
			// no need to do anything
		} else if ((int) (rotation % 90) == 0) {
			b.x += b.width / 2 - b.height / 2;
			b.y += b.height / 2 - b.width / 2;
			int a = b.width;
			b.width = b.height;
			b.height = a;
		} else {
			Point c = new Point(b.x + b.width / 2, b.y + b.height / 2);
			Polygon p = new Polygon(toArray(b));
			p.rotate((float) Math.toRadians(rotation), c.x, c.y);
			b = p.getBounds();
		}
		return b;
	}

	/**
	 * 
	 * @param point
	 *            the point to be rotated
	 * @param the
	 *            point around which the point will be rotated
	 * @param rad
	 *            angle in radians
	 * @return the rotated point
	 */
	public static Point rotate(Point point, int cx, int cy, float rad) {
		Point p = copy(point);
		p.x = (int) (Math.cos(rad) * (point.x - cx) - Math.sin(rad) * (point.y - cy) + cx);
		p.y = (int) (Math.sin(rad) * (point.x - cx) + Math.cos(rad) * (point.y - cy) + cy);
		return p;
	}

	/**
	 * Fixes the rectangle, so that width and height are always positive, but the
	 * resulting rectangle covers the same area.
	 * 
	 * @param r
	 *            the rectangle to be fixed (remains unchanged)
	 * @return the fixed rectangle (new instance)
	 */
	public static Rectangle fix(Rectangle r) {
		return new Rectangle(Math.min(r.x, r.x + r.width),
				Math.min(r.y, r.y + r.height),
				Math.abs(r.width), Math.abs(r.height));
	}

	public static boolean contains(Rectangle rect, Rectangle other) {
		return rect.contains(other.x, other.y) && rect.contains(other.x + other.width, other.y + other.height);
	}

	public static int binarySearch(Identifiable[] array, String identifier, int min, int max) {
		if (min > max)
			return -1;
		int mid = (min + max) / 2;
		int result = identifier.compareTo(array[mid].getIdentifier());
		if (result > 0)
			return binarySearch(array, identifier, mid + 1, max);
		else if (result < 0)
			return binarySearch(array, identifier, min, mid - 1);
		else
			return mid;
	}

	/**
	 * Overlays the base RGB with the overlay RGB with the given opacity.<br>
	 * It doesn't work terribly well with RGB (which is an additive color model),
	 * but it's a good-enough approximation.
	 * 
	 * @param base
	 *            the base color
	 * @param overlay
	 *            the color that will be laid over base
	 * @param alpha
	 *            the opacity of the overlay color. Only values from range 0.0 - 1.0.
	 * @return the resulting color
	 */
	public static RGB tint(RGB base, RGB overlay, double alpha) {
		if (alpha < 0 || alpha > 1)
			throw new IllegalArgumentException("Alpha values must be within 0-1 range.");
		RGB tinted = new RGB(base.red, base.green, base.blue);
		tinted.red = (int) (((1 - alpha) * tinted.red + alpha * overlay.red));
		tinted.green = (int) (((1 - alpha) * tinted.green + alpha * overlay.green));
		tinted.blue = (int) (((1 - alpha) * tinted.blue + alpha * overlay.blue));

		return tinted;
	}
}
