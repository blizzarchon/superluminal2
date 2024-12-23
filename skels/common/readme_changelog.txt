Changelog

2.4:
(New Features)
- Player ship systems now have the option to set a max level, similar to enemy ships.
- Added compatibility for newer Macs with M series of chips aka ARM architecture.
- Simplified hyperspace.xml output during ship saving.
- Added support for Hyperspace systemLimit to set a max number of systems.
- Systems' default levels now correspond to their blueprint definition when possible.
  * in case of missing blueprint, levels default to 1-3 like vanilla auxiliary systems.
- Boss ships now show up in ship load selection and save to bosses.xml.append.
- Uncapped number of placeable weapon mounts, previously set to 8.
- Corrected enemy ships' Crew tab to selection of 1 race.
(Quality of Life)
- Added support for races added from Mod Management mods via findLikes.
- Door entries in the .txt file are now sorted by x, y and orientation.
- Ships with unrecognized hidden augs load with warning instead of not loading at all.
(Bug fixes)
- Fixed bug where weapon image sometimes showed projectile instead of weapon.
- Fixed bug where immediately saving ship on load didn't save to the correct slot.
- Fixed bug where a system's start level could be lowered by another system's max level.
- Fixed bug where reducing Armaments via number input did not properly remove them.
- Fixed bug where path for generated floor would not update if it was already visible.
- Fixed crash when trying to view generated floor image location in file system.

2.3:
(New Features)
- "Prefix list.txt" is no longer necessary and has thus been removed.
- Custom crew information is now loaded in from the .dat file. As such, "Race list.txt" is no longer needed and has been removed.
- Blueprint lists can now be chosen from Weapon, Drone and Crew selection dialogs. Sorted alphabetically.
- Exported ship mods now use slipstream tag logic that prevents ship duplication.
- Layout slot selected now determines the blueprint name suffix, correcting any possible discrepancies.
  * blueprint name chosen: MY_CUSTOM_SHIP_2, ship slot selected: C -> upon save, blueprint name is MY_CUSTOM_SHIP_3
- Added option for using ID system instead of direct text for saving of ship text data. Off by default.
- Hyperspace's Temporal system can now be assigned to a room under the Special menu
(Quality of Life)
- Picking crew now opens same kind of selection dialog as when picking weapons, drones and augments.
- Augments and hidden augments have separate sections. This also solves a bug where you could have more than 3 regular augments.
- Number of hidden augments and crew capacity update immediately when amount is changed.
- Layout slot buttons are bigger and easier to select.
- Opening 'Database' for ship images retrieves only the relevant images, though a button can be pressed to see all folders and images.
(Bug fixes)
- Fixed bug where hyperspace data of ships loaded via Mod Management was not being loaded.
- Fixed bug where loaded vanilla ships had zero crew.
- Fixed bug where new ship started with 99 crew capacity instead of 8.
- Fixed bug where new ship could not be saved with default values for name, class or description.

"Superluminal for Hyperspace":
- Added support for some FTL: Hyperspace features:
  * Custom ships can be selected from Load Ship menu. Selectable ships start with a prefix from external resource "Prefix list.txt".
  * Custom crew can be selected from crew menu. Custom crew names are loaded from external resource "Race list.txt".
  * Ship blueprint name is now a text field where the name can be freely edited, instead of choosing an existing ship blueprint.
  * Crew capacity is no longer limited to 8 and can be set higher or lower.
  * Augment section has been expanded to more entries. Augments can now be designated as hidden augments.
  * Layout slot choice of A, B or C determines which layout the ship represents.
- ID system for saving ship textual data turned off in favor of direct text.

2.2.1:
- Fixed a crash when saving a ship that was created with old FTL file format.
- Fixed a crash when changing colors in Generate FLoor Image window using RGB or hex values.
- Fixed values not being consistently capped to min/max in Generate Floor Image window.
- Fixed a bug which could cause ship class and ship name to not be loaded.
- Fixed a bug which caused XML files to be broken when using the "Include Mod Files From" feature when saving.

2.2:
- Added support for FTL 1.6.1+
- Self-patching functionality should now work on all platforms (still broken in versions 2.1.2b and lower; you'll need to update manually)
- Added File > Change .dat Files, allowing you to navigate to other .dat files, and tell the editor to use them (useful if you ever want to use pre-1.6 archives for whatever reason)
- Pirate ships from dlcPirateBlueprints.xml file can now be loaded in the editor.
- The editor now also scans .rawclobber files in loaded mods.
- Automatic update check performed at startup no longer blocks GUI.
- Pinned objects are now highlighted with a yellow border.
- Updated GUI library from 4.5.1 to 4.6.1.
- Updated links to the new FTL forums address.

2.1.2b:
- Updated GUI library from 4.4 to 4.5.1. Hopefully this fixes the issues with Windows 10.

2.1.2a:
- Fixed system stations blocking placement of doors.

2.1.2:
- Changed default color of floor images generated by the editor to standard FTL gray - 0x64696F.
- Added customization options to floor generation - can set border width, floor margin, corner size, and floor and border colors.
- You can now save a mod file along with the ship, effectively adding the ship to the mod. The mod has to be loaded via Mod Management (Ctrl+M) first, though.
- Added experimental self-patching functionality.

2.1.1a:
- Fixed a typo in the error message displayed when trying to animate gibs when the ship has none (animte -> animate)
- Fixed a bug with systems sharing interior images.

2.1.1:
- Editor now recognizes .rawappend files when loading mods via Mod Management
- Added an option to enemy ships to select weapon/drone loadout like in player ships
- When loading a ship that is missing a glow in rooms.xml, the editor now uses the default glow for that system instead of failing to load the ship

2.1.0d:
- Fixed glow images being saved with incorrect name in rooms.xml

2.1.0c:
- Fixed a crash caused by systems whose image had no corresponding entry in rooms.xml (ships exhibiting this problem will fail to load)
- ShipLoader dialog now provides an explanation why a ship could not be loaded.

2.1.0b:
- Fixed a crash caused by the editor sometimes attempting to access already deleted elements
- Door orientation toggle button ("Horizontal") now is disabled when changing the door's orientation would cause it to collide with another door.
- Fixed the editor failing to load ships with custom interior image for the cloaking system, that had no matching glow image
- Fixed glow selection dialog sometimes listing repeated entries
- Glow selection dialog now lists its entries in alphanumerical order
- Fixed glows being deletable
- Fixed a crash related to drone selection
- Fixed glows sometimes not being properly disposed when closing / loading another ship

2.1.0a:
- Fixed a bug that would prevent the editor from loading some of the default settings when the program was started for the first time.
- Fixed a crash when assigning activation glow image to cloaking system
- Fixed several bugs related to system/glow assignment
- Improved database browse search -- search terms can now be partial
- Added crash-save feature -- should the editor crash, your ship will be saved as an .ftl file in the editor's directory.

2.1.0:
- Added zoom feature (in the form of a separate window, found under View > Open Zoom Window)
- Added cursor position tracker at the top-right of the main window + config setting to show the pointer location relative to the ship's origin
- Added automated floor image generation (Edit > Generate Floor Image...)
- Added ability to change the name that interior images get exported as (important for glows)
- Added ability to select images directly from FTL's archives -- all 'Browse' buttons now open a dropdown menu instead, allowing you to choose what you want to browse ('System' is default file selection, 'Database' is the FTL archives)
- Added a bunch of undoable operations:
  * image undo (can undo hull/floor/cloak/shield/thumbnail modification)
  * interior image modification
  * door orientation modification
  * system availability toggle
  * active system selection undo
  * mount rotation/mirror/direction modification
- Changed station images to rectangles taking half of a grid cell, made them fainter
- Added manning glows' placement modification (blue station appearing where the old purple ones used to be, can be clicked and moved around to adjust the glow images' locations)
- Added a modifiable hotkey setting that is shared for all search dialogs
- Weapon mounts now display artillery image only when the artillery systems is assigned
- Gib animation now won't start if the ship has no gibs
- Fixed some cursor visibility issues with Room Tool
- Fixed some systems for player ships allowing to set their level to 9 or 10.
- Fixed a bug with hotkeys, which caused hotkeys that used the Ctrl modifier to also require Alt modifier to be triggered.

2.0.6b beta:
- Fixed a crash related to the Overview Window & gib animation
- Fixed 'Cancel' button in search dialogs not working correctly
- Fixed gib linking via Ship Overview not really working

2.0.6a beta:
- Fixed a bug that prevented gibs from being selectable via Ship Overview
- Added a potential fix for crashes related to door & gib linking

2.0.6 beta:
- Improved stats display in weapon & drone selection dialogs
- Added 'Follow Hull' button to Mount Tool -- when checked, newly created weapon mounts will follow hull
- Added 'Follow Hull' button to gibs
- Newly created gibs now correctly follow hull by default
- Several minor UI tweaks
- Mac Command key support should now work correctly (tested)
- .ftl files can now be dropped onto the main editor frame to load them
- The editor can now open .ftl and .zip files by dragging them onto the .exe, or by using the "Open With..." option in the mod files' context menu
- Added "Unsaved Changes" warning when closing the application
- Added artillery modification & display (moved to Armaments tab)
- Fixed undo of offset that was modified by shift-dragging
- Added undo of reordering objects via Ship Overview
- Calculate Optimal Offset is now undoable
- Doors/Rooms and Mounts/Gibs can now be linked via ShipOverview
- When undoing deletion of elements, the editor now attempts to reinsert them at their old index

2.0.5 beta (which was actually 2.0.4...):
- Fixed shift-dragging the ship origin to set the offset
- Fixed a bug with messed up numbering when reordering game objects via ShipOverview, and then adding new objects of the same type
- Added support for Mac's Command key in hotkeys (untested)
- Fixed application name on Mac (now reads 'Superluminal' instead of 'SWT')
- Fixed a bug that would cause the editor to crash during mount-gib or door linking
- Fixed value of angular velocity constant in the editor's database (was 10 times too big), which was causing gibs to spin wildly
- Added ship death animation simulation -- animates both gibs and weapon mounts attached to them
- Save command now always saves the ship, without checking whether or not it's been modified
- Made 3 more actions hotkeyable: Save Ship As, Open .shp, Animate Gibs
- Made some minor improvements to the Settings window
- Added 'allowDoorOverlap' settings option
- Added option to make weapon mounts and images follow hull (select object > check "Follow Hull")
- Added mono-directional dragging -- hold down Shift while dragging an object to have it move in only one direction (horizontal or vertical)

2.0.3a beta:
- Fixed a bug that would cause the editor to crash when you tried to open the editor without pointing it to the game's archives
- Fixed "Save As" thinking it was "Save"
- Fixed Mac distribution of the editor not having proper permissions set -- you should now be able to launch the editor without having to go through the chmod command

2.0.3 beta:
- Menu buttons' hotkey text is now updated when you modify hotkeys
- Hotkeys can now be unbound
- Added .shp file loading
- Added gibs' angular velocity modification
- Added raw value modification to gibs
- Fixed a somewhat rare bug with system visibility
- Fixed a bug with station visibility that would cause the station to not be hidden when its system was assigned to a room that cannot contain it
- Fixed a bug with automated door linking persisting after saving was completed, which could cause bugged links when the user moved the doors/rooms around and then saved again
- Fixed a bug with Mod Management that would allow the same mod to be loaded multiple times
- Added mount-gib linking
- Reworked hotkey system
- Added search functionality to weapon, drone & augment selection dialogs (Ctrl+F hotkey)
- Added hotkeys to Mod Management: Confirm (Enter), Load (Ctrl+L), Remove (Delete)
- Added undo/redo. Currently undoable operations:
  * Creation of new rooms, doors, mounts and gibs
  * Deletion of rooms, doors, mounts and gibs
  * Move (by mouse)
  * Room resize
  * System (un)assignment
  * Door linking
- Added "Unsaved Changes" warnings/prompts -- however, this is tied to the undo/redo system, so only making undoable changes will cause the warnings to pop up

2.0.1 beta:
- Fixed a minor code screw-up that prevented shield, floor and thumbnail images from being saved.
- Fixed some stations being visible when they should not be
- Fixed the corrupt image "bug" -- the editor now detects when you've installed mods with SMM while the editor was running, and now automatically reloads the database
- Added keybind modification to the Settings dialog
- Rooms, weapon mounts and gibs can now be reordered via the Ship Overview window -- simply drag them.

2.0.0 beta9:
- Fixed a bug that would prevent weapon/drone slots from saving correctly for enemy ships
- Ship save destination is now on a per-ship basis, instead of being application-wide
- File and folder selection dialogs should now remember their own paths. Some of the file selection dialogs are grouped together, eg. all interior images' dialogs use the same path.
- Toggling hangar image for enemy ships now displays the enemy window instead
- Disabled the horizontal offset slider for enemy ships, since it doesn't affect them
- Fixed enemy offset loading & improved enemy optimal offset calculation
- Ship Loader now also remembers previous selection
- Enemy ship images now get saved to 'ships_glow' instead of 'ships_noglow' -- still not sure if only one is enough for enemy ship hulls to show up correctly
- Removed unnecessary method calls, reducing ManipulationTool dragging lag by around 33%
- Fixed a bug with database reloading that would crash the editor in any mods were loaded
- Corrected ship saving to only export interior and glow images when the system using them is actually assigned
- Fixed a bug with the loading dialog that would cause the editor to crash if two loading dialogs were displayed at the same time
- Hiding an object now also deselects it
- Fixed config dialog's contents not wrapping when the window was resized, improved scrolling
- Added artillery loading & saving (no modification yet)
- Added new config option to reset door links when the door is moved
- Reworked Gib Tool to be a part of Images Tool
- Fixed a bug that would cause gib ordering to not be preserved
- Fixed angular velocity not being loaded
- Fixed gib saving
- Added gib modification (WOOO!)
- Added gib image saving
- Fixed shield & interior images (?) being exported for enemy ships
- Slightly reworked & improved the Overview Window, added gibs, and a visibility toggle button

2.0.0 beta8:
- The update dialog now displays a brief list of changes that have been made since the version you're using
- Fixed a bug with ship loading that would not link mounts to gibs correctly, causing problems when you tried to save the ship
- Weapon/drone/augment selection dialogs now will also scroll to show the last selected item
- Added the ability to change enemy ships' boarding AI, now only gets exported for enemy ships
- Fixed a bug that would not allow to confirm "No Drone List" in drone selection dialog
- Fixed systems not disappearing when hiding rooms
- Done some preliminary work on gibs - can be viewed and moved around with Gib Tool (you have to hide all the other elements first - keys 1 through 8 by default). Gibs are not exported yet.

2.0.0 beta7:
- Some of the previously unloadable ships can now be loaded (missing 'max' attribute on <crewCount> tag)
- Fixed a bug that would cause systems to be exported evem when the room they've beem assigned to has been deleted
- Stations are no longer saved for enemy ships, since they apparently don't affect them anymore
- Station Tool is now disabled for enemy ships
- Some of enemy ships' systems' level caps have been raised to 10
- Reworked the way the editor handles systems to allow multiple artilleries
- Improvied resize detection on Linux environments -- grid should fit the window most of the time now
- Fixed issues with keybinds on Linux -- temporary workaround

2.0.0 beta6a:
- Added verification to image browsers to make sure that the selected file actually exists
- Improved error handling in ship-saving code
- Fixed a minor bug with crew UI that would cause a crash
- Added tooltips to rooms' sidebar
- Changed tooltips to stay until dismissed by the user
- Fixed drone parts and missile amount changes not being applied to the ship
- Fixed 'File > Reload Database' function

2.0.0 beta5:
- Added crew modification
- Weapon/drone/augment dialogs now remember previous selection
- Some more tooltips
- Several minor tweaks, behind-the-scenes changes
- Added a popup for when the user downloaded a wrong version of the editor for their system
- Decoupled room and system drawing logic, systems are drawn on separate layer above rooms

2.0.0 beta4a:
- Fixed a minor oversight that would cause the editor to crash when editing enemy ships

2.0.0 beta4:
- Added Calculate Optimal Offset option - calculates both thick and fine offsets
- Added fine offset modification (HORIZONTAL and VERTICAL properties)
- Added ability to show the hangar image as background, which accurately shows where the ship will be positioned in-game.
- Added more tooltips
- Added mouse shortcuts to Weapon Mounting Tool
- Added enemy shield resizing

2.0.0 beta3:
- Fixed weapon mounts with direction NONE being incorrectly saved ('none' instead of 'no')
- Fixed a bug that caused the grid not to be resized properly when the editor was started
- Fixed a minor screw-up that caused the additional weapon/drone slots to be unusable
- Fixed Images Tool's hotkey not working
- Added a crappy image for Images Tool
- Some spinners (ie. numerical fields with up/down arrows) were showing up incorrectly on Linux, attempted to fix that by giving them a fixed width
- Added "Show File" button to Image Viewer, which will show the file in the OS' filesystem, disabled if it's not applicable for the currently viewed image.
- Wired min and max sector spinners to update the ship's min/max sector values (they were having no effect previously)
- Min/max sector tags now get saved before the <systemList> tag
- Editor now also saves the <boardingAI>sabotage</boardingAI> tag
- Fixed DatabaseCore in Mod Management being draggable and thus removeable on some platforms
- Added the option to reload the entire database
- Weapon mounts can now display any weapon as if it were equipped at the mount, without actually changing the loadout
- Added info icons to several elements of the UI, that display a short tip when hovered over, which describes the setting
- Added ship offset modification

2.0.0 beta2:
- Fixed a crash when resizing the sidebar to occupy the entire window
- Fixed a bug when saving a ship after a new weapon mount has been placed
- Fixed a bug causing new rooms to always have ID -1
- Hopefully fixed a bug where right-clicking on a room to assign system causes a crash
- Fixed a crash when clicking interior images' "Clear" button
- Default images are now loaded when a system is assigned for the first time
- Also removed ability to assign interior images to systems in enemy ships
- Images tab moved from Properties to its own separate "tool"
- Weapon/drone slots now allow up to 8 slots, but show a warning the first time the user assigns more than 4 slots

2.0.0 beta1:
- Test release
