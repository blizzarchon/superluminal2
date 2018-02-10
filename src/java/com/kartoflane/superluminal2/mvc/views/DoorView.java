package com.kartoflane.superluminal2.mvc.views;

import org.eclipse.swt.events.PaintEvent;

import com.kartoflane.superluminal2.ftl.DoorObject;
import com.kartoflane.superluminal2.mvc.models.ObjectModel;


public class DoorView extends BaseView
{
	public DoorView()
	{
		super();

		setImage( "cpath:/assets/door_object.png" );
		setBorderColor( null );
		setBackgroundColor( null );
		setBorderThickness( 2 );
	}

	private ObjectModel getModel()
	{
		return (ObjectModel)model;
	}

	private DoorObject getGameObject()
	{
		return (DoorObject)getModel().getGameObject();
	}

	@Override
	public void paintControl( PaintEvent e )
	{
		if ( alpha > 0 ) {
			paintImage( e, image, cachedImageBounds, alpha );
			paintBorderSquare( e, borderColor, getBorderThickness(), alpha );
		}
	}

	@Override
	public void updateView()
	{
		if ( controller.isSelected() ) {
			setBorderColor( controller.isPinned() ? PIN_RGB : SELECT_RGB );
			setBackgroundColor( HIGHLIGHT_RGB );
			setBorderThickness( 2 );
		}
		else if ( controller.isHighlighted() ) {
			setBorderColor( controller.isPinned() ? PIN_HIGHLIGHT_RGB : HIGHLIGHT_RGB );
			setBackgroundColor( defaultBackground );
			setBorderThickness( 3 );
		}
		else {
			setBorderColor( defaultBorder );
			setBackgroundColor( defaultBackground );
			setBorderThickness( 2 );
		}

		setRotation( getGameObject().isHorizontal() ? 0 : 90 );
	}

	@Override
	public void dispose()
	{
		super.dispose();
	}
}
