package com.kartoflane.superluminal2.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.jdom2.input.JDOMParseException;

import com.kartoflane.superluminal2.Superluminal;
import com.kartoflane.superluminal2.components.ShipMetadata;
import com.kartoflane.superluminal2.core.Database;
import com.kartoflane.superluminal2.core.Manager;
import com.kartoflane.superluminal2.core.ShipUtils;
import com.kartoflane.superluminal2.ftl.ShipObject;

public class ShipLoaderDialog {
	private static ShipLoaderDialog instance = null;
	private static final Logger log = LogManager.getLogger(ShipLoaderDialog.class);

	private static final int minTreeWidth = 400;
	private static final int defaultBlueTabWidth = 200;
	private static final int defaultClassTabWidth = 200;
	private static final int defaultMetadataWidth = 250;

	private HashMap<ShipMetadata, TreeItem> dataTreeMap = new HashMap<ShipMetadata, TreeItem>();
	private HashMap<String, TreeItem> blueprintTreeMap = new HashMap<String, TreeItem>();

	private boolean sortByBlueprint = true;

	private Shell shell;
	private Text txtBlueprint;
	private Text txtClass;
	private Text txtName;
	private Text txtDescription;
	private TreeItem trtmPlayer;
	private TreeItem trtmEnemy;
	private Tree tree;
	private Button btnLoad;

	public ShipLoaderDialog(Shell parent) {
		instance = this;

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText(Superluminal.APP_NAME + " - Ship Loader");
		shell.setLayout(new GridLayout(2, false));

		tree = new Tree(shell, SWT.BORDER | SWT.FULL_SELECTION);
		GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_tree.widthHint = minTreeWidth;
		tree.setLayoutData(gd_tree);
		tree.setHeaderVisible(true);

		TreeColumn trclmnBlueprint = new TreeColumn(tree, SWT.LEFT);
		trclmnBlueprint.setMoveable(true);
		trclmnBlueprint.setWidth(defaultBlueTabWidth);
		trclmnBlueprint.setText("Blueprint Name");

		TreeColumn trclmnClass = new TreeColumn(tree, SWT.RIGHT);
		trclmnClass.setMoveable(true);
		trclmnClass.setWidth(defaultClassTabWidth);
		trclmnClass.setText("Ship Class");

		trtmPlayer = new TreeItem(tree, SWT.NONE);
		trtmPlayer.setText(0, "Player Ships");
		trtmPlayer.setText(1, "");

		trtmEnemy = new TreeItem(tree, 0);
		trtmEnemy.setText(0, "Enemy Ships");
		trtmEnemy.setText(1, "");

		Composite metadataComposite = new Composite(shell, SWT.BORDER);
		metadataComposite.setLayout(new GridLayout(2, false));
		GridData gd_metadataComposite = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_metadataComposite.widthHint = defaultMetadataWidth;
		metadataComposite.setLayoutData(gd_metadataComposite);

		Label lblBlueprint = new Label(metadataComposite, SWT.NONE);
		lblBlueprint.setText("Blueprint:");

		txtBlueprint = new Text(metadataComposite, SWT.BORDER | SWT.READ_ONLY);
		txtBlueprint.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		Label lblClass = new Label(metadataComposite, SWT.NONE);
		lblClass.setText("Class:");

		txtClass = new Text(metadataComposite, SWT.BORDER | SWT.READ_ONLY);
		txtClass.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblName = new Label(metadataComposite, SWT.NONE);
		lblName.setText("Name:");

		txtName = new Text(metadataComposite, SWT.BORDER | SWT.READ_ONLY);
		txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblDescription = new Label(metadataComposite, SWT.NONE);
		lblDescription.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblDescription.setText("Description:");

		txtDescription = new Text(metadataComposite, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_txtDescription = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		gd_txtDescription.heightHint = 100;
		txtDescription.setLayoutData(gd_txtDescription);

		Composite buttonComposite = new Composite(shell, SWT.NONE);
		GridLayout gl_buttonComposite = new GridLayout(2, false);
		gl_buttonComposite.marginWidth = 0;
		gl_buttonComposite.marginHeight = 0;
		buttonComposite.setLayout(gl_buttonComposite);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		btnLoad = new Button(buttonComposite, SWT.NONE);
		GridData gd_btnLoad = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		gd_btnLoad.widthHint = 80;
		btnLoad.setLayoutData(gd_btnLoad);
		btnLoad.setText("Load");
		btnLoad.setEnabled(false);

		Button btnCancel = new Button(buttonComposite, SWT.NONE);
		GridData gd_btnCancel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnCancel.widthHint = 80;
		btnCancel.setLayoutData(gd_btnCancel);
		btnCancel.setText("Cancel");
		shell.setMinimumSize(minTreeWidth + defaultMetadataWidth, 300);

		tree.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (e.button == 1 && tree.getSelectionCount() != 0) {
					TreeItem selectedItem = tree.getSelection()[0];

					if (selectedItem.getItemCount() == 0 && btnLoad.isEnabled())
						btnLoad.notifyListeners(SWT.Selection, null);
					else
						selectedItem.setExpanded(!selectedItem.getExpanded());
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}
		});

		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tree.getSelectionCount() != 0) {
					TreeItem selectedItem = tree.getSelection()[0];
					ShipMetadata metadata = (ShipMetadata) selectedItem.getData();

					if (metadata == null) {
						btnLoad.setEnabled(false);
						txtBlueprint.setText("");
						txtClass.setText("");
						txtName.setText("");
						txtDescription.setText("");
					} else {
						btnLoad.setEnabled(true);
						txtBlueprint.setText(metadata.getBlueprintName());
						txtClass.setText(metadata.getShipClass());
						if (metadata.isPlayerShip()) {
							txtName.setText(metadata.getShipName());
							txtDescription.setText(metadata.getShipDescription());
						} else {
							txtName.setText("N/A");
							txtDescription.setText("N/A");
						}
					}
				} else {
					btnLoad.setEnabled(false);
				}
			}
		});

		trclmnBlueprint.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// sort by blueprint name
				if (!sortByBlueprint) {
					sortByBlueprint = true;
					loadShipList(Database.getInstance().getShipMetadata());
				}
			}
		});

		trclmnClass.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// sort by class name
				if (sortByBlueprint) {
					sortByBlueprint = false;
					loadShipList(Database.getInstance().getShipMetadata());
				}
			}
		});

		btnLoad.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tree.getSelectionCount() != 0) {
					TreeItem selectedItem = tree.getSelection()[0];
					ShipMetadata metadata = (ShipMetadata) selectedItem.getData();
					try {
						ShipObject object = ShipUtils.loadShipXML(metadata.getElement());

						Manager.loadShip(object);
					} catch (IOException ex) { // Multi-catch parameters were introduced in Java7 :(
						handleException(metadata, ex);
					} catch (NumberFormatException ex) {
						handleException(metadata, ex);
					} catch (IllegalArgumentException ex) {
						handleException(metadata, ex);
					} catch (JDOMParseException ex) {
						handleException(metadata, ex);
					}
				}
			}
		});

		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.setVisible(false);
			}
		});

		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event e) {
				shell.setVisible(false);
				e.doit = false;
			}
		});

		shell.pack();
	}

	public void loadShipList(HashMap<String, ArrayList<ShipMetadata>> metadataMap) {
		for (TreeItem it : dataTreeMap.values())
			it.dispose();
		for (TreeItem it : blueprintTreeMap.values())
			it.dispose();
		dataTreeMap.clear();
		blueprintTreeMap.clear();

		Database db = Database.getInstance();

		MetadataIterator it = new MetadataIterator(metadataMap, sortByBlueprint);
		for (it.first(); it.hasNext(); it.next()) {
			String blueprint = it.current();
			boolean isPlayer = db.isPlayerShip(blueprint);

			TreeItem blueprintItem = new TreeItem(isPlayer ? trtmPlayer : trtmEnemy, SWT.NONE);
			blueprintItem.setText(0, blueprint);
			blueprintItem.setText(1, "");

			ArrayList<ShipMetadata> dataList = metadataMap.get(blueprint);

			if (dataList.size() == 1) {
				ShipMetadata metadata = dataList.get(0);

				blueprintItem.setData(metadata);
				blueprintItem.setText(1, metadata.getShipClass());
				dataTreeMap.put(metadata, blueprintItem);
			} else {
				for (ShipMetadata metadata : dataList) {
					TreeItem metadataItem = new TreeItem(blueprintItem, SWT.NONE);

					metadataItem.setText(0, blueprint);
					metadataItem.setText(1, metadata.getShipClass());
					metadataItem.setData(metadata);
					dataTreeMap.put(metadata, metadataItem);
				}
			}

			blueprintTreeMap.put(blueprint, blueprintItem);
		}
	}

	public void open() {
		shell.open();
	}

	public static ShipLoaderDialog getInstance() {
		return instance;
	}

	private void handleException(ShipMetadata metadata, Exception ex) {
		log.error("An error has occured while loading " + metadata.getBlueprintName() + ":", ex);

		MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
		box.setText(Superluminal.APP_NAME + " - Loading Failed");

		StringBuilder buf = new StringBuilder();
		buf.append(String.format("%s could not be loaded:", metadata.getBlueprintName()));
		buf.append("\n\n");
		buf.append(ex.getMessage());

		box.setMessage(buf.toString());
		box.open();
	}

	private class MetadataIterator implements Iterator<String> {
		private final HashMap<String, ArrayList<ShipMetadata>> map;
		private final MetadataComparator comparator;

		private String current = null;

		public MetadataIterator(HashMap<String, ArrayList<ShipMetadata>> map, boolean byBlueprint) {
			comparator = new MetadataComparator(map, byBlueprint);
			this.map = map;
		}

		private String getLargestElement() {
			String result = null;
			for (String blueprint : map.keySet()) {
				if (result == null || comparator.compare(blueprint, result) < 0)
					result = blueprint;
			}

			return result;
		}

		public void first() {
			current = getLargestElement();
		}

		public String current() {
			return current;
		}

		@Override
		public boolean hasNext() {
			return !map.isEmpty();
		}

		@Override
		public String next() {
			remove();
			current = getLargestElement();
			return current;
		}

		@Override
		public void remove() {
			map.remove(current);
		}
	}

	private class MetadataComparator implements Comparator<String> {

		private final boolean byBlueprint;
		private final HashMap<String, ArrayList<ShipMetadata>> map;

		public MetadataComparator(HashMap<String, ArrayList<ShipMetadata>> map, boolean byBlueprint) {
			this.byBlueprint = byBlueprint;
			this.map = map;
		}

		@Override
		public int compare(String o1, String o2) {
			if (byBlueprint) {
				// Just compare the two blueprints together for alphanumerical ordering
				return o1.compareTo(o2);
			} else {
				// If there are multiple ships overriding the same blueprint, sorting by class name
				// becomes tricky. Take class name of the default ship and sort by that.
				ShipMetadata m1, m2;
				m1 = map.get(o1).get(0);
				m2 = map.get(o2).get(0);
				int result = m1.getShipClass().compareTo(m2.getShipClass());
				if (result == 0) // if class names are the same, fall back to sorting by blueprint
					result = o1.compareTo(o2);
				return result;
			}
		}
	}
}
