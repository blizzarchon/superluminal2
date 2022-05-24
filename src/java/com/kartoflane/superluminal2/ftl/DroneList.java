package com.kartoflane.superluminal2.ftl;

import com.kartoflane.superluminal2.components.interfaces.DroneLike;

public class DroneList extends BlueprintList<DroneObject> implements DroneLike
{
	private static final long serialVersionUID = 4618623391139370151L;
	private static final String empty = "No Drone List";


	/**
	 * Creates the default drone list object.
	 */
	public DroneList()
	{
		super( empty );
	}

	public DroneList( String blueprint )
	{
		super( blueprint );
	}

	@Override
	public String buttonView() {
		return blueprintName;
	}
}
