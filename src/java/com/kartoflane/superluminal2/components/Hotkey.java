package com.kartoflane.superluminal2.components;

import com.kartoflane.superluminal2.components.enums.Hotkeys;
import com.kartoflane.superluminal2.core.Manager;

public class Hotkey {
	private final Hotkeys id;

	private boolean shift = false;
	private boolean ctrl = false;
	private boolean alt = false;
	private int key = '\0';

	public Hotkey(Hotkeys id) {
		this.id = id;
	}

	public Hotkeys getId() {
		return id;
	}

	public void setKey(int ch) {
		key = ch;
	}

	public int getKey() {
		return key;
	}

	public void setShift(boolean shift) {
		this.shift = shift;
	}

	public boolean getShift() {
		return shift;
	}

	public void setCtrl(boolean control) {
		ctrl = control;
	}

	public boolean getCtrl() {
		return ctrl;
	}

	public void setAlt(boolean alt) {
		this.alt = alt;
	}

	public boolean getAlt() {
		return alt;
	}

	/**
	 * Checks whether the hotkey is to be activated by comparing the hotkey's modifier settings with currently
	 * active modifiers, and the hotkey's trigger key with currently pressed key.
	 * 
	 * @param keyCode
	 *            int representing the currently pressed key
	 * @return true if the hotkey is tiggered, false otherwise
	 */
	public boolean passes(int keyCode) {
		return Manager.modShift == shift && Manager.modCtrl == ctrl && Manager.modAlt == alt &&
				Character.toLowerCase(key) == Character.toLowerCase(keyCode);
	}

	@Override
	public String toString() {
		if (key == '\0')
			return "";

		String msg = "";

		if (shift)
			msg += "Shift+";
		if (ctrl)
			msg += "Ctrl+";
		if (alt)
			msg += "Alt+";

		if (key == ' ')
			msg += "Spacebar";
		else if (key == '\t')
			msg += "Tab";
		else {
			msg += Character.toUpperCase((char) key);
		}

		return msg;
	}
}
