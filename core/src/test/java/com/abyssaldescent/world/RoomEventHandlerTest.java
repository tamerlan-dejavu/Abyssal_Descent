package com.abyssaldescent.world;

import com.abyssaldescent.entity.enemy.Enemy;
import com.abyssaldescent.entity.enemy.EnemyContext;
import com.abyssaldescent.entity.enemy.EnemyType;
import com.abyssaldescent.entity.enemy.ai.strategy.SwarmStrategy;
import com.abyssaldescent.event.DoorOpenedEvent;
import com.abyssaldescent.event.EnemyDeathEvent;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.RoomClearedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoomEventHandlerTest {
    private RoomManager roomManager;
    private EventBus eventBus;
    private Room room;
    private Enemy enemy;
    private Door door;
    private RoomClearedEvent[] clearedEvent;

    @BeforeEach
    void setUp() {
        roomManager = new RoomManager();
        eventBus = EventBus.getInstance();
        eventBus.clear();
        new RoomEventHandler(roomManager, eventBus);

        room = new Room("room1");
        EnemyContext context = new EnemyContext(EnemyType.SHADOW_GOBLIN, "enemy1", 0, 0);
        enemy = new Enemy(context, new SwarmStrategy());
        door = new Door("door1", 1);

        room.addEnemy(enemy);
        room.addDoor(door);
        roomManager.addRoom(room);
        roomManager.setCurrentRoom("room1");

        clearedEvent = new RoomClearedEvent[1];
        eventBus.subscribe(RoomClearedEvent.class, e -> clearedEvent[0] = e);
    }

    @Test
    void enemy_death_removes_enemy_from_room() {
        EnemyDeathEvent event = new EnemyDeathEvent("enemy1", "goblin", 0, 0);
        eventBus.post(event);
        assertTrue(room.getEnemies().isEmpty());
    }

    @Test
    void room_cleared_event_fires_when_all_enemies_removed() {
        EnemyDeathEvent event = new EnemyDeathEvent("enemy1", "goblin", 0, 0);
        eventBus.post(event);
        assertNotNull(clearedEvent[0]);
        assertEquals("room1", clearedEvent[0].getRoomId());
    }

    @Test
    void enemy_death_does_nothing_without_current_room() {
        roomManager = new RoomManager();
        handler = new RoomEventHandler(roomManager, eventBus);
        EnemyDeathEvent event = new EnemyDeathEvent("enemy1", "goblin", 0, 0);
        eventBus.post(event);
        assertNull(clearedEvent[0]);
    }

    @Test
    void door_opened_event_with_cleared_room_transitions_room() {
        room.removeEnemy(enemy);
        Room room2 = new Room("room1");
        roomManager.addRoom(room2);

        DoorOpenedEvent event = new DoorOpenedEvent("room1", 1);
        eventBus.post(event);
        assertEquals(room2, roomManager.getCurrentRoom());
    }

    @Test
    void door_opened_event_without_cleared_room_does_not_transition() {
        Room room2 = new Room("room2");
        roomManager.addRoom(room2);

        DoorOpenedEvent event = new DoorOpenedEvent("room2", 1);
        eventBus.post(event);
        assertEquals(room, roomManager.getCurrentRoom());
    }
}
