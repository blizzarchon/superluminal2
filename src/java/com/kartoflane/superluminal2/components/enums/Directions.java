package com.kartoflane.superluminal2.components.enums;

public enum Directions {
	UP,
	DOWN,
	LEFT,
	RIGHT,
	NONE;

	/**
	 * A non-case-sensitive alternative to valueOf(String), that also
	 * interprets NO as NONE and ignores trailing/leading whitespace.
	 */
	public static Directions parseDir(String value) {
		value = value.toUpperCase().trim();
		if (value.equals("NO"))
			return NONE;
		else
			return valueOf(value);
	}

	@Override
	public String toString() {
		switch (this) {
			case NONE:
				return "no";
			default:
				return name().toLowerCase();
		}
	}
}
