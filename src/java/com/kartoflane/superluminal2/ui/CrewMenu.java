package com.kartoflane.superluminal2.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;

import com.kartoflane.superluminal2.components.enums.Races;
import com.kartoflane.superluminal2.utils.UIUtils;


public class CrewMenu
{
	private static CrewMenu instance;

	private String result = null;

	private Menu crewMenu;
	private MenuItem mntmNoCrew;


	public CrewMenu( Control parent )
	{
		if ( instance != null )
			throw new IllegalStateException( "Previous instance has not been disposed!" );
		instance = this;
		crewMenu = new Menu( parent );

		SelectionAdapter listener = new SelectionAdapter() {
			@Override
			public void widgetSelected( SelectionEvent e )
			{
				result = (String)( (Widget)e.getSource() ).getData();
			}
		};

		mntmNoCrew = new MenuItem( crewMenu, SWT.NONE );
		mntmNoCrew.setText( "no_crew" );
		mntmNoCrew.setData( "no_crew" );
		mntmNoCrew.addSelectionListener( listener );

		new MenuItem( crewMenu, SWT.SEPARATOR );

		for ( String race : Races.getPlayerRacesAliases() ) {
			MenuItem item = new MenuItem( crewMenu, SWT.NONE );
			item.setText( race );
			item.setData( race );
			item.addSelectionListener( listener );
		}
	}

	public static CrewMenu getInstance()
	{
		return instance;
	}

	public String open()
	{
		crewMenu.setVisible( true );

		Display display = UIUtils.getDisplay();
		while ( crewMenu.isVisible() ) {
			if ( !display.readAndDispatch() )
				display.sleep();
		}

		dispose();
		return result;
	}

	public void setLocation( int x, int y )
	{
		crewMenu.setLocation( x, y );
	}

	public void setLocation( Point p )
	{
		crewMenu.setLocation( p.x, p.y );
	}

	public void dispose()
	{
		crewMenu.dispose();
		instance = null;
	}

	public boolean isDisposed()
	{
		return crewMenu == null || crewMenu.isDisposed();
	}
}
