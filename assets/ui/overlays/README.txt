Overlay panel textures for Settings and Difficulty screens
===========================================================

When the player clicks Settings or New Game, the main menu background blurs
and a centered panel (1800 x 900 px) overlays it.

PANEL BACKGROUNDS (1800 x 900 px, PNG)
  settings_panel.png      — full background for the settings overlay
  difficulty_panel.png    — full background for the difficulty selection overlay

DIFFICULTY BUTTONS (500 x 150 px, PNG — same size as main-menu buttons)
  easy_off.png   / easy_on.png      — Easy   button idle / hover
  normal_off.png / normal_on.png    — Normal button idle / hover
  hard_off.png   / hard_on.png      — Hard   button idle / hover

All files are optional.  When absent the engine draws a dark fallback panel
with shape-rendered buttons automatically.

Layout reference (panel coordinate origin = bottom-left of panel):
  Difficulty buttons: centred horizontally at ~42 % panel height
  Back button        : bottom-left corner of panel (offset 30 px)
  Settings sliders   : top ~72 % descending, labels left-aligned
