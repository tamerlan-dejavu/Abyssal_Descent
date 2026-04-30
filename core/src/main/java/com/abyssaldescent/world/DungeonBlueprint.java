package com.abyssaldescent.world;

import com.abyssaldescent.entity.enemy.EnemyType;
import java.util.Arrays;
import java.util.List;

public final class DungeonBlueprint {

    public static List<EnemyType> tierEnemies(Tier tier) {
        switch (tier) {
            case SURFACE_CAVERNS:
                return Arrays.asList(EnemyType.SHADOW_GOBLIN, EnemyType.MOSS_CRAWLER, EnemyType.STONE_WATCHER);
            case DROWNED_DEPTHS:
                return Arrays.asList(EnemyType.BONE_ARCHER, EnemyType.DROWNER, EnemyType.RIFT_JELLYFISH);
            case LAVA_SANCTUM:
                return Arrays.asList(EnemyType.RIFT_KNIGHT, EnemyType.LAVA_SNAKE, EnemyType.SLAG_ELEMENTAL);
            default:
                return Arrays.asList(EnemyType.SHADOW_GOBLIN);
        }
    }

    public static EnemyType tierBoss(Tier tier) {
        switch (tier) {
            case SURFACE_CAVERNS: return EnemyType.STONE_WATCHER;
            case DROWNED_DEPTHS:  return EnemyType.DROWNER;
            case LAVA_SANCTUM:    return EnemyType.MALTARION_ECHO;
            default: return EnemyType.STONE_WATCHER;
        }
    }

    public static int enemyCountFor(RoomType type, RoomSize size) {
        if (!type.spawnsEnemies()) return 0;
        if (type == RoomType.BOSS) return 1;
        switch (size) {
            case SMALL:  return 2;
            case MEDIUM: return 3;
            case LARGE:  return 5;
            case HUGE:   return 7;
            default: return 3;
        }
    }

    public static int keyIdFor(Tier tier) {
        return tier.getDepth() * 100;
    }
}
