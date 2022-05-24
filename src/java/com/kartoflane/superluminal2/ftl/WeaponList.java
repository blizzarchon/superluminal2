package com.kartoflane.superluminal2.ftl;

import com.kartoflane.superluminal2.components.interfaces.WeaponLike;
import com.kartoflane.superluminal2.db.Database;

public class WeaponList extends BlueprintList<WeaponObject> implements WeaponLike
{
	private static final long serialVersionUID = 4618623391139370151L;
	private static final String empty = "No Weapon List";


	/**
	 * Creates the default weapon list object.
	 */
	public WeaponList()
	{
		super( empty );
	}

	public WeaponList( String blueprint )
	{
		super( blueprint );
	}

	@Override
	public AnimationObject getAnimation() {
		return Database.DEFAULT_ANIM_OBJ;
	}

	@Override
	public String buttonView() {
		return blueprintName;
	}
}
