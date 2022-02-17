package com.kartoflane.superluminal2.ftl;

import com.kartoflane.superluminal2.components.interfaces.Identifiable;


public class AugmentObject extends GameObject implements Comparable<AugmentObject>, Identifiable
{
	private static final IDeferredText NO_AUGMENT = new VerbatimText( "<No Augment>" );

	private final String blueprintName;
	private IDeferredText title = IDeferredText.EMPTY;
	private IDeferredText description = IDeferredText.EMPTY;
	public boolean isHidden;


	/** Creates a default augment object. */
	public AugmentObject()
	{
		blueprintName = "Default Augment";
		title = NO_AUGMENT;
		isHidden = false;
	}

	public AugmentObject( String blueprintName )
	{
		this.blueprintName = blueprintName;
	}

	public void setHidden(boolean hidden)
	{
		isHidden = hidden;
	}
	
	public String getIdentifier()
	{
		return blueprintName;
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

	@Override
	public int compareTo( AugmentObject o )
	{
		return blueprintName.compareTo( o.getBlueprintName() );
	}

	@Override
	public boolean equals( Object o )
	{
		if ( o instanceof AugmentObject ) {
			AugmentObject other = (AugmentObject)o;
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
