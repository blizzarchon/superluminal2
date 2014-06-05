package com.kartoflane.superluminal2.mvc.models;

import com.kartoflane.superluminal2.ftl.GameObject;

public class ObjectModel extends BaseModel {

	protected GameObject gameObject = null;

	private ObjectModel() {
		super();
	}

	public ObjectModel(GameObject gameObject) {
		this();
		setGameObject(gameObject);
	}

	public void setGameObject(GameObject o) {
		gameObject = o;
	}

	public GameObject getGameObject() {
		return gameObject;
	}

	@Override
	public void dispose() {
		super.dispose();
		gameObject.setModel(null);
		gameObject = null;
	}

	@Override
	public void delete() {
		gameObject.delete();
	}

	@Override
	public void restore() {
		gameObject.restore();
	}

	@Override
	public void setDeletable(boolean deletable) {
		gameObject.setDeletable(deletable);
	}

	@Override
	public boolean isDeletable() {
		return gameObject.isDeletable();
	}
}
