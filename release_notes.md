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

