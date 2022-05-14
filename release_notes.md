#### 2022-05-14 mxtune-1.16.5-2.0.0-alpha-2022-05-14.48539161 
* Music Block updates are still WIP, but making progress: 
  * GUI updates: resized and added buttons for redstone signals. 
  * Responds to rear redstone signal for start/stop play. 
  * Outputs redstone signals to left/right sides. 
  * Breaking the block now returns a block item entity containing the settings and inventory. 
  * New textures, but these will probably change yet again when other visual features are enabled. 
* Simplify the mxTune client audio initialization. This should improve reliability of music plays. 
* When a tune ends the server no longer sends a stop. The server tracks seconds elapsed so a tune can be started mid-play for a player who wonders into the area where an mxTune instrument is active. 

#### 2022-03-28 mxtune-1.16.5-2.0.0-alpha-2022-03-28.52233404
* Make the MusicBlock keep the chunk loaded while it's playing.
* All mxTune music now uses the vanilla Records SoundCategory.
* Enable server side config for listener range: i.e. distance where volume is zero.
* Ensure mxTune music playing subtitle works even when master and/or record volume is off.
* Experimental: Active solo playing player will hear their music when changing dimensions. Unreliable at this point. Cancelling a play after changing dimension may fail.
* Fixed issue where a hurt player could not open the instrument inventory.
* Instruments: Allow sneak pass thru to blocks/tile entities. i.e. you can open the Music Block inventory when holding an instrument now.
* Instruments: When in the instrument selection gui you can double-click on an entry to select and close the gui. (This was implemented in the previous snapshot but was not included in the release notes)
* Music Blocks now emit NOTE particles when active. i.e. begins work toward completing the Band Amp.
* Known Issue: When in Standalone mode, and the game paused any active mxTunes may end prematurely when the game is un-paused. The server side is unaware of client pauses and will stop a tune when its duration ends.

#### 2022-03-15 mxtune-1.16.5-2.0.0-alpha-2022-03-15.70879418
* Due to the change below all instruments have been converted to Accordions :D
* Instruments are now a single item with multiple models. Meaning no need to craft 89 instruments in the future. Works like the 1.12.2 version now.
* The "Music Venue Tool" item is a work in progress. All it does at this time is mark bounding boxes. They do nothing at this time. In the future these will be used to define areas where you can play in groups. They will be similar to how stages work in the game Maple Story 2.
  * You can't edit or delete these! (you could with a nbt editor like NBTExplorer - <worldsave> data/capabilities.dat/mxtune:stage_area musicVenues list)
* Many behind the scenes improvements, but sadly a release date is not in sight. Still too much to do and learn about 1.16.5+

#### 2021-12-31 mxtune-1.16.5-0.0.0-SNAPSHOT-2021-12-31
* Properly support 16 part tunes now. Only 14 instruments could actually be used in the previous snapshot.
* Add tooltip help to Music Block and Music Paper
* Fixed some crashes that could occur with large MML files.
* This snapshot is NOT compatible with the previous one. It will crash if you simply drop it in an existing world. Please test on a NEW world only.
  * If you REALLY want to use the same world: Delete the **_music.mv_** file in the  'saves<world-name>/mxtune' | 'world/mxtune' folder. All of your sheet music will be unusable so you should remove them from the world and re-create.

#### 2021-12-25 mxtune-1.16.5-0.0.0-SNAPSHOT-2021-12-25
* Let's do a snapshot release for Christmas and add a festive texture to the music block.

