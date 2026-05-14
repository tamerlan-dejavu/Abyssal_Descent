Door overlay textures
=====================
One PNG per cardinal direction. These are drawn on the screen edge to show
where the player can exit the current room.

  doors/
    door_north.png   — top edge indicator
    door_south.png   — bottom edge indicator
    door_east.png    — right edge indicator
    door_west.png    — left edge indicator

Recommended size: 200x24 px (horizontal) / 24x200 px (vertical).
If a file is missing the game draws a coloured rectangle instead:
  White  = Battle Arena exit
  Green  = Save Room exit
  Gold   = Final Chamber exit
