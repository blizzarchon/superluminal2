package com.kartoflane.superluminal2.ftl;

import com.kartoflane.superluminal2.components.enums.CrewStats;
import com.kartoflane.superluminal2.components.interfaces.CrewLike;
import com.kartoflane.superluminal2.components.interfaces.Identifiable;

import java.util.ArrayList;
import java.util.HashMap;


public class CrewObject extends GameObject implements Comparable<CrewObject>, Identifiable, CrewLike
{
	protected static final IDeferredText NO_CREW = new VerbatimText( "<No Crew>" );

	protected final String blueprintName;
	protected IDeferredText title = IDeferredText.EMPTY;
	protected IDeferredText shortName = IDeferredText.EMPTY;
	protected IDeferredText description = IDeferredText.EMPTY;

	protected HashMap<CrewStats, Integer> statMap = null;
	protected ArrayList<String> powerList = null;


	/** Creates a default crew object. */
	public CrewObject()
	{
		blueprintName = "Default Crew";
		title = NO_CREW;
		shortName = NO_CREW;
	}

	public CrewObject( String blueprintName )
	{
		this.blueprintName = blueprintName;
	}

	@Override
	public String getIdentifier()
	{
		return blueprintName;
	}

	@Override
	public String buttonView() {
		return title.toString();
	}

	public void update()
	{
		// Nothing to do here
	}

	public String getBlueprintName()
	{
		return blueprintName;
	}

	public void setTitle( IDeferredText title )
	{
		if ( title == null )
			throw new IllegalArgumentException( blueprintName + ": title must not be null." );
		this.title = title;
	}

	public IDeferredText getTitle()
	{
		return title;
	}

	public void setShortName( IDeferredText name )
	{
		if ( name == null )
			throw new IllegalArgumentException( blueprintName + ": name must not be null." );
		shortName = name;
	}

	public IDeferredText getShortName()
	{
		return shortName;
	}

	public void setDescription( IDeferredText desc )
	{
		if ( desc == null )
			throw new IllegalArgumentException( blueprintName + ": description must not be null." );
		description = desc;
	}

	public IDeferredText getDescription()
	{
		return description;
	}

	public void setStat( CrewStats stat, int value )
	{
		if ( stat == null )
			throw new IllegalArgumentException( "Stat type must not be null." );
		if ( statMap == null )
			initStatMap();
		statMap.put( stat, value );
	}

	public int getStat( CrewStats stat )
	{
		if ( stat == null )
			throw new IllegalArgumentException( "Stat type must not be null." );
		if ( statMap == null )
			initStatMap();
		return statMap.get( stat );
	}

	private void initStatMap()
	{
		statMap = new HashMap<CrewStats, Integer>();
		for ( CrewStats stat : CrewStats.values() )
			statMap.put( stat, 0 );
	}

	public void addPower( String power )
	{
		if ( power == null )
			throw new IllegalArgumentException( "Power type must not be null." );
		if ( powerList == null ) {
			powerList = new ArrayList<String>();
		}
		powerList.add( power );
	}

	public ArrayList<String> getPowerList() {
		return powerList;
	}

	@Override
	public int compareTo( CrewObject o )
	{
		return blueprintName.compareTo( o.getBlueprintName() );
	}

	@Override
	public boolean equals( Object o )
	{
		if ( o instanceof CrewObject) {
			CrewObject other = (CrewObject)o;
			return blueprintName.equals( other.blueprintName );
		}
		else
			return false;
	}

	@Override
	public String toString()
	{
		return title.toString();
	}
}
