package com.abyssaldescent.command;

import com.abyssaldescent.dungeon.Chest;
import com.abyssaldescent.dungeon.Direction;
import com.abyssaldescent.dungeon.Door;
import com.abyssaldescent.dungeon.DungeonManager;
import com.abyssaldescent.dungeon.Room;
import com.abyssaldescent.entity.player.PlayerContext;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.RoomChangedEvent;
import com.badlogic.gdx.Gdx;

public final class InteractCommand implements Command {

    public static final InteractCommand INSTANCE = new InteractCommand();

    private static final float DOOR_SIZE = 277f;
    private static final float DOOR_HALF_SIZE = DOOR_SIZE * 0.5f;
    private static final float DOOR_SIDE_OFFSET = 160f;

    private InteractCommand() {}

    @Override
    public void execute(PlayerContext ctx) {
        DungeonManager mgr = DungeonManager.getInstance();
        Room current = mgr.getCurrentRoom();
        if (current == null) return;

        float px = ctx.getPosition().x;
        float py = ctx.getPosition().y;
        float rw = Gdx.graphics.getWidth();
        float rh = Gdx.graphics.getHeight();

        // Try to open chest first if near center
        if (isNearChest(px, py, rw, rh)) {
            Chest chest = current.getChest();
            if (chest != null && !chest.isOpened()) {
                chest.open();
                if (chest.hasKey()) {
                    mgr.collectKey();
                }
                return;
            }
        }

        // Try to go through doors
        for (Door door : current.getDoors()) {
            if (nearDoor(px, py, rw, rh, door.getDirection())) {
                Room target = mgr.getGraph().getRoom(door.getToRoomId());
                if (target == null) continue;

                // Block entrance to FINAL room without all keys
                if (target.getType() == com.abyssaldescent.dungeon.RoomType.FINAL && !mgr.allKeysCollected()) {
                    return;
                }

                mgr.transitionTo(door.getToRoomId());
                target.setCleared(target.isCleared()); // mark visited in next tick

                // Reposition player on opposite side of new room
                repositionPlayer(ctx, door.getDirection(), rw, rh);

                EventBus.getInstance().post(
                    new RoomChangedEvent(
                        target.getTier().name(),
                        target.getTier().floorNumber,
                        target.getId()));
                return;
            }
        }
    }

    private static boolean isNearChest(float px, float py, float rw, float rh) {
        float cx = rw / 2f;
        float cy = rh / 2f;
        float dist = (float) Math.sqrt((px - cx) * (px - cx) + (py - cy) * (py - cy));
        return dist < 300f;  // Increased range
    }

    private static boolean nearDoor(float px, float py, float rw, float rh, Direction dir) {
        float cx = rw * 0.5f;
        float cy = rh * 0.5f;

        switch (dir) {
            case NORTH:
                return py >= rh - DOOR_HALF_SIZE - 20f && py <= rh && px >= cx - DOOR_HALF_SIZE && px <= cx + DOOR_HALF_SIZE;
            case SOUTH:
                return py <= DOOR_HALF_SIZE + 20f && py >= 0 && px >= cx - DOOR_HALF_SIZE && px <= cx + DOOR_HALF_SIZE;
            case EAST:
                float eastX = rw - DOOR_HALF_SIZE - DOOR_SIDE_OFFSET;
                return px >= eastX - DOOR_HALF_SIZE && px <= eastX + DOOR_HALF_SIZE && py >= cy - DOOR_HALF_SIZE && py <= cy + DOOR_HALF_SIZE;
            case WEST:
                float westX = DOOR_HALF_SIZE + DOOR_SIDE_OFFSET - 100f;
                return px >= westX - DOOR_HALF_SIZE && px <= westX + DOOR_HALF_SIZE && py >= cy - DOOR_HALF_SIZE && py <= cy + DOOR_HALF_SIZE;
            default:
                return false;
        }
    }

    private static void repositionPlayer(PlayerContext ctx, Direction enteredVia, float rw, float rh) {
        float cx = rw * 0.5f;
        float cy = rh * 0.5f;

        switch (enteredVia) {
            case NORTH:
                ctx.setPosition(cx, DOOR_HALF_SIZE + 20f + 75f);
                break;
            case SOUTH:
                ctx.setPosition(cx, rh - DOOR_HALF_SIZE - 20f - 75f);
                break;
            case EAST:
                float eastX = rw - DOOR_HALF_SIZE - DOOR_SIDE_OFFSET;
                ctx.setPosition(eastX - DOOR_HALF_SIZE - 50f, cy);
                break;
            case WEST:
                float westX = DOOR_HALF_SIZE + DOOR_SIDE_OFFSET - 100f;
                ctx.setPosition(westX + DOOR_HALF_SIZE + 50f, cy);
                break;
            default:
                break;
        }
    }
}
