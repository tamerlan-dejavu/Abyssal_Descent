Overlay assets for Settings and Difficulty screens
====================================================

════════════════════════════════════════════════════════════
  SETTINGS OVERLAY
════════════════════════════════════════════════════════════
Centred on screen, blurred background.

  settings_panel.png        — panel background, 1800×900 px (optional)
                              Fallback: dark purple rectangle with border.

════════════════════════════════════════════════════════════
  DIFFICULTY OVERLAY  (full-screen)
════════════════════════════════════════════════════════════
Covers the entire window.  No blur — game logo is NOT visible during
difficulty selection (it's a separate full-screen state).

BACKGROUND
  difficulty_bg.png         — full-screen background, e.g. 2880×1800 px
                              Also tries: difficulty_panel.png
                              Fallback: dark blue overlay over main menu bg.

DIFFICULTY BUTTONS  — 500 × 150 px each
  Layout: horizontal row, centred, at 42 % screen height.

  easy_idle.png   easy_glow.png       ← Easy   (idle / active-hover)
  normal_idle.png normal_glow.png     ← Normal (idle / active-hover)
  hard_idle.png   hard_glow.png       ← Hard   (idle / active-hover)

  Legacy fallback names also accepted:
    easy_off.png / easy_on.png
    normal_off.png / normal_on.png
    hard_off.png   / hard_on.png

BACK BUTTON  — 500 × 150 px
  Position: bottom-RIGHT corner, 40 px margin from screen edges.

  back_idle.png   back_glow.png       ← Back   (idle / active-hover)
  Fallback: back_off.png / back_on.png

════════════════════════════════════════════════════════════
  RULES
════════════════════════════════════════════════════════════
• _idle / _glow are checked first; _off / _on used as fallback.
• Any missing texture → shape-rendered button (no crash).
• All textures are loaded once on screen show() and disposed on dispose().
