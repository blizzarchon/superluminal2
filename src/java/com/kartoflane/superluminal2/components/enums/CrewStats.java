package com.kartoflane.superluminal2.components.enums;

public enum CrewStats
{
	/** The blueprint's rarity */
	RARITY,
	/** How much the weapon costs to buy */
	COST;

	public String getTagName()
	{
		return name().toLowerCase();
	}

	public String formatValue( float value )
	{
		switch ( this ) {
			case COST:
				return "" + (int)value;
			case RARITY:
				int r = (int)value;
				switch ( r ) {
					case 0:
						return "Unobtainable (0)";
					case 1:
						return "Common (1)";
					case 2:
						return "Uncommon (2)";
					case 3:
						return "Unusual (3)";
					case 4:
						return "Rare (4)";
					case 5:
						return "Very Rare (5)";
					default:
						throw new IllegalArgumentException( "Incorrect rarity value: " + value );
				}
			default:
				return "" + value;
		}
	}

	@Override
	public String toString()
	{
		switch ( this ) {
			case COST:
				return "Cost";
			default:
				String s = getTagName();
				s = s.substring( 0, 1 ).toUpperCase() + s.substring( 1 ).toLowerCase();
				return s;
		}
	}
}
