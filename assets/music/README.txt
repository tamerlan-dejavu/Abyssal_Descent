Room music tracks
=================
One MP3 per room type per tier. The game switches track automatically whenever
the player enters a new room.

  music/
    upper_ruins/
      starting.mp3        — calm ambient, entrance
      battle_arena.mp3    — combat loop
      save_room.mp3       — soft, safe feeling
      final.mp3           — boss / dramatic

    flooded_catacombs/
      starting.mp3
      battle_arena.mp3
      save_room.mp3
      final.mp3

    maltarions_abyss/
      starting.mp3
      battle_arena.mp3
      save_room.mp3
      final.mp3

Fallback: if a specific track is missing the game scans the assets root for
any .mp3 file and plays that instead (legacy behaviour).
