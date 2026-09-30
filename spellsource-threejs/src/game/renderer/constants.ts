/** Board and layout dimensions (world units) */

// Card dimensions
export const CARD_WIDTH = 0.7;
export const CARD_HEIGHT = 1.0;
export const CARD_DEPTH = 0.02;

// Board table
export const BOARD_WIDTH = 12;
export const BOARD_DEPTH = 8;
export const BOARD_Y = 0; // table surface Y

// Battlefield rows (center of each row, Z axis)
export const BATTLEFIELD_Z_BOTTOM = -0.9;
export const BATTLEFIELD_Z_TOP = 0.9;
export const BATTLEFIELD_SLOT_SPACING = 1.1;
export const MAX_BATTLEFIELD_SLOTS = 7;

// Hand area
export const HAND_Z_BOTTOM = -3.6;
export const HAND_Z_TOP = 3.6;
export const HAND_Y_OFFSET = 0.1; // cards float slightly above table
export const HAND_CARD_SPACING = 0.8;
export const MAX_HAND_SIZE = 10;
export const HAND_FAN_ANGLE = 3; // degrees per card from center

// Hero portrait positions
export const HERO_Z_BOTTOM = -2.4;
export const HERO_Z_TOP = 2.4;

// Hero power (to the right of hero)
export const HERO_POWER_X_OFFSET = -1.2;

// Weapon (to the left of hero)
export const WEAPON_X_OFFSET = 1.2;

// Deck pile position (right side of board)
export const DECK_X = -5.0;
export const DECK_Z_BOTTOM = -2.4;
export const DECK_Z_TOP = 2.4;

// Mana display (bottom right)
export const MANA_X = -4.5;
export const MANA_Z_BOTTOM = -3.4;
export const MANA_Z_TOP = 3.4;

// End turn button
export const END_TURN_X = -5.8;
export const END_TURN_Z = 0;

// Camera (orthographic)
// These define the desired world-space extents the camera must show.
// The CameraRig dynamically computes the frustum to fit the board
// regardless of screen aspect ratio.
export const CAMERA_FIT_HALF_WIDTH = 6.5; // half of horizontal extent to always show
export const CAMERA_FIT_HALF_HEIGHT = 4.2; // half of vertical extent to always show
export const CAMERA_POSITION = { x: 0, y: 9, z: -4.5 } as const;
export const CAMERA_LOOK_AT = { x: 0, y: 0, z: -0.2 } as const;

// Colors
export const COLOR_BOARD_SURFACE = 0x2a1a0a;
export const COLOR_BOARD_BORDER = 0x4a2a10;
export const COLOR_BOARD_DIVIDER = 0x3a2a1a;
export const COLOR_CARD_FRONT = 0xd4c5a0;
export const COLOR_CARD_BACK = 0x2244aa;
export const COLOR_HERO_PORTRAIT = 0x556655;
export const COLOR_MANA_CRYSTAL = 0x2266dd;
export const COLOR_MANA_EMPTY = 0x333344;
export const COLOR_END_TURN_ACTIVE = 0x44aa44;
export const COLOR_END_TURN_INACTIVE = 0x666666;
export const COLOR_AMBIENT_LIGHT = 0xffffff;
export const COLOR_DIR_LIGHT = 0xffeedd;
export const COLOR_BACKGROUND = 0x1a0f05;
