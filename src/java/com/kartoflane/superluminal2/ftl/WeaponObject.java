package com.kartoflane.superluminal2.ftl;

import java.util.HashMap;

import com.kartoflane.superluminal2.components.enums.WeaponStats;
import com.kartoflane.superluminal2.components.enums.WeaponTypes;
import com.kartoflane.superluminal2.components.interfaces.Identifiable;
import com.kartoflane.superluminal2.db.Database;


/**
 * A class representing a weapon. Since it serves only as a data vessel
 * for MountObject's internal use, and is never drawn or displayed in
 * any way, it has no Controller or View counterparts.
 * 
 * @author kartoFlane
 * 
 */
public class WeaponObject extends GameObject implements Comparable<WeaponObject>, Identifiable
{
	private static final IDeferredText NO_WEAPON = new VerbatimText( "<No Weapon>" );

	private final String blueprintName;
	private WeaponTypes weaponType;
	private IDeferredText title = IDeferredText.EMPTY;
	private IDeferredText shortName = IDeferredText.EMPTY;
	private IDeferredText description = IDeferredText.EMPTY;
	private String animName = "";

	private AnimationObject cachedAnimation = null;
	private HashMap<WeaponStats, Float> statMap = null;


	/**
	 * Creates a default weapon object.
	 */
	public WeaponObject()
	{
		weaponType = null;
		blueprintName = "Default Weapon";
		title = NO_WEAPON;
		shortName = NO_WEAPON;
		animName = "Default Animation";
		cachedAnimation = Database.DEFAULT_ANIM_OBJ;
	}

	public WeaponObject( String blueprint )
	{
		blueprintName = blueprint;
	}

	@Override
	public String getIdentifier()
	{
		return blueprintName;
	}

	public void update()
	{
		// Nothing to do here
	}

	public void setAnimName( String animName )
	{
		if ( animName == null )
			throw new IllegalArgumentException( blueprintName + ": animation must not be null." );
		this.animName = animName;
		cacheAnimation();
	}

	public String getAnimName()
	{
		return animName;
	}

	public void cacheAnimation()
	{
		AnimationObject anim = Database.getInstance().getAnimation( animName );
		if ( anim == null )
			throw new IllegalArgumentException( "AnimationObject not found for anim name " + animName );
		cachedAnimation = anim;
	}

	public AnimationObject getAnimation()
	{
		return cachedAnimation;
	}

	public void setType( WeaponTypes type )
	{
		weaponType = type;
	}

	public WeaponTypes getType()
	{
		return weaponType;
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

	public void setStat( WeaponStats stat, float value )
	{
		if ( stat == null )
			throw new IllegalArgumentException( "Stat type must not be null." );
		if ( statMap == null )
			initStatMap();
		statMap.put( stat, value );
	}

	public float getStat( WeaponStats stat )
	{
		if ( stat == null )
			throw new IllegalArgumentException( "Stat type must not be null." );
		if ( statMap == null )
			initStatMap();
		return statMap.get( stat );
	}

	private void initStatMap()
	{
		statMap = new HashMap<WeaponStats, Float>();
		for ( WeaponStats stat : WeaponStats.values() )
			statMap.put( stat, 0f );
	}

	@Override
	public String toString()
	{
		return title.toString();
	}

	@Override
	public int compareTo( WeaponObject o )
	{
		return blueprintName.compareTo( o.blueprintName );
	}

	@Override
	public boolean equals( Object o )
	{
		if ( o instanceof WeaponObject ) {
			WeaponObject other = (WeaponObject)o;
			return blueprintName.equals( other.blueprintName );
		}
		else
			return false;
	}
}
