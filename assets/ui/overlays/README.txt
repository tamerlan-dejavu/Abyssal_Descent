Overlay panel textures for Settings and Difficulty screens
===========================================================

SETTINGS OVERLAY
-----------------
Appears centred on screen with blurred background.

  settings_panel.png          — 1800×900 px panel background (optional)
                                Fallback: dark purple rectangle with border.

DIFFICULTY OVERLAY
------------------
Appears in the LOWER part of the screen (shifted ~300 px below centre) so the
game logo above it stays fully visible. Background is NOT blurred.

  difficulty_bg.png           — 1800×900 px panel background (preferred name)
  difficulty_panel.png        — same, alternative name (fallback if _bg not found)

DIFFICULTY BUTTONS  (500×150 px PNG, placed horizontally inside the panel)
  Preferred naming (_idle / _glow):
    easy_idle.png   easy_glow.png     — Easy   button idle / hover-glow
    normal_idle.png normal_glow.png   — Normal button idle / hover-glow
    hard_idle.png   hard_glow.png     — Hard   button idle / hover-glow

  Legacy naming (_off / _on) is also accepted as a fallback:
    easy_off.png    easy_on.png
    normal_off.png  normal_on.png
    hard_off.png    hard_on.png

  The engine tries _idle/_glow first; if absent it tries _off/_on.
  If neither exists the button renders as a styled shape (no texture needed).

All files are optional — the engine gracefully draws a fallback panel and
shape-rendered buttons when any texture is missing.

LAYOUT REFERENCE  (panel coordinate origin = bottom-left of panel)
  Difficulty buttons  : centred horizontally, ~42 % panel height from bottom
  Back button         : bottom-left corner (30 px offset)
  Settings sliders    : top ~72 % of panel, descending every 80 px
