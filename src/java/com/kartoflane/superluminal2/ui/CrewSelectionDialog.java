package com.kartoflane.superluminal2.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.kartoflane.superluminal2.Superluminal;
import com.kartoflane.superluminal2.components.Hotkey;
import com.kartoflane.superluminal2.components.enums.CrewStats;
import com.kartoflane.superluminal2.components.enums.Hotkeys;
import com.kartoflane.superluminal2.components.interfaces.CrewLike;
import com.kartoflane.superluminal2.components.interfaces.Predicate;
import com.kartoflane.superluminal2.core.Manager;
import com.kartoflane.superluminal2.db.Database;
import com.kartoflane.superluminal2.ftl.CrewList;
import com.kartoflane.superluminal2.ftl.CrewObject;
import com.kartoflane.superluminal2.utils.UIUtils;


public class CrewSelectionDialog
{
	private static CrewSelectionDialog instance = null;

	private static final int defaultBlueTabWidth = 200;
	private static final int defaultNameTabWidth = 150;
	private static final int minTreeWidth = defaultBlueTabWidth + defaultNameTabWidth + 5;
	private static final int defaultDataWidth = 200;
	private static final Predicate<CrewObject> defaultFilter = new Predicate<CrewObject>() {
		public boolean accept( CrewObject object )
		{
			return true;
		}
	};

	private static CrewObject selection = Database.DEFAULT_CREW_OBJ;
	private static CrewList selectionList = Database.DEFAULT_CREW_LIST;

	private CrewObject result = null;
	private CrewList resultList = null;
	private int response = SWT.NO;

	private boolean sortByBlueprint = true;
	private Predicate<CrewObject> filter = defaultFilter;

	private Shell shell = null;
	private Text txtDesc;
	private StatTable statTable;
	private Button btnCancel;
	private Button btnConfirm;
	private Tree tree;
	private TreeColumn trclmnBlueprint;
	private TreeColumn trclmnName;
	private Button btnSearch;
	private Button btnSwitch;

	private final String btnSwitch_list = "Blueprint Lists";
	private final String btnSwitch_regular = "Single Crew Types";
	private boolean listsView = false;
	private boolean selectedEmpty = false;


	public CrewSelectionDialog( Shell parent )
	{
		if ( instance != null )
			throw new IllegalStateException( "Previous instance has not been disposed!" );
		instance = this;

		shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL );
		shell.setText( Superluminal.APP_NAME + " - Crew Selection" );
		shell.setLayout( new GridLayout( 1, false ) );

		SashForm sashForm = new SashForm( shell, SWT.SMOOTH );
		sashForm.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 1 ) );

		tree = new Tree( sashForm, SWT.BORDER | SWT.FULL_SELECTION );
		tree.setHeaderVisible( true );
		// remove the horizontal bar so that it doesn't flicker when the tree is resized
		tree.getHorizontalBar().dispose();

		trclmnBlueprint = new TreeColumn( tree, SWT.LEFT );
		trclmnBlueprint.setWidth( defaultBlueTabWidth );
		trclmnBlueprint.setText( "Blueprint" );

		trclmnName = new TreeColumn( tree, SWT.RIGHT );
		trclmnName.setWidth( defaultNameTabWidth );
		trclmnName.setText( "Name" );

		ScrolledComposite scrolledComposite = new ScrolledComposite( sashForm, SWT.BORDER | SWT.V_SCROLL );
		scrolledComposite.setExpandHorizontal( true );
		scrolledComposite.setExpandVertical( true );

		Composite compData = new Composite( scrolledComposite, SWT.NONE );
		GridLayout gl_compData = new GridLayout( 1, false );
		gl_compData.marginHeight = 0;
		gl_compData.marginWidth = 0;
		compData.setLayout( gl_compData );

		Label lblDescription = new Label( compData, SWT.NONE );
		lblDescription.setText( "Description:" );

		txtDesc = new Text( compData, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI );
		GridData gd_txtDesc = new GridData( SWT.FILL, SWT.FILL, true, true, 1, 1 );
		gd_txtDesc.heightHint = 60;
		txtDesc.setLayoutData( gd_txtDesc );
		scrolledComposite.setContent( compData );
		scrolledComposite.setMinSize( compData.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
		sashForm.setWeights( new int[] { minTreeWidth, defaultDataWidth } );

		statTable = new StatTable( compData );
		statTable.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 1, 1 ));

		Composite compButtons = new Composite( shell, SWT.NONE );
		GridLayout gl_compButtons = new GridLayout( 3, false );
		gl_compButtons.marginWidth = 0;
		gl_compButtons.marginHeight = 0;
		compButtons.setLayout( gl_compButtons );
		compButtons.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 1, 1 ) );

		btnSearch = new Button( compButtons, SWT.NONE );
		GridData gd_btnSearch = new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1 );
		gd_btnSearch.widthHint = 80;
		btnSearch.setLayoutData( gd_btnSearch );
		btnSearch.setText( "Search" );

		btnSwitch = new Button( compButtons, SWT.NONE );
		GridData gd_btnSwitch = new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1 );
		gd_btnSwitch.widthHint = 133;
		btnSwitch.setLayoutData( gd_btnSwitch );
		btnSwitch.setText( btnSwitch_list );

		btnConfirm = new Button( compButtons, SWT.NONE );
		GridData gd_btnConfirm = new GridData( SWT.RIGHT, SWT.CENTER, true, false, 1, 1 );
		gd_btnConfirm.widthHint = 80;
		btnConfirm.setLayoutData( gd_btnConfirm );
		btnConfirm.setText( "Confirm" );

		btnCancel = new Button( compButtons, SWT.NONE );
		GridData gd_btnCancel = new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1 );
		gd_btnCancel.widthHint = 80;
		btnCancel.setLayoutData( gd_btnCancel );
		btnCancel.setText( "Cancel" );

		tree.addMouseListener(
			new MouseListener() {
				@Override
				public void mouseDoubleClick( MouseEvent e )
				{
					if ( e.button == 1 && tree.getSelectionCount() != 0 ) {
						TreeItem selectedItem = tree.getSelection()[0];
						if ( selectedItem.getItemCount() == 0 && btnConfirm.isEnabled() )
							btnConfirm.notifyListeners( SWT.Selection, null );
						else if ( selectedItem.getBounds().contains( e.x, e.y ) )
							selectedItem.setExpanded( !selectedItem.getExpanded() );
					}
				}

				@Override
				public void mouseDown( MouseEvent e )
				{
				}

				@Override
				public void mouseUp( MouseEvent e )
				{
				}
			}
		);

		tree.addSelectionListener(
			new SelectionAdapter() {
				@Override
				public void widgetSelected( SelectionEvent e )
				{
					if ( tree.getSelectionCount() != 0 ) {
						TreeItem selectedItem = tree.getSelection()[0];
						Object o = selectedItem.getData();
						selectedEmpty = false;

						if ( o instanceof CrewList ) {
							selectionList = (CrewList)o;
							resultList = selectionList;
							result = null;
							if ( o.equals( Database.DEFAULT_CREW_LIST ) ) selectedEmpty = true;
							btnConfirm.setEnabled( listsView );
						}
						else if ( o instanceof CrewObject ) {
							selection = (CrewObject)o;
							result = selection;
							btnConfirm.setEnabled( !listsView );
						}
						else {
							result = null;
							btnConfirm.setEnabled( false );
						}
					}
					else {
						resultList = null;
						result = null;
						btnConfirm.setEnabled( false );
					}
					updateData();
				}
			}
		);

		trclmnBlueprint.addSelectionListener(
			new SelectionAdapter() {
				@Override
				public void widgetSelected( SelectionEvent e )
				{
					// Sort by blueprint name
					if ( !sortByBlueprint ) {
						sortByBlueprint = true;
						if ( !listsView ) updateTree();
					}
				}
			}
		);

		trclmnName.addSelectionListener(
			new SelectionAdapter() {
				@Override
				public void widgetSelected( SelectionEvent e )
				{
					// Sort by title
					if ( sortByBlueprint ) {
						sortByBlueprint = false;
						if ( !listsView ) updateTree();
					}
				}
			}
		);

		btnSearch.addSelectionListener(
			new SelectionAdapter() {
				@Override
				public void widgetSelected( SelectionEvent e )
				{
					CrewSearchDialog csDialog = new CrewSearchDialog( shell );
					Predicate<CrewObject> result = csDialog.open();

					if ( result == AbstractSearchDialog.RESULT_DEFAULT ) {
						filter = defaultFilter;
					}
					else if ( result == AbstractSearchDialog.RESULT_UNCHANGED ) {
						// Do nothing
					}
					else {
						filter = result;
					}

					updateTree();
					tree.notifyListeners( SWT.Selection, null );
				}
			}
		);

		btnSwitch.addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected( SelectionEvent e )
					{
						btnSearch.setEnabled( listsView );
						if ( btnSwitch.getText().equals( btnSwitch_list ) )
						{
							btnSwitch.setText( btnSwitch_regular );
							listsView = true;
							updateTreeList();
						}
						else
						{
							btnSwitch.setText( btnSwitch_list );
							listsView = false;
							updateTree();
						}
						tree.notifyListeners( SWT.Selection, null );
					}
				}
		);

		btnCancel.addSelectionListener(
			new SelectionAdapter() {
				@Override
				public void widgetSelected( SelectionEvent e )
				{
					response = SWT.NO;
					dispose();
				}
			}
		);

		btnConfirm.addSelectionListener(
			new SelectionAdapter() {
				@Override
				public void widgetSelected( SelectionEvent e )
				{
					response = SWT.YES;
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

		shell.addTraverseListener(
			new TraverseListener() {
				@Override
				public void keyTraversed( TraverseEvent e )
				{
					if ( e.detail == SWT.TRAVERSE_RETURN && tree.getSelectionCount() != 0 ) {
						TreeItem selectedItem = tree.getSelection()[0];
						if ( selectedItem.getItemCount() == 0 && btnConfirm.isEnabled() )
							btnConfirm.notifyListeners( SWT.Selection, null );
						else
							selectedItem.setExpanded( !selectedItem.getExpanded() );
					}
				}
			}
		);

		ControlAdapter resizer = new ControlAdapter() {
			@Override
			public void controlResized( ControlEvent e )
			{
				final int BORDER_OFFSET = tree.getBorderWidth();
				if ( trclmnBlueprint.getWidth() > tree.getClientArea().width - BORDER_OFFSET )
					trclmnBlueprint.setWidth( tree.getClientArea().width - BORDER_OFFSET );
				trclmnName.setWidth( tree.getClientArea().width - trclmnBlueprint.getWidth() - BORDER_OFFSET );
			}
		};
		tree.addControlListener( resizer );
		trclmnBlueprint.addControlListener( resizer );

		shell.setMinimumSize( minTreeWidth + defaultDataWidth, 300 );
		shell.pack();
		Point size = shell.getSize();
		shell.setSize( size.x + 5, size.y );
		Point parSize = parent.getSize();
		Point parLoc = parent.getLocation();
		shell.setLocation( parLoc.x + parSize.x / 3 - size.x / 2, parLoc.y + parSize.y / 3 - size.y / 2 );

		// Register hotkeys
		Hotkey h = new Hotkey( Manager.getHotkey( Hotkeys.SEARCH ) );
		h.addNotifyAction( btnSearch, true );
		Manager.hookHotkey( shell, h );
	}

	private void open()
	{
		response = SWT.NO;

		if ( listsView )
			updateTreeList();
		else
			updateTree();

		shell.open();

		updateData();

		Display display = UIUtils.getDisplay();
		while ( !shell.isDisposed() ) {
			if ( !display.readAndDispatch() )
				display.sleep();
		}
	}

	public CrewLike open( CrewLike current )
	{
		if ( current instanceof CrewObject )
		{
			return open( (CrewObject) current );
		}
		else if ( current instanceof CrewList )
		{
			return open( (CrewList) current );
		}
		else
		{
			throw new IllegalArgumentException(
					"Crew Selection Error: " + current.toString() + " is not a crew or list of crew"
			);
		}
	}

	public CrewLike open( CrewList current )
	{
		resultList = current;
		result = null;

		if ( current == null || current == Database.DEFAULT_CREW_LIST ) {
			resultList = selectionList;
		}
		else {
			selectionList = resultList;
		}

		btnSearch.setEnabled( true );
		btnSwitch.setEnabled( true );
		btnSwitch.notifyListeners( SWT.Selection, null );

		open();

		if ( response == SWT.YES ) {
			if ( selectedEmpty ) return Database.DEFAULT_CREW_OBJ;
			return listsView ? resultList : result;
		}
		else {
			return null;
		}
	}

	public CrewLike open( CrewObject current )
	{
		resultList = null;
		result = current;

		if ( current == null || current == Database.DEFAULT_CREW_OBJ ) {
			result = selection;
		}
		else {
			selection = result;
		}

		btnSearch.setEnabled( true );
		btnSwitch.setEnabled( true );

		open();

		if ( response == SWT.YES ) {
			if ( selectedEmpty ) return Database.DEFAULT_CREW_OBJ;
			return listsView ? resultList : result;
		}
		else {
			return null;
		}
	}

	private void updateTree()
	{
		for ( TreeItem trtm : tree.getItems() )
			trtm.dispose();

		TreeItem trtm = new TreeItem( tree, SWT.NONE );
		trtm.setText( "No Crew" );
		trtm.setData( Database.DEFAULT_CREW_OBJ );

		TreeItem selection = null;

		CrewIterator it = new CrewIterator( Database.getInstance().getCrews(), sortByBlueprint );
		for ( it.first(); it.hasNext(); it.next() ) {
			CrewObject crew = it.current();

			trtm = new TreeItem( tree, SWT.NONE );
			trtm.setText( 0, crew.getBlueprintName() );
			trtm.setText( 1, crew.getTitle().toString() );
			trtm.setData( crew );

			if ( result == crew )
				selection = trtm;
		}

		tree.layout();

		if ( selection != null ) {
			tree.select( selection );
			tree.setTopItem( selection );
		}
		else {
			tree.select( tree.getItem( 0 ) );
		}
	}

	private void updateTreeList()
	{
		for ( TreeItem trtm : tree.getItems() )
			trtm.dispose();

		TreeItem trtm = new TreeItem( tree, SWT.NONE );
		trtm.setText( "No Crew List" );
		trtm.setData( Database.DEFAULT_CREW_LIST );

		TreeItem selection = null;

		for ( CrewList list : Database.getInstance().getCrewLists() ) {
			trtm = new TreeItem( tree, SWT.NONE );
			trtm.setText( list.getBlueprintName() );
			trtm.setData( list );

			if ( resultList == list )
				selection = trtm;

			for ( CrewObject crew : list ) {
				TreeItem crewItem = new TreeItem( trtm, SWT.NONE );
				crewItem.setText( 0, crew.getBlueprintName() );
				crewItem.setText( 1, crew.getTitle().toString() );
				crewItem.setData( crew );
			}
		}

		tree.layout();

		if ( selection != null ) {
			tree.select( selection );
			tree.setTopItem( selection );
		}
		else {
			tree.select( tree.getItem( 0 ) );
		}
	}

	private void updateData()
	{
		if ( result != null ) {
			statTable.setVisible( false );
			statTable.clear();
			for ( CrewStats stat : CrewStats.values() ) {
				float value = result.getStat( stat );
				statTable.addEntry( stat.toString(), stat.formatValue( value ) );
			}
			statTable.setVisible( true );
			statTable.updateColumnWidth();
			txtDesc.setText( result.getDescription().toString() );
		}
		else {
			statTable.clear();
			txtDesc.setText( "" );
		}
	}

	public static CrewSelectionDialog getInstance()
	{
		return instance;
	}

	public void dispose()
	{
		Manager.unhookHotkeys( shell );
		shell.dispose();
		instance = null;
	}

	public boolean isActive()
	{
		return !shell.isDisposed() && shell.isVisible();
	}


	private class CrewIterator implements Iterator<CrewObject>
	{
		private final ArrayList<CrewObject> list;
		private final CrewComparator comparator;

		private CrewObject current = null;


		public CrewIterator( ArrayList<CrewObject> list, boolean byBlueprint )
		{
			comparator = new CrewComparator( byBlueprint );

			if ( filter == defaultFilter ) {
				this.list = list;
			}
			else {
				this.list = new ArrayList<CrewObject>();
				for ( CrewObject a : list ) {
					if ( filter.accept( a ) )
						this.list.add( a );
				}
			}
		}

		private CrewObject getSmallestElement()
		{
			CrewObject result = null;
			for ( CrewObject crew : list ) {
				if ( result == null || comparator.compare( crew, result ) < 0 )
					result = crew;
			}

			return result;
		}

		public void first()
		{
			current = getSmallestElement();
		}

		public CrewObject current()
		{
			return current;
		}

		@Override
		public boolean hasNext()
		{
			return !list.isEmpty();
		}

		@Override
		public CrewObject next()
		{
			remove();
			current = getSmallestElement();
			return current;
		}

		@Override
		public void remove()
		{
			list.remove( current );
		}
	}

	private class CrewComparator implements Comparator<CrewObject>
	{
		private final boolean byBlueprint;


		public CrewComparator( boolean byBlueprint )
		{
			this.byBlueprint = byBlueprint;
		}

		@Override
		public int compare( CrewObject o1, CrewObject o2 )
		{
			if ( byBlueprint ) {
				// Just compare the two blueprints together for alphanumerical ordering
				return o1.getBlueprintName().compareTo( o2.getBlueprintName() );
			}
			else {
				int result = o1.getTitle().compareTo( o2.getTitle() );
				if ( result == 0 ) // If titles are the same, fall back to sorting by blueprint
					result = o1.getBlueprintName().compareTo( o2.getBlueprintName() );
				return result;
			}
		}
	}
}
