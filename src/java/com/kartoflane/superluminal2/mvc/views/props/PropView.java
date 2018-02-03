package com.kartoflane.superluminal2.mvc.views.props;

import org.eclipse.swt.events.PaintEvent;

import com.kartoflane.superluminal2.components.enums.Shapes;
import com.kartoflane.superluminal2.mvc.controllers.props.PropController;
import com.kartoflane.superluminal2.mvc.views.BaseView;


public class PropView extends BaseView
{
	protected Shapes shape = Shapes.RECTANGLE;


	public PropView()
	{
		super();
	}

	protected PropController getController()
	{
		return (PropController)controller;
	}

	@Override
	public void paintControl( PaintEvent e )
	{
		if ( alpha > 0 ) {
			switch ( shape ) {
				case LINE:
					// Do nothing.
					break;
				case RECTANGLE:
					paintBackgroundSquare( e, backgroundColor, alpha );
					break;
				case OVAL:
					paintBackgroundOval( e, backgroundColor, alpha );
					break;
				case POLYGON:
					paintBackgroundPolygon( e, getController().getPolygon().toArray(), backgroundColor, alpha );
					break;
			}

			paintImage( e, image, cachedImageBounds, alpha );

			switch ( shape ) {
				case LINE:
					int sx = controller.getParent().getX(),
						sy = controller.getParent().getY(),
						ex = controller.getX(),
						ey = controller.getY();

					paintLine( e, sx, sy, ex, ey, borderColor, borderThickness, alpha );
					break;
				case RECTANGLE:
					paintBorderSquare( e, borderColor, borderThickness, alpha );
					break;
				case OVAL:
					paintBorderOval( e, borderColor, borderThickness, alpha );
					break;
				case POLYGON:
					paintBorderPolygon( e, getController().getPolygon().toArray(), borderColor, borderThickness, alpha );
					break;
			}
		}
	}

	@Override
	public void updateView()
	{
		shape = getController().getShape();
		if ( controller.isSelected() ) {
			setBorderColor( controller.isPinned() ? PIN_RGB : SELECT_RGB );
		}
		else if ( controller.isHighlighted() ) {
			setBorderColor( HIGHLIGHT_RGB );
		}
		else {
			setBorderColor( defaultBorder );
			setBackgroundColor( defaultBackground );
		}
	}
}
