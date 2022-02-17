To use, simply extract and run the jar file. 

By default, only vanilla races are included; to enable custom races, either add custom races manually to the Race list (in the format "id,Alias", ids can be found in a mod's blueprints.xml) or rename one of the other Race lists to "Race list.txt" (one is included for Multiverse, and one is included for Multiverse + its addon Eldritch Horrors, although they may need to be manually updated if a new version is out with new crew).

Similarly, some mods add new prefixes for player ships (such as Multiverse using "CREW_SHIP" and "ELITE_SHIP"); the "Prefix List.txt" is the file that lists all of the player ship prefixes.

If you get an exception mentioning "null" when trying to load, it's probably due to the Race list missing the races that that ship uses; update the race list and try again!

Directories for files are currently hardcoded, so don't shuffle things around!