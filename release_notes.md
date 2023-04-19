#### 2023-04-19 mxtune-1.16.5-2.0.0-alpha-2023-04-19.40843381
* Classic JAMS implemented.
  * To Join Group:
    * Player click leader of a group/(yellow-diamond on placard) with an instrument.
  * Two access modes added Pin and Open.
  * Create/Manage from the instrument inventory.
  * TODO: Add helps to Pin and Group GUI's.
* 2nd pass placard textures. Still meh :P

#### 2023-03-25 mxtune-1.16.5-2.0.0-alpha-2023-03-25.75248774
* Auto Select option for MultiInstrument
  On: Changes to the instrument specified by the Sheet Music in the inventory slot.
  Off: Uses the instrument shown on the selector button.
  Your old sheet music may not work. The instrument name must show on the second line of the Sheet Music tooltip.
* -cough- right-clicking a Music Score will create and add yourself and a chicken a group. If another player clicks on you while holding an instrument in their main hand they will be joined to your group. Your group can now play JAMS just like in the 1.12.2 version. Members queue up the parts and the leader kicks it off.  Sorry, no group GUIs yet.

#### 2022-10-02 mxtune-1.16.5-2.0.0-alpha-2022-10-02.26788435
* Parse MML once and cache generated MIDI sequence. Minor fixes.
  * Manage network protocol version. Client and server side versions must match.
  * Music Block as Item: Shows sheet music title for first instrument found in inventory.
  * Music Block: Make instrument rotation smoother.
  * Fix-up some javadocs.
  * Squash a potential NPE when getting the music preset index.
  * Optimization: Parse MML once and cache the sequence.

#### 2022-08-07 mxtune-1.16.5-2.0.0-alpha-2022-08-07.46813887 
* Music Venue Tool updated! Music Venues enabled! 
  * To use this snapshot you must update to Forge 1.16.5 - 36.2.39+ 
  * Removed a SoundEngine mixin in favor of using back ported PR that allows custom stream sources: 
    * Ref: Allow sound instances to customize stream source (#8595) 
  * The music venue tool now allows creating and removing venues. A venue is just a box defined by two corners. The minimum size in blocks is 2x2x2. A venue creates a sound barrier for mxTune music. mxTunes played inside can't be heard outside and vise versa. 
  * Updated tooltips for the "Music Venue Tool" and added a recipe for it: 
    * 1 "wooden rod" and any 1 "mxTune held instrument". 
  * Sorry but group JAMS are not implemented yet, but I will begin work on that next. 

#### 2022-06-23 mxtune-1.16.5-2.0.0-alpha-2022-06-22.78821947 
* Client Audio Refactor:
  * Initial implementation of sound source limiting and prioritization.
  * Track elapsed play time on client too so at some point I will add a progress bar to the HUD and Music Block. 
  * Many bugs and odd behavior will probably appear :D

#### 2022-06-06 mxtune-1.16.5-2.0.0-alpha-2022-06-06.32450078  
* Behind the scenes improvements: 
  * Improve skipping forward in active tunes. Example when you enter a world where a tune is in progress, you will hear that tune sooner. 
    * There probably some bugs hiding here so please do let me know if you find one. 
  * Ensure the Music Block removes its chunk load ticket one minute after a tune stops. 
  * General clean up of sound mxTune classes. 
  * Waiting on the next Forge Recommended Build for 1.16.5 which should include "Allow sound instances to customize stream source #8595" 
    * https://github.com/MinecraftForge/MinecraftForge/pull/8595 
    * This will allow me to remove a mixin, and improve reliability. 

#### 2022-05-23 mxtune-1.16.5-2.0.0-alpha-2022-05-24.31048005 
* More Music Block updates, but not complete: 
  * Owner can lock, manage contents/settings and break. 
  * Add hoover helps and help button. 
  * New model and textures. NOT FINAL! WIP. Has playing texture now. 
  * Block lights up when playing. 
  * Instruments are now displayed in the world for now, but may change. 
  * Music Block can now be rotated and picked up into inventory using a standard mod wrench (ItemTag forge:tools/wrench). Think of other mods that have wrenches: RFTo... Therm... 
    * Main Hand Wrench: Right-Click rotates front to the clicked face. SHIFT-Right-Click picks up into inventory. 
  * Included basic Wrench Test Item. No recipe, so give it to yourself. 
### IMPORTANT 
  With this snapshot, the player who places the Music Block becomes the owner. If updating to this version, any placed Music Blocks placed will have a null owner. They are not locked, but you can't remove them in survival mode. I suggest breaking all the Music Blocks before updating then and placing them back after. That will guarantee your player will be the owner of the Music Block. Read the Music Block Inventory Screen help for more information on how Ownership and Locking work. 

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

