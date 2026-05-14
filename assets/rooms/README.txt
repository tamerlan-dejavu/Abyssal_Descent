Room background textures
========================
Place PNG files matching the paths below. Any resolution works — they are
stretched to fill the 2880x1800 window. PNG is preferred; JPG also works
(just change the extension in RoomTheme.getBackgroundPath if needed).

  rooms/
    upper_ruins/
      starting.png        — Entrance hall / starting room
      battle_arena.png    — Combat arena
      save_room.png       — Rest / save checkpoint
      final.png           — Boss / final chamber

    flooded_catacombs/
      starting.png
      battle_arena.png
      save_room.png
      final.png

    maltarions_abyss/
      starting.png
      battle_arena.png
      save_room.png
      final.png

Fallback: if a file is missing the game tries the legacy paths under
ui/backgrounds/ and finally generates a solid-colour placeholder.
