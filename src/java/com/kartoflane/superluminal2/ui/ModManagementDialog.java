package com.kartoflane.superluminal2.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.kartoflane.superluminal2.Superluminal;
import com.kartoflane.superluminal2.components.Hotkey;
import com.kartoflane.superluminal2.core.Cache;
import com.kartoflane.superluminal2.core.Manager;
import com.kartoflane.superluminal2.db.AbstractDatabaseEntry;
import com.kartoflane.superluminal2.db.Database;
import com.kartoflane.superluminal2.db.ModDatabaseEntry;
import com.kartoflane.superluminal2.utils.UIUtils;
import com.kartoflane.superluminal2.utils.Utils;


public class ModManagementDialog
{
	private static final Logger log = LogManager.getLogger( ModManagementDialog.class );

	private static ModManagementDialog instance = null;
	private static String prevPath = null;

	private ArrayList<AbstractDatabaseEntry> entries = null;

	private TreeItem dragItem = null;
	private AbstractDatabaseEntry dragData = null;

	private Shell shell;
	private Tree tree;
	private Button btnRemove;
	private Button btnLoad;
	private TreeItem trtmCore;
	private Color disabledColor = null;
	private Button btnConfirm;
	private Button btnCancel;
	private DragSource dragSource;
	private DropTarget dropTarget;


	public ModManagementDialog( Shell parent )
	{
		if ( instance != null )
			throw new IllegalStateException( "Previous instance has not been disposed!" );
		instance = this;

		final Database db = Database.getInstance();
		entries = new ArrayList<AbstractDatabaseEntry>();

		shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL );
		shell.setText( Superluminal.APP_NAME + " - Mod Management" );
		shell.setLayout( new GridLayout( 4, false ) );

		tree = new Tree( shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI );
		tree.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1 ) );

		trtmCore = new TreeItem( tree, SWT.NONE );
		trtmCore.setText( "Base Game" );
		trtmCore.setData( db.getCore() );
		RGB rgb = trtmCore.getBackground().getRGB();
		rgb.red = (int)( 0.85 * rgb.red );
		rgb.green = (int)( 0.85 * rgb.green );
		rgb.blue = (int)( 0.85 * rgb.blue );
		disabledColor = Cache.checkOutColor( this, rgb );
		trtmCore.setBackground( disabledColor );

		// Need to specify a transfer type, even if it's not used, because
		// otherwise it's not even possible to initiate drag and drop...
		Transfer[] sourceTypes = new Transfer[] { TextTransfer.getInstance() };
		dragSource = new DragSource( tree, DND.DROP_MOVE );
		dragSource.setTransfer( sourceTypes );

		Transfer[] dropTypes = new Transfer[] { TextTransfer.getInstance(), FileTransfer.getInstance() };
		dropTarget = new DropTarget( tree, DND.DROP_MOVE | DND.DROP_DEFAULT );
		dropTarget.setTransfer( dropTypes );

		btnLoad = new Button( shell, SWT.NONE );
		GridData gd_btnLoad = new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1 );
		gd_btnLoad.widthHint = 80;
		btnLoad.setLayoutData( gd_btnLoad );
		btnLoad.setText( "Load Mod" );

		btnRemove = new Button( shell, SWT.NONE );
		GridData gd_btnRemove = new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1 );
		gd_btnRemove.widthHint = 80;
		btnRemove.setLayoutData( gd_btnRemove );
		btnRemove.setText( "Remove" );
		btnRemove.setEnabled( false );

		btnConfirm = new Button( shell, SWT.NONE );
		GridData gd_btnConfirm = new GridData( SWT.RIGHT, SWT.CENTER, true, false, 1, 1 );
		gd_btnConfirm.widthHint = 80;
		btnConfirm.setLayoutData( gd_btnConfirm );
		btnConfirm.setText( "Confirm" );

		btnCancel = new Button( shell, SWT.NONE );
		GridData gd_btnCancel = new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1 );
		gd_btnCancel.widthHint = 80;
		btnCancel.setLayoutData( gd_btnCancel );
		btnCancel.setText( "Cancel" );

		for ( AbstractDatabaseEntry de : db.getEntries() ) {
			if ( de == db.getCore() )
				continue;
			createTreeItem( de );
			entries.add( de );
		}

		dragSource.addDragListener(
			new DragSourceListener() {
				@Override
				public void dragStart( DragSourceEvent e )
				{
					TreeItem[] selection = tree.getSelection();
					if ( selection.length > 0 && selection[0].getItemCount() == 0 && selection[0] != trtmCore ) {
						e.doit = true;
						dragItem = selection[0];
					}
					else {
						e.doit = false;
					}
				}

				@Override
				public void dragSetData( DragSourceEvent e )
				{
					e.data = "whatever"; // This needs not be an empty string, otherwise the drag mechanism freaks out...
					dragData = (AbstractDatabaseEntry)dragItem.getData();
				}

				@Override
				public void dragFinished( DragSourceEvent e )
				{
					if ( dragItem != null && dragItem.getData() == null )
						dragItem.dispose();
					dragItem = null;
					dragData = null;
				}
			}
		);

		dropTarget.addDropListener(
			new DropTargetAdapter() {
				@Override
				public void dragOver( DropTargetEvent e )
				{
					e.detail = DND.DROP_MOVE;
					e.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
					if ( dragItem != null ) {
						Point p = tree.toControl( e.x, e.y );
						TreeItem item = tree.getItem( p );
						if ( item == null )
							item = tree.getItem( tree.getItemCount() - 1 );

						Rectangle bounds = item.getBounds();
						if ( p.y < bounds.y + bounds.height / 2 ) {
							if ( item == trtmCore ) {
								e.detail = DND.DROP_NONE;
								e.feedback = DND.FEEDBACK_NONE;
							}
							else {
								e.feedback |= DND.FEEDBACK_INSERT_BEFORE;
							}
						}
						else {
							e.feedback |= DND.FEEDBACK_INSERT_AFTER;
						}
					}
					else {
						Object object = FileTransfer.getInstance().nativeToJava( e.currentDataType );
						if ( object instanceof String[] ) {
							String fileList[] = (String[])object;
							for ( String path : fileList ) {
								if ( !path.endsWith( ".ftl" ) && !path.endsWith( ".zip" ) ) {
									e.detail = DND.DROP_NONE;
									e.feedback = DND.FEEDBACK_NONE;
									break;
								}
							}
						}
						else {
							e.detail = DND.DROP_NONE;
							e.feedback = DND.FEEDBACK_NONE;
						}
					}
				}

				@Override
				public void drop( DropTargetEvent e )
				{
					if ( dragData == null ) {
						Object object = FileTransfer.getInstance().nativeToJava( e.currentDataType );
						if ( object instanceof String[] ) {
							String fileList[] = (String[])object;
							// Already ensured that all items are .ftl or .zip
							for ( String path : fileList ) {
								addEntry( new File( path ) );
							}
						}
					}
					else if ( dragData != db.getCore() ) {
						if ( dragItem == null ) {
							createTreeItem( dragData );
						}
						else {
							Point p = tree.toControl( e.x, e.y );
							TreeItem item = tree.getItem( p );
							if ( item == null )
								item = tree.getItem( tree.getItemCount() - 1 );

							Rectangle bounds = item.getBounds();
							TreeItem[] items = tree.getItems();
							int index = indexOf( items, item );

							if ( p.y < bounds.y + bounds.height / 2 ) {
								if ( item != trtmCore ) {
									dragItem.setData( null );
									createTreeItem( dragData, index );
									entries.remove( dragData );
									entries.add( index - 1, dragData );
								}
							}
							else {
								if ( index == items.length )
									index--;
								dragItem.setData( null );
								createTreeItem( dragData, index + 1 );
								entries.remove( dragData );
								entries.add( Utils.limit( 0, index - 1, items.length ), dragData );
							}
						}
					}
				}
			}
		);

		tree.addSelectionListener(
			new SelectionAdapter() {
				@Override
				public void widgetSelected( SelectionEvent e )
				{
					if ( Utils.contains( tree.getSelection(), trtmCore ) )
						tree.deselect( trtmCore );
					btnRemove.setEnabled( tree.getSelectionCount() > 0 );
				}
			}
		);

		btnLoad.addSelectionListener(
			new SelectionAdapter() {
				@Override
				public void widgetSelected( SelectionEvent e )
				{
					FileDialog dialog = new FileDialog( shell, SWT.OPEN | SWT.MULTI );
					dialog.setFilterExtensions( new String[] { "*.zip;*.ftl" } );
					dialog.setFilterPath( prevPath );
					dialog.setFileName( prevPath );

					dialog.setText( "Load Mod" );

					String path = dialog.open();
					File[] results = null;
					if ( path == null ) {
						// User aborted selection
						// Nothing to do here
					}
					else {
						String[] paths = dialog.getFileNames();
						results = new File[paths.length];
						for ( int i = 0; i < paths.length; i++ ) {
							results[i] = new File( dialog.getFilterPath() + "/" + paths[i] );
						}
					}

					if ( results != null ) {
						for ( File f : results ) {
							prevPath = f.getParent();
							addEntry( f );
						}
					}
				}
			}
		);

		btnRemove.addSelectionListener(
			new SelectionAdapter() {
				@Override
				public void widgetSelected( SelectionEvent e )
				{
					for ( TreeItem trtm : tree.getSelection() ) {
						AbstractDatabaseEntry de = (AbstractDatabaseEntry)trtm.getData();
						if ( de == db.getCore() ) // Redundant check to make sure that the core database is never unloaded
							continue;
						trtm.dispose();
						entries.remove( de );
						de = null;
					}
					btnRemove.setEnabled( tree.getSelectionCount() > 0 );
				}
			}
		);

		btnConfirm.addSelectionListener(
			new SelectionAdapter() {
				@Override
				public void widgetSelected( SelectionEvent e )
				{
					UIUtils.showLoadDialog(
						shell, null, "Loading mods, please wait...", new Runnable() {
							public void run()
							{
								// Load added entries
								AbstractDatabaseEntry[] dbEntries = db.getEntries();
								for ( AbstractDatabaseEntry de : entries ) {
									if ( de == db.getCore() )
										continue;
									if ( !Utils.contains( dbEntries, de ) )
										db.addEntry( de );
								}
								// Unload deleted entries
								for ( AbstractDatabaseEntry de : db.getEntries() ) {
									if ( de == db.getCore() )
										continue;
									if ( !entries.contains( de ) )
										db.removeEntry( de );
								}
								// Reorder entries to match user input
								for ( AbstractDatabaseEntry de : entries ) {
									if ( de == db.getCore() )
										continue;
									db.reorderEntry( de, entries.indexOf( de ) + 1 );
								}
								db.cacheAnimations();
							}
						}
					);
					dispose();
				}
			}
		);

		btnCancel.addSelectionListener(
			new SelectionAdapter() {
				@Override
				public void widgetSelected( SelectionEvent e )
				{
					dispose();
				}
			}
		);

		shell.addListener(
			SWT.Close, new Listener() {
				@Override
				public void handleEvent( Event e )
				{
					btnCancel.notifyListeners( SWT.Selection, null );
					e.doit = false;
				}
			}
		);

		shell.setSize( 450, 220 );
		Point size = shell.getSize();
		Point parSize = parent.getSize();
		Point parLoc = parent.getLocation();
		shell.setLocation( parLoc.x + parSize.x / 3 - size.x / 2, parLoc.y + parSize.y / 3 - size.y / 2 );

		// Register hotkeys
		Hotkey h = new Hotkey();
		h.setKey( SWT.CR );
		h.addNotifyAction( btnConfirm, true );
		Manager.hookHotkey( shell, h );

		h = new Hotkey();
		h.setKey( 'l' );
		h.setCtrl( true );
		h.addNotifyAction( btnLoad, true );
		Manager.hookHotkey( shell, h );

		h = new Hotkey();
		h.setKey( SWT.DEL );
		h.addNotifyAction( btnRemove, true );
		Manager.hookHotkey( shell, h );
	}

	public static ModManagementDialog getInstance()
	{
		return instance;
	}

	public void open()
	{
		shell.open();
	}

	public void addEntry( File file )
	{
		if ( !file.getAbsolutePath().endsWith( ".ftl" ) && !file.getAbsolutePath().endsWith( ".zip" ) )
			throw new IllegalArgumentException( "Argument must be a .zip or .ftl file." );

		try {
			AbstractDatabaseEntry de = new ModDatabaseEntry( file );
			if ( !entries.contains( de ) ) {
				createTreeItem( de );
				entries.add( de );
			}
		}
		catch ( IOException ex ) {
			log.warn( String.format( "An error has occurred while loading mod file '%s': ", file.getName(), ex ) );
		}
	}

	public void dispose()
	{
		Manager.unhookHotkeys( shell );
		if ( disabledColor != null )
			Cache.checkInColor( this, disabledColor.getRGB() );
		disabledColor = null;
		shell.dispose();
		instance = null;
	}

	public boolean isActive()
	{
		return !shell.isDisposed() && shell.isVisible();
	}

	private TreeItem createTreeItem( AbstractDatabaseEntry de )
	{
		return createTreeItem( de, tree.getItemCount() );
	}

	private TreeItem createTreeItem( AbstractDatabaseEntry de, int index )
	{
		TreeItem trtm = new TreeItem( tree, SWT.NONE, index );
		trtm.setText( de.getName() );
		trtm.setData( de );
		return trtm;
	}

	private int indexOf( TreeItem[] items, TreeItem item )
	{
		if ( items == null )
			throw new IllegalArgumentException( "Array must not be null." );
		if ( item == null )
			throw new IllegalArgumentException( "Item must not be null." );
		int result = -1;
		for ( int i = 0; i < items.length && result == -1; i++ ) {
			if ( items[i] == item )
				result = i;
		}
		return result;
	}
}
