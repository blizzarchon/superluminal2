package com.kartoflane.superluminal2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.JDOMParseException;

import com.kartoflane.superluminal2.components.Hotkey;
import com.kartoflane.superluminal2.components.enums.Hotkeys;
import com.kartoflane.superluminal2.components.enums.OS;
import com.kartoflane.superluminal2.core.KeyboardInputDispatcher;
import com.kartoflane.superluminal2.core.Manager;
import com.kartoflane.superluminal2.core.SuperluminalConfig;
import com.kartoflane.superluminal2.core.UpdateCheckWorker;
import com.kartoflane.superluminal2.db.AbstractDatabaseEntry;
import com.kartoflane.superluminal2.db.Database;
import com.kartoflane.superluminal2.db.ModDatabaseEntry;
import com.kartoflane.superluminal2.ui.EditorWindow;
import com.kartoflane.superluminal2.ui.ShipContainer;
import com.kartoflane.superluminal2.utils.IOUtils;
import com.kartoflane.superluminal2.utils.UIUtils;
import com.kartoflane.superluminal2.utils.Utils;

import net.vhati.modmanager.core.ComparableVersion;
import net.vhati.modmanager.core.FTLUtilities;


public class Superluminal
{
	public static final Logger log = LogManager.getLogger( Superluminal.class );

	public static final String APP_NAME = "Superluminal";
	public static final ComparableVersion APP_VERSION = new ComparableVersion( "2.2.1" );
	public static final String APP_UPDATE_FETCH_URL = "https://raw.github.com/kartoFlane/superluminal2/master/skels/common/auto_update.xml";
	public static final String APP_FORUM_URL = "http://www.subsetgames.com/forum/viewtopic.php?f=12&t=24901&p=78738";
	public static final String APP_AUTHOR = "kartoFlane";

	public static final String HOTKEYS_FILE = "hotkeys.xml";
	public static final String CONFIG_FILE = "editor.cfg";

	/** Length of a single row of text */
	public static final int WRAP_WIDTH = 55;
	/**
	 * How many characters beyond the limit set by WRAP_WIDTH can be
	 * accepted before splitting up the word / moving it to next line
	 */
	public static final int WRAP_TOLERANCE = 9;


	/**
	 * TODO:
	 * =============================================================================================
	 * 
	 * IMMEDIATE:
	 * - work out correction from center-relative to top-left-corner-relative
	 * 
	 * MEDIUM:
	 * - room tool doesn't work sometimes? -- as per SleeperService
	 * - ship overview sometimes messes up invisible object detection (grays out items incorrectly)
	 * 
	 * LOW:
	 * - Rework highlight to be cursor based? --> allows to show mounts/rooms that are hidden
	 * beneath hull/other rooms
	 * - rework the layered painter to allow more freedom in arranging stuff's ordering -> single views
	 * consisting of primitive subviews placed on different layers?
	 * 
	 * Suggestions:
	 * - detachable toolbar?
	 * - detachable sidebar elements?
	 */

	public static void main( String[] args )
	{
		log.debug( String.format( "%s v%s", APP_NAME, APP_VERSION ) );
		log.debug( String.format( "%s %s", System.getProperty( "os.name" ), System.getProperty( "os.version" ) ) );
		log.debug(
			String.format(
				"%s, %s, %s", System.getProperty( "java.vm.name" ),
				System.getProperty( "java.version" ), System.getProperty( "os.arch" )
			)
		);
		log.debug( String.format( "SWT v%s", SWT.getVersion() ) );
		System.out.println();

		try {
			// Display#setAppName() allows to set the name of the application on OSX
			// However, in order to work, it has to be called before any instance of
			// Display is created (via Display#getCurrent() or #getDefault())
			// Also tests whether the correct version of the editor is installed (since SWT code is platform-specific)
			Display.setAppName( APP_NAME );
			Display.setAppVersion( APP_VERSION.toString() );
		}
		catch ( Throwable t ) {
			log.error( "Failed to retrieve display - wrong version of the editor has been downloaded." );

			OS os = OS.identifyOS();
			if ( os.isUnknown() ) {
				log.error(
					String.format(
						"Your system (%s %s) was not recognized, or is not supported :(",
						System.getProperty( "os.name" ), System.getProperty( "sun.arch.data.model" )
					)
				);
			}
			else {
				log.error( "You should download version for " + os.toString() );
			}

			String msg = "";
			msg += "You have downloaded a wrong version of the editor for your system.\n";
			msg += "\n";
			if ( os.isUnknown() ) {
				msg += String.format(
					"Your system (%s %s) was not recognized, or is not supported :(",
					System.getProperty( "os.name" ), System.getProperty( "sun.arch.data.model" )
				);
			}
			else {
				msg += "You should download version for: " + os.toString();
			}

			UIUtils.showSwingDialog( APP_NAME + " - Wrong version", msg );
			System.exit( 1 );
		}

		Display display = Display.getDefault();
		File configFile = new File( CONFIG_FILE );
		SuperluminalConfig appConfig = new SuperluminalConfig( configFile );

		// Read the config file
		InputStreamReader reader = null;
		try {
			if ( configFile.exists() ) {
				log.trace( "Loading properties from config file..." );
				reader = new InputStreamReader( new FileInputStream( configFile ), "UTF-8" );
				appConfig.load( reader );
			}
		}
		catch ( IOException e ) {
			log.error( "Error loading config.", e );
			UIUtils.showErrorDialog( null, null, "Error loading config from " + configFile.getPath() );
		}
		finally {
			try {
				if ( reader != null )
					reader.close();
			}
			catch ( IOException e ) {
			}
		}

		// Read config values
		Manager.sidebarOnRightSide = appConfig.getPropertyAsBoolean( SuperluminalConfig.SIDEBAR_SIDE, false );
		Manager.rememberGeometry = appConfig.getPropertyAsBoolean( SuperluminalConfig.SAVE_GEOMETRY, true );
		Manager.checkUpdates = appConfig.getPropertyAsBoolean( SuperluminalConfig.CHECK_UPDATES, true );
		Manager.startMaximised = appConfig.getPropertyAsBoolean( SuperluminalConfig.START_MAX, false );
		Manager.closeLoader = appConfig.getPropertyAsBoolean( SuperluminalConfig.CLOSE_LOADER, false );
		Manager.allowRoomOverlap = appConfig.getPropertyAsBoolean( SuperluminalConfig.ALLOW_OVERLAP, false );
		Manager.allowDoorOverlap = appConfig.getPropertyAsBoolean( SuperluminalConfig.ALLOW_OVERLAP_DOOR, false );
		Manager.resetDoorLinksOnMove = appConfig.getPropertyAsBoolean( SuperluminalConfig.RESET_LINKS, true );
		Manager.mouseShipRelative = appConfig.getPropertyAsBoolean( SuperluminalConfig.MOUSE_SHIP_RELATIVE, false );

		Manager.shownSlotWarning = appConfig.getPropertyAsBoolean( SuperluminalConfig.SLOT_WARNING, false );
		Manager.shownArtilleryWarning = appConfig.getPropertyAsBoolean( SuperluminalConfig.ARTILLERY_WARNING, false );
		Manager.windowSize = appConfig.getPropertyAsPoint( SuperluminalConfig.GEOMETRY, 0, 0 );

		initHotkeys();
		Manager.loadDefaultHotkeys();

		File hotkeysFile = new File( HOTKEYS_FILE );
		if ( hotkeysFile.exists() )
			loadHotkeys( hotkeysFile );

		// Read FTL resources path
		File datsDir = null;
		Manager.resourcePath = appConfig.getProperty( SuperluminalConfig.FTL_RESOURCE, "" );

		if ( Manager.resourcePath.length() > 0 ) {
			log.info( "Using FTL dats path from config: " + Manager.resourcePath );
			datsDir = new File( Manager.resourcePath );
			if ( FTLUtilities.isDatsDirValid( datsDir ) == false ) {
				log.error( "The config's ftlResourcePath does not exist, or it lacks data.dat." );
				datsDir = null;
			}
		}
		else {
			log.trace( "No FTL dats path previously set." );
		}

		// Create the main window instance
		EditorWindow editorWindow = null;
		try {
			editorWindow = new EditorWindow( display );
		}
		catch ( Exception e ) {
			log.error( "Exception occurred while creating EditorWindow: ", e );

			String msg = "An error has occurred while creating the editor's GUI:\n" +
				e.getClass().getSimpleName() + ": " + e.getMessage() + "\n\n" +
				"Check the log for details.";
			UIUtils.showErrorDialog( null, null, Utils.wrap( msg, 50, 5 ) );
			System.exit( 1 );
		}

		// Find / prompt for the path to set in the config
		if ( datsDir == null ) {
			datsDir = FTLUtilities.findDatsDir();
			if ( datsDir != null ) {
				MessageBox box = new MessageBox( editorWindow.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO );
				box.setText( "Confirm" );
				box.setMessage( "FTL resources were found in:\n" + datsDir.getPath() + "\nIs this correct?" );
				int response = box.open();
				if ( response == SWT.NO )
					datsDir = null;
			}

			if ( datsDir == null ) {
				log.debug( "FTL dats path was not located automatically. Prompting user for location." );
				datsDir = UIUtils.promptForDatsDir( editorWindow.getShell() );
			}

			if ( datsDir != null ) {
				Manager.resourcePath = datsDir.getAbsolutePath();
				appConfig.setProperty( SuperluminalConfig.FTL_RESOURCE, Manager.resourcePath );
				log.info( "FTL dats located at: " + Manager.resourcePath );
			}
		}

		// Show a warning if no dats were selected, or load them if they were
		if ( datsDir == null ) {
			UIUtils.showWarningDialog(
				editorWindow.getShell(), null, "FTL resources were not found.\n" +
					"The editor will not be able to load any data from the game,\n" +
					"and may crash unexpectedly."
			);
			log.debug( "No FTL dats path found - creating empty Database." );
			new Database();
		}
		else {
			try {
				log.trace( "Creating database..." );

				final Database db = new Database( datsDir );
				final List<String> argsList = Arrays.asList( args );

				log.trace( "Loading database..." );

				UIUtils.showLoadDialog(
					editorWindow.getShell(), null, null, new Runnable() {
						public void run()
						{
							db.getCore().load();

							for ( String arg : argsList ) {
								File f = new File( arg );

								if ( !f.exists() ) {
									log.warn( String.format( "'%s' was not loaded because the specified file could not be found.", arg ) );
									continue;
								}
								if ( !arg.endsWith( ".ftl" ) && !arg.endsWith( ".zip" ) ) {
									log.warn(
										String.format(
											"'%s' was not loaded because the specified file is not a .zip or .ftl.",
											f.getName()
										)
									);
									continue;
								}

								try {
									AbstractDatabaseEntry de = new ModDatabaseEntry( f );
									if ( !Utils.contains( db.getEntries(), de ) )
										db.addEntry( de );
								}
								catch ( Exception e ) {
									log.warn( String.format( "Could not create a database entry for file '%s': ", arg ), e );
								}
							}

							db.cacheAnimations();
						}
					}
				);
			}
			catch ( Exception e ) {
				log.error( "An error occurred while loading dat archives:", e );

				String msg = "An error has occurred while loading the game's resources.\n\n" +
					"Please check editor-log.txt in the editor's directory, and post\n" +
					"it in the editor's thread at the FTL forums.";
				UIUtils.showErrorDialog( editorWindow.getShell(), null, msg );
				System.exit( 1 );
			}
		}

		KeyboardInputDispatcher kid = new KeyboardInputDispatcher();
		display.addFilter( SWT.KeyDown, kid );
		display.addFilter( SWT.KeyUp, kid );

		// Open the main window's shell, making it visible
		editorWindow.open();

		if ( Manager.checkUpdates ) {
			checkForUpdates( false ); // Automatic update check
		}

		log.info( "Running..." );

		try {
			while ( !editorWindow.getShell().isDisposed() ) {
				if ( !display.readAndDispatch() )
					display.sleep();
			}
		}
		catch ( Throwable t ) {
			String msg = APP_NAME + " has encountered a problem and needs to close.\n\n" +
				"Please check editor-log.txt in the editor's directory, and post " +
				"it in the editor's thread at the FTL forums.";

			ShipContainer ship = Manager.getCurrentShip();
			if ( ship != null ) {
				String name = "crash_" + System.currentTimeMillis() + ".ftl";
				msg += "\n\nThe ship you had been working on has been saved in the editor's directory as '" +
					name + "'.";
				ship.save( new File( name ) );
			}

			log.error( "An unexpected error has occurred and the editor was forced to terminate.", t );
			UIUtils.showErrorDialog( editorWindow.getShell(), null, msg );
		}

		log.info( "Exiting..." );

		saveHotkeys( hotkeysFile );

		// Save config
		try {
			appConfig.setCurrent();
			appConfig.writeConfig();
		}
		catch ( IOException e ) {
			String errorMsg = String.format( "Error writing config to \"%s\".", configFile.getPath() );
			log.error( errorMsg, e );
			UIUtils.showErrorDialog( editorWindow.getShell(), null, errorMsg );
		}

		editorWindow.dispose();
		display.dispose();

		log.info( "Bye" );
		// Prevents the application from lingering on MacOSX when the window is closed,
		// makes no difference on Windows/Linux
		System.exit( 0 );
	}

	/**
	 * 
	 * @param manual
	 *            if true, an information dialog will pop up even if the program is up to date
	 */
	public static void checkForUpdates( boolean manual )
	{
		new UpdateCheckWorker( manual ).execute();
	}

	/** Create a dummy Hotkey object for each hotkey, and store them in the hotkey map. */
	private static void initHotkeys()
	{
		for ( Hotkeys keyId : Hotkeys.values() )
			Manager.putHotkey( keyId, new Hotkey() );
	}

	/**
	 * Loads hotkey configuration from the given file.
	 * 
	 * @param f
	 *            File from which hotkey config will be read.
	 */
	private static void loadHotkeys( File f )
	{
		try {
			Document keyDoc = IOUtils.readFileXML( f );

			Element root = keyDoc.getRootElement();
			for ( Element bind : root.getChildren( "bind" ) ) {
				String actionName = bind.getValue();
				if ( actionName == null ) {
					log.warn( HOTKEYS_FILE + " contained a bind without an assigned action." );
					continue;
				}

				Hotkeys action = null;
				String loading = null;
				String attr = null;
				try {
					action = Hotkeys.valueOf( actionName );
				}
				catch ( IllegalArgumentException e ) {
					log.warn( "Action '" + actionName + "' was not recognised, and was not loaded." );
					continue;
				}
				try {
					loading = "shift";
					attr = bind.getAttributeValue( loading );
					boolean shift = attr != null && Boolean.valueOf( attr );

					loading = "ctrl";
					attr = bind.getAttributeValue( loading );
					boolean ctrl = attr != null && Boolean.valueOf( attr );

					loading = "alt";
					attr = bind.getAttributeValue( loading );
					boolean alt = attr != null && Boolean.valueOf( attr );

					loading = "cmd";
					attr = bind.getAttributeValue( loading );
					boolean cmd = attr != null && Boolean.valueOf( attr );

					loading = "char";
					attr = bind.getAttributeValue( loading );
					if ( attr == null )
						throw new NullPointerException();
					if ( attr.length() > 1 )
						throw new IllegalArgumentException( action + " keybind has invalid 'char' attribute: " + attr );
					boolean enabled = attr.length() == 1;
					int ch = '\0';
					if ( enabled )
						ch = attr.charAt( 0 );

					Hotkey h = Manager.getHotkey( action );
					h.setEnabled( enabled );
					h.setShift( shift );
					h.setCtrl( ctrl );
					h.setAlt( alt );
					h.setCommand( cmd );
					h.setKey( ch );
				}
				catch ( IllegalArgumentException e ) {
					log.warn(
						String.format(
							"Keybind for action %s had invalid '%s' attribute and was not loaded.",
							action.name(), loading
						)
					);
				}
				catch ( NullPointerException e ) {
					log.warn(
						String.format(
							"Keybind for action %s was missing '%s' attribute and was not loaded.",
							action.name(), loading
						)
					);
				}
			}
		}
		catch ( FileNotFoundException ex ) {
			log.error( "Keybind file could not be found: " + f.getAbsolutePath() );
		}
		catch ( IOException ex ) {
			log.error( "An error has occurred while loading keybind file: ", ex );
		}
		catch ( JDOMParseException ex ) {
			log.error( "JDOM exception occurred while loading file " + f.getAbsolutePath(), ex );
		}
	}

	/**
	 * Saves the current hotkey configuration in the given file.
	 * 
	 * @param f
	 *            File in which hotkey config will be saved.
	 */
	private static void saveHotkeys( File f )
	{
		Document keyDoc = new Document();

		Element wrapper = new Element( "wrapper" );
		Element root = new Element( "keybinds" );

		for ( Hotkeys action : Hotkeys.values() ) {
			Hotkey h = Manager.getHotkey( action );
			Element bind = new Element( "bind" );
			bind.setText( action.name() );
			bind.setAttribute( "shift", "" + h.getShift() );
			bind.setAttribute( "ctrl", "" + h.getCtrl() );
			bind.setAttribute( "alt", "" + h.getAlt() );
			bind.setAttribute( "cmd", "" + h.getCommand() );
			bind.setAttribute( "char", "" + ( h.isEnabled() ? h.getKeyString() : "" ) );

			root.addContent( bind );
		}

		wrapper.addContent( root );
		keyDoc.setRootElement( wrapper );

		try {
			IOUtils.writeFileXML( keyDoc, f );
		}
		catch ( IOException e ) {
			log.error( "An error occurred while saving hotkeys file: ", e );
		}
	}
}
