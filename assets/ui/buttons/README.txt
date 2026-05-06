Button textures for MainMenuScreen
===================================

Recommended size: 500 x 150 px  (PNG with transparency)

  button_normal.png   — idle / default state
  button_hover.png    — hovered state (optional: if absent, a warm-yellow tint is applied)

When these files are present the ShapeRenderer fallback is hidden automatically.
Both textures are stretched to fit the button rect (500 x 150 world pixels).

Hover border (yellow, 3 px) is always rendered on top via ShapeRenderer regardless of
whether texture files are used.
