package awubot.comms;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class Enemy {
    public MapLocation location;
    public Direction direction;
    public int health;

    public Enemy(RobotInfo info) {
        this.location = info.getLocation();
        this.direction = info.getDirection();
        this.health = info.getHealth();
    }

    public Enemy(MapLocation location, Direction direction, int health) {
        this.location = location;
        this.direction = direction;
        this.health = health;
    }

    public int hashCode() {
        return (location.hashCode() ^ (direction.ordinal() * 67) ^ (health * health)) & ((1 << 16) - 1);
    }
}
