package com.kartoflane.superluminal2.ftl;

import com.kartoflane.superluminal2.components.interfaces.CrewLike;

public class CrewList extends BlueprintList<CrewObject> implements CrewLike
{
    private static final long serialVersionUID = 4618623391139370151L;
    private static final String empty = "No Crew List";

    /**
     * Creates the default crew list object.
     */
    public CrewList()
    {
        super( empty );
    }

    public CrewList( String blueprint )
    {
        super( blueprint );
    }

    @Override
    public String buttonView() {
        return blueprintName;
    }
}
