

package awubot.comms;

import static awubot.Game.*;
import static awubot.util.Util.*;

import awubot.RobotTracker;
import awubot.util.Rng;
import awubot.util.string.FastIndexedIntSet;
import battlecode.common.Direction;
import battlecode.common.MapLocation;


public class EnemyManager {
    public static final int LOCATION = 7;
    public static final int DIRECTION = 3;
    public static final int HP = 5;
    public static final int VIEW_RADIUS = 4;
    public static final int ENC_WIDTH = LOCATION + DIRECTION + HP;
    public static final int NULL = (1 << ENC_WIDTH) - 1;

    public static FastIndexedIntSet knownEnemiesSet;
    public static int numKnownEnemies;
    public static Enemy[] knownEnemies;

    public static void init() {
        knownEnemiesSet = new FastIndexedIntSet();
        knownEnemies = new Enemy[100];
        numKnownEnemies = 0;
    }
 
    public static void preTick() {
        knownEnemiesSet.clear();
        numKnownEnemies = 0;
    }

    public static void registerPacket(int data, MapLocation src) {
        Enemy enemy = decodeEnemy(data, src);
        if (enemy == null) {
            return;
        }
        if (!rc.canSenseLocation(enemy.location) && knownEnemiesSet.add_and_ret(enemy.hashCode())) {
            // indicate("enemy recv " + enemy.location + " dir " + enemy.direction + " hp " + enemy.health);
            knownEnemies[numKnownEnemies++] = enemy;
        }
    }
    
    public static void cullUnsendableEnemies() {
        int i = 0;
        while (i < numKnownEnemies) {
            Enemy enemy = knownEnemies[i];
            int dx = Math.abs(enemy.location.x - myLocX);
            int dy = Math.abs(enemy.location.y - myLocY);
            if (dx > VIEW_RADIUS || dy > VIEW_RADIUS) {
                // out of range
                // knownEnemiesSet.remove(encodeEnemy(enemy)); unnecessary
                knownEnemies[i] = knownEnemies[numKnownEnemies - 1];
                numKnownEnemies--;
            } else {
                i++;
            }
        }
    }

    public static boolean trySqueak() {
        if (isRatKing) {
            return trySqueakKing();
        } else {
            return trySqueakRat();
        }
    }

    public static boolean trySqueakKing() {
        if (RobotTracker.numEnemies == 0) return false;
        int data = 0, squeaked = 0;
        for (int i = 0; i < RobotTracker.numEnemies && squeaked < 2; i++) {
            Enemy enemy = new Enemy(RobotTracker.sortedEnemies[i]);
            int dx = Math.abs(enemy.location.x - myLocX);
            int dy = Math.abs(enemy.location.y - myLocY);
            if (dx > VIEW_RADIUS || dy > VIEW_RADIUS) continue;
            data |= (encodeEnemy(enemy) << (i * ENC_WIDTH));
            squeaked++;
        }
        if (squeaked == 1) data |= (NULL << ENC_WIDTH);
        Squeaker.squeakEnemy(data);
        return true;
    }

    public static boolean trySqueakRat() {
        cullUnsendableEnemies();
        int squeaked = 0;
        int data = 0;
        if (RobotTracker.numEnemies == 1) {
            Enemy enemy = new Enemy(RobotTracker.sortedEnemies[0]);
            data |= encodeEnemy(enemy);
            squeaked++;
        } else if (RobotTracker.numEnemies > 1) {
            int i = Rng.mcg() % (RobotTracker.numEnemies - 1) + 1;
            Enemy enemy = new Enemy(RobotTracker.sortedEnemies[i]);
            data |= encodeEnemy(enemy);
            int j = Rng.mcg() % i;
            enemy = new Enemy(RobotTracker.sortedEnemies[j]);
            data |= (encodeEnemy(enemy) << ENC_WIDTH);
            squeaked += 2;
        }
        if (squeaked == 0) return false;
        if (squeaked == 1) data |= (NULL << ENC_WIDTH);
        Squeaker.squeakEnemy(data);
        return true;
    }

    public static int encodeEnemy(Enemy enemy) {
        int dx = enemy.location.x - myLocX + VIEW_RADIUS;
        int dy = enemy.location.y - myLocY + VIEW_RADIUS;
        while (dx < 0) dx++;
        while (dy < 0) dy++;
        while (dx > 2 * VIEW_RADIUS) dx--;
        while (dy > 2 * VIEW_RADIUS) dy--;
        int encLoc = dx * (2 * VIEW_RADIUS + 1) + dy;
        if (!(encLoc >= 0 && encLoc < (1 << LOCATION))) {
            // println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            // println("ERROR: cannot encode enemy location " + enemy.location + " from " + myLoc + " dx " + dx + " dy " + dy + " encLoc " + encLoc);
            // println("tried encoding " + enemy.location + " from " + myLoc);
        }
        int encDir = enemy.direction.ordinal();
        int encHP = encodeHealth(enemy.health);
        assert(encLoc >= 0 && encLoc < (1 << LOCATION)) : "encLoc overflow " + encLoc;
        assert(encDir >= 0 && encDir < (1 << DIRECTION)) : "encDir overflow " + encDir;
        assert(encHP >= 0 && encHP < (1 << HP)) : "encHP overflow " + encHP;
        return (encLoc << (DIRECTION + HP)) | (encDir << HP) | encHP;
    }

    public static Enemy decodeEnemy(int data, MapLocation src) {
        if (data == NULL) return null;
        int encLoc = data >>> (DIRECTION + HP);
        int encDir = (data >>> HP) & ((1 << DIRECTION) - 1);
        int encHP = data & ((1 << HP) - 1);
        assert(encLoc >= 0 && encLoc < (1 << LOCATION)) : "encLoc width " + encLoc;
        assert(encDir >= 0 && encDir < (1 << DIRECTION)) : "encDir width " + encDir;
        assert(encHP >= 0 && encHP < (1 << HP)) : "encHP width " + encHP;
        MapLocation loc = new MapLocation(
            (encLoc / (2 * VIEW_RADIUS + 1)) - VIEW_RADIUS + src.x,
            (encLoc % (2 * VIEW_RADIUS + 1)) - VIEW_RADIUS + src.y
        );
        return new Enemy(loc, Direction.values()[encDir], decodeHealth(encHP));
    }

    public static int decodeHealth(int i) {
        switch (i) {
            case 0:
                return 24;
            case 1:
                return 26;
            case 2:
                return 28;
            case 3:
                return 30;
            case 4:
                return 32;
            case 5:
                return 34;
            case 6:
                return 36;
            case 7:
                return 38;
            case 8:
                return 40;
            case 9:
                return 42;
            case 10:
                return 44;
            case 11:
                return 46;
            case 12:
                return 48;
            case 13:
                return 50;
            case 14:
                return 52;
            case 15:
                return 54;
            case 16:
                return 56;
            case 17:
                return 58;
            case 18:
                return 60;
            case 19:
                return 62;
            case 20:
                return 64;
            case 21:
                return 66;
            case 22:
                return 68;
            case 23:
                return 70;
            case 24:
                return 72;
            case 25:
                return 74;
            case 26:
                return 76;
            case 27:
                return 78;
            case 28:
                return 80;
            case 29:
                return 88;
            case 30:
                return 90;
            case 31:
                return 100;
            default: 
                assert(false) : "Invalid health encoding " + i;
                return -1;
        }
    }

    public static int encodeHealth(int x) {
        switch (x) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
                return 0;
            case 25:
            case 26:
                return 1;
            case 27:
            case 28:
                return 2;
            case 29:
            case 30:
                return 3;
            case 31:
            case 32:
                return 4;
            case 33:
            case 34:
                return 5;
            case 35:
            case 36:
                return 6;
            case 37:
            case 38:
                return 7;
            case 39:
            case 40:
                return 8;
            case 41:
            case 42:
                return 9;
            case 43:
            case 44:
                return 10;
            case 45:
            case 46:
                return 11;
            case 47:
            case 48:
                return 12;
            case 49:
            case 50:
                return 13;
            case 51:
            case 52:
                return 14;
            case 53:
            case 54:
                return 15;
            case 55:
            case 56:
                return 16;
            case 57:
            case 58:
                return 17;
            case 59:
            case 60:
                return 18;
            case 61:
            case 62:
                return 19;
            case 63:
            case 64:
                return 20;
            case 65:
            case 66:
                return 21;
            case 67:
            case 68:
                return 22;
            case 69:
            case 70:
                return 23;
            case 71:
            case 72:
                return 24;
            case 73:
            case 74:
                return 25;
            case 75:
            case 76:
                return 26;
            case 77:
            case 78:
                return 27;
            case 79:
            case 80:
                return 28;
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
                return 29;
            case 89:
            case 90:
                return 30;
            case 91:
            case 92:
            case 93:
            case 94:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
                return 31;
            default: return 31;
        }
    }
}