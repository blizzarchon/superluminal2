package com.kartoflane.superluminal2.mvc.controllers;

import java.util.ArrayList;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import com.kartoflane.superluminal2.components.Tuple;
import com.kartoflane.superluminal2.components.enums.Orientations;
import com.kartoflane.superluminal2.components.interfaces.Indexable;
import com.kartoflane.superluminal2.core.Grid;
import com.kartoflane.superluminal2.core.Grid.Snapmodes;
import com.kartoflane.superluminal2.core.LayeredPainter;
import com.kartoflane.superluminal2.core.LayeredPainter.Layers;
import com.kartoflane.superluminal2.core.Manager;
import com.kartoflane.superluminal2.events.SLEvent;
import com.kartoflane.superluminal2.ftl.RoomObject;
import com.kartoflane.superluminal2.mvc.View;
import com.kartoflane.superluminal2.mvc.models.ObjectModel;
import com.kartoflane.superluminal2.mvc.views.RoomView;
import com.kartoflane.superluminal2.tools.ManipulationTool;
import com.kartoflane.superluminal2.tools.Tool.Tools;
import com.kartoflane.superluminal2.ui.OverviewWindow;
import com.kartoflane.superluminal2.ui.ShipContainer;
import com.kartoflane.superluminal2.ui.SystemsMenu;
import com.kartoflane.superluminal2.ui.sidebar.data.DataComposite;
import com.kartoflane.superluminal2.ui.sidebar.data.RoomDataComposite;
import com.kartoflane.superluminal2.undo.UndoableResizeEdit;
import com.kartoflane.superluminal2.utils.Utils;


public class RoomController extends ObjectController implements Indexable, Comparable<RoomController>
{
	/**
	 * Tolerance greater than half of CELL_SIZE will result in negative size for 1x1 rooms,
	 * which will actually cause the collision area to grow.
	 * 
	 * @see {@link ShipContainer#CELL_SIZE}
	 * @see {@link AbstractController#collides(int, int, int, int)}
	 */
	public static final int COLLISION_TOLERANCE = ShipContainer.CELL_SIZE / 2;

	protected ShipContainer container = null;
	protected boolean resizing = false;
	protected boolean canResize = false;

	private Point resizeAnchor;


	private RoomController( ShipContainer container, ObjectModel model, RoomView view )
	{
		super();
		setModel( model );
		setView( view );
		this.container = container;

		resizeAnchor = new Point( 0, 0 );

		setSelectable( true );
		setLocModifiable( true );
		setBounded( true );
		setCollidable( !Manager.allowRoomOverlap );
		setTolerance( COLLISION_TOLERANCE );
		setParent( container.getShipController() );
		setPresentedFactor( ShipContainer.CELL_SIZE );
	}

	/**
	 * Creates a new object represented by the MVC system, ie.
	 * a new Controller associated with new Model and View objects
	 * 
	 * @param shipController
	 *            the ShipController object to which this room belongs
	 */
	public static RoomController newInstance( ShipContainer container, RoomObject object )
	{
		ObjectModel model = new ObjectModel( object );
		RoomView view = new RoomView();
		RoomController controller = new RoomController( container, model, view );

		OverviewWindow ow = OverviewWindow.getInstance();
		controller.addListener( SLEvent.DELETE, ow );
		controller.addListener( SLEvent.DISPOSE, ow );

		return controller;
	}

	public RoomObject getGameObject()
	{
		return (RoomObject)getModel().getGameObject();
	}

	@Override
	public void setView( View view )
	{
		super.setView( view );
		this.view.addToPainter( Layers.ROOM );
	}

	protected RoomView getView()
	{
		return (RoomView)view;
	}

	/**
	 * Change size of the object to the specified values.<br>
	 * 
	 * @param w
	 *            width of the room, in pixels
	 * @param h
	 *            height of the room, in pixels
	 */
	@Override
	public boolean setSize( int w, int h )
	{
		boolean result = super.setSize( w, h );

		if ( result ) {
			w = ( w + 1 ) / ShipContainer.CELL_SIZE;
			h = ( h + 1 ) / ShipContainer.CELL_SIZE;

			setSnapMode( getSnapMode( w, h ) );

			setCollidable( w != 0 && h != 0 && !Manager.allowRoomOverlap );
			updateBoundingArea();
			return true;
		}
		else
			return false;
	}

	private Snapmodes getSnapMode( int w, int h )
	{
		if ( w % 2 == 1 && h % 2 == 1 )
			return Snapmodes.CELL;
		else if ( w % 2 == 1 )
			return Snapmodes.EDGE_H;
		else if ( h % 2 == 1 )
			return Snapmodes.EDGE_V;
		else if ( w % 2 == 0 && h % 2 == 0 )
			return Snapmodes.CROSS;
		else
			return Snapmodes.CELL;
	}

	@Override
	public Point getPresentedLocation()
	{
		return new Point(
			( getX() - getW() / 2 ) / getPresentedFactor(),
			( getY() - getH() / 2 ) / getPresentedFactor()
		);
	}

	@Override
	public Point getPresentedSize()
	{
		return new Point( getW() / getPresentedFactor(), getH() / getPresentedFactor() );
	}

	@Override
	public void setPresentedLocation( int x, int y )
	{
		setLocation( x * getPresentedFactor() + getW() / 2, y * getPresentedFactor() + getH() / 2 );
	}

	@Override
	public void setPresentedSize( int w, int h )
	{
		Point presLoc = getPresentedLocation();
		if ( setSize( w * getPresentedFactor(), h * getPresentedFactor() ) ) {
			updateBoundingArea();
			setPresentedLocation( presLoc.x, presLoc.y );
		}
	}

	public void setId( int id )
	{
		getGameObject().setId( id );
	}

	public int getId()
	{
		return getGameObject().getId();
	}

	public boolean isResizing()
	{
		return resizing;
	}

	protected void setResizing( boolean res )
	{
		this.resizing = res;

		if ( res )
			( (ManipulationTool)Manager.getSelectedTool() ).setStateRoomResize();
		// Tool returns to normal state on its own
	}

	public boolean canResize()
	{
		return canResize;
	}

	public Point getResizeAnchor()
	{
		return Utils.copy( resizeAnchor );
	}

	@Override
	public void setHighlighted( boolean high )
	{
		super.setHighlighted( high && !selected );
	}

	@Override
	public void mouseDown( MouseEvent e )
	{
		if ( Manager.getSelectedToolId() == Tools.POINTER ) {
			if ( e.button == 1 && isSelected() && !isPinned() ) {
				for ( int i = 0; i < 4; i++ ) {
					if ( getView().getResizeHandles()[i].contains( e.x, e.y ) ) {
						resizeAnchor = Grid.getInstance().snapToGrid( getView().getResizeHandles()[3 - i].getCenter(), Snapmodes.CROSS );
						canResize = true;
						setResizing( true );

						break;
					}
				}
			}
			super.mouseDown( e );

			if ( currentEdit != null ) {
				Runnable a = new Runnable() {
					public void run()
					{
						container.getParent().updateSidebarContent();
						container.updateBoundingArea();
					}
				};
				currentEdit.setUndoCallback( a );
				currentEdit.setRedoCallback( a );
			}
		}
	}

	@Override
	public void mouseUp( MouseEvent e )
	{
		if ( Manager.getSelectedToolId() == Tools.POINTER ) {
			super.mouseUp( e );

			if ( e.button == 1 ) {
				if ( isResizing() ) {
					// Confirm resize
					setResizing( false );

					if ( canResize ) {
						// If the area is clear, resize and reposition the room
						Point resLoc = CursorController.getInstance().getLocation();
						Point resSize = CursorController.getInstance().getSize();

						int w = resSize.x;
						int h = resSize.y;
						w = ( w + 1 ) / ShipContainer.CELL_SIZE;
						h = ( h + 1 ) / ShipContainer.CELL_SIZE;
						resLoc = Grid.getInstance().snapToGrid( resLoc, getSnapMode( w, h ) );
						resSize = Grid.getInstance().snapToGrid( resSize, Snapmodes.CROSS );

						UndoableResizeEdit edit = new UndoableResizeEdit( this );
						edit.setOld( new Tuple<Point, Point>( getLocation(), getSize() ) );
						edit.setCurrent( new Tuple<Point, Point>( resLoc, resSize ) );

						setVisible( false );
						setSize( resSize );
						setLocation( resLoc );
						setVisible( true );

						setFollowOffset( getX() - getParent().getX(), getY() - getParent().getY() );

						container.getParent().updateSidebarContent();
						container.updateBoundingArea();

						Runnable a = new Runnable() {
							public void run()
							{
								container.getParent().updateSidebarContent();
								container.updateBoundingArea();
							}
						};
						edit.setUndoCallback( a );
						edit.setRedoCallback( a );
						container.postEdit( edit );
					}
					updateView();
				}
				container.getParent().updateSidebarContent();
			}
			else if ( e.button == 3 ) {
				if ( !isSelected() )
					Manager.setSelected( this );

				if ( resizing ) {
					setResizing( false );
					setMoving( false );
				}
				else if ( selected && getBounds().contains( e.x, e.y ) ) {
					// Open system popup menu
					SystemsMenu sysMenu = new SystemsMenu( container.getParent().getShell(), this );
					sysMenu.open();
				}
			}
		}
	}

	@Override
	public void mouseMove( MouseEvent e )
	{
		// super.mouseMove() only handles box movement -- we're implementing a different
		// handling of that functionality here, so we don't call super.mouseMove()
		if ( Manager.getSelectedToolId() == Tools.POINTER ) {
			if ( Manager.leftMouseDown && selected ) {
				if ( resizing ) {
					Rectangle resizeBounds = CursorController.getInstance().getDimensions();
					canResize = true;

					if ( resizeBounds.x < getParent().getX() || resizeBounds.y < getParent().getY() )
						canResize = false;

					// calculate collision with other boxes on the same layer
					ArrayList<AbstractController> layer = LayeredPainter.getInstance().getLayerMap().get( Layers.ROOM );
					for ( int i = 0; i < layer.size() && canResize; i++ ) {
						AbstractController c = layer.get( i );
						if ( c != this && c.isVisible() && c.collides( resizeBounds ) )
							canResize = false;
					}

					CursorController.getInstance().updateView();
				}
				else if ( moving ) {
					// Decide direction for mono-directional dragging
					if ( monoDirectionalDrag && lockDragTo == null ) {
						int d = Utils.distance( getX() + clickOffset.x, getY() + clickOffset.y, e.x, e.y );
						// Have the user drag more than 2px away from click point to determine the direction
						// Not noticeable to the user, but increases the chance of correct detection
						if ( d > 2 ) {
							Point click = new Point( getX() + clickOffset.x, getY() + clickOffset.y );
							Point cursor = new Point( e.x, e.y );
							double angle = ( Utils.angle( click, cursor ) + 90 ) % 360;
							if ( ( angle >= 315 || angle < 45 ) || ( angle >= 135 && angle < 225 ) ) {
								lockDragTo = Orientations.HORIZONTAL;
							}
							else {
								lockDragTo = Orientations.VERTICAL;
							}
						}
					}
					else {
						Point p = Grid.getInstance().snapToGrid(
							e.x - clickOffset.x - getW() / 2, e.y - clickOffset.y - getH() / 2, Snapmodes.CROSS
						);
						Rectangle checkBounds = new Rectangle( p.x, p.y, getW(), getH() );

						p = Grid.getInstance().snapToGrid( checkBounds.x + getW() / 2, checkBounds.y + getH() / 2, snapmode );
						if ( lockDragTo == Orientations.HORIZONTAL ) {
							p.y = getY();
						}
						else if ( lockDragTo == Orientations.VERTICAL ) {
							p.x = getX();
						}
						// proceed only if the position actually changed
						if ( p.x != getX() || p.y != getY() ) {
							reposition( p.x, p.y );
							updateView();
							updateFollowOffset();

							container.updateBoundingArea();
						}
					}
				}
			}
		}
	}

	@Override
	public int compareTo( RoomController o )
	{
		return getGameObject().compareTo( o.getGameObject() );
	}

	@Override
	public DataComposite getDataComposite( Composite parent )
	{
		return new RoomDataComposite( parent, this );
	}

	/**
	 * @param id
	 *            the slot id that is to be checked
	 * @return true if the room can contain station at the given slot, false otherwise.
	 */
	public boolean canContainSlotId( int slotId )
	{
		Rectangle bounds = getBounds();
		int w = bounds.width / ShipContainer.CELL_SIZE;
		int h = bounds.height / ShipContainer.CELL_SIZE;
		return w + ( h - 1 ) * w > slotId && slotId >= 0;
	}

	/**
	 * Returns the location of the given slot id relative to the room's top left corner.
	 * 
	 * @param slotId
	 *            id of the slot
	 * @return location of the slot relative to room's top left corner,
	 *         or null if the slot can't be contained
	 */
	public Point getSlotLocation( int slotId )
	{
		Point size = getSize();
		int w = size.x / ShipContainer.CELL_SIZE;
		int h = size.y / ShipContainer.CELL_SIZE;

		// can't contain the slot
		if ( w + ( h - 1 ) * w <= slotId || slotId < 0 )
			return null;

		int x = slotId % w;
		int y = slotId / w;

		return new Point(
			x * ShipContainer.CELL_SIZE + ShipContainer.CELL_SIZE / 2 + 1, y * ShipContainer.CELL_SIZE + ShipContainer.CELL_SIZE / 2 + 1
		);
	}

	/** Returns the slot id of the tile at the given coordinates (relative to the canvas). */
	public int getSlotId( int x, int y )
	{
		Rectangle bounds = getBounds();
		if ( !bounds.contains( x, y ) )
			return -1;

		x = ( x - bounds.x ) / ShipContainer.CELL_SIZE;
		y = ( y - bounds.y ) / ShipContainer.CELL_SIZE;

		return x + ( y * ( bounds.width / ShipContainer.CELL_SIZE ) );
	}

	@Override
	public void select()
	{
		super.select();
		updateView();
		redraw();
	}

	@Override
	public void deselect()
	{
		super.deselect();
		setMoving( false );
		setResizing( false );
		updateView();
		redraw();
	}

	@Override
	public void dispose()
	{
		if ( model.isDisposed() )
			return;

		super.dispose();
	}

	@Override
	public void updateBoundingArea()
	{
		Point gridSize = Grid.getInstance().getSize();
		gridSize.x -= ( gridSize.x % ShipContainer.CELL_SIZE ) + ShipContainer.CELL_SIZE;
		gridSize.y -= ( gridSize.y % ShipContainer.CELL_SIZE ) + ShipContainer.CELL_SIZE;
		// rooms with odd-numbered sizes bigger than 1 are misaligned by 1 pixel, add correction
		setBoundingPoints(
			getParent().getX() + getW() / 2, getParent().getY() + getH() / 2,
			gridSize.x - getW() / 2 - ( getW() > ShipContainer.CELL_SIZE && getW() % ( 2 * ShipContainer.CELL_SIZE ) != 0 ? 1 : 0 ),
			gridSize.y - getH() / 2 - ( getH() > ShipContainer.CELL_SIZE && getH() % ( 2 * ShipContainer.CELL_SIZE ) != 0 ? 1 : 0 )
		);
	}

	@Override
	public String toString()
	{
		RoomObject room = getGameObject();
		return room.toString();
	}
}
