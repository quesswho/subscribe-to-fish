package fish;

import battlecode.common.*;
import battlecode.instrumenter.RobotDeathException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * RobotController backed by the C++ engine. Each call crosses JNI once; results that are not a single int come back
 * through a direct buffer and are turned into the same objects RobotControllerImpl returns. Natives return a value
 * >= 0, or the negated index of the exception to throw (see Host.ERRORS).
 */
final class NativeRC implements RobotController {
    // ---- shared state, set by Host ----
    static IntBuffer buf;
    static ByteBuffer rawBuf;
    static int mapWidth, mapHeight;
    private static final Direction[] DIRS = Direction.values();
    private static final UnitType[] TYPES = UnitType.values();
    private static final Team[] TEAMS = Team.values();
    private static final TrapType[] TRAPS = TrapType.values();
    private static final int NULL = Integer.MIN_VALUE;
    static final int ROBOT_RECORD = 10;
    static final int MAPINFO_RECORD = 7;
    /** Mirror of both teams' shared arrays, kept by the engine (jvm.cpp SHARED_MIRROR). */
    static final int SHARED = 65536 - 2 * GameConstants.SHARED_ARRAY_SIZE;

    // Mirrors InternalRobot.getRobotInfo's cache: an unchanged robot is sensed as the same object.
    private static RobotInfo[] infoCache = new RobotInfo[1 << 16];

    static void allocate() {
        rawBuf = ByteBuffer.allocateDirect(4 * 1 << 16).order(ByteOrder.nativeOrder());
        buf = rawBuf.asIntBuffer();
    }

    static void resetGame(int width, int height) {
        mapWidth = width;
        mapHeight = height;
        infoCache = new RobotInfo[1 << 16];
    }

    // ---- natives (engine/src/host/natives.cpp) ----
    static native int nRound();
    static native int nCoop();
    static native int nBackstabber();
    static native int nTrapCount(long h, int type);
    static native int nLoc(long h);
    static native int nParts(long h);
    static native int nDir(long h);
    static native int nHealth(long h);
    static native int nRaw(long h);
    static native int nGlobal(long h);
    static native int nDirt(long h);
    static native int nType(long h);
    static native int nCarrying(long h);
    static native int nState(long h, int which);
    static native int nCanSense(long h, int x, int y);
    static native int nOccupied(long h, int x, int y);
    static native int nRobotAt(long h, int x, int y);
    static native int nSenseRobot(long h, int id);
    static native int nNearby(long h, int x, int y, int r, int team);
    static native int nPassable(long h, int x, int y);
    static native int nMapInfo(long h, int x, int y);
    static native int nMapInfos(long h, int x, int y, int r);
    static native int nLocsWithin(long h, int x, int y, int r);
    static native int nCooldown(long h, int which);
    static native int nReady(long h, int which);
    static native int nMove(long h, int dir, int apply);
    static native int nTurn(long h, int dir, int apply);
    static native int nRatCost(long h);
    static native int nBuildRat(long h, int x, int y, int apply);
    static native int nBecomeKing(long h, int apply);
    static native int nDirtAction(long h, int x, int y, int place, int apply);
    static native int nTrapAction(long h, int x, int y, int type, int place, int apply);
    static native int nPickUp(long h, int x, int y, int amount, int apply);
    static native int nAttack(long h, int x, int y, int cheese, int withCheese, int apply);
    static native int nSqueak(long h, int content);
    static native int nSqueaks(long h, int round);
    static native int nWriteShared(long h, int index, int value);
    static native int nReadShared(long h, int index);
    static native int nTransfer(long h, int x, int y, int amount, int apply);
    static native int nThrow(long h, int apply);
    static native int nDrop(long h, int dir, int apply);
    static native int nCarry(long h, int x, int y, int apply);
    static native void nResign(long h);
    static native String nEngineError();

    // ---- per robot ----
    final long h;
    private final int id;
    private final Team team;
    private int lastLocPacked = NULL;
    private MapLocation lastLoc;

    NativeRC(long handle, int id, int team) {
        this.h = handle;
        this.id = id;
        this.team = TEAMS[team];
    }

    // ---- helpers ----
    private static int check(int r) throws GameActionException {
        if (r < 0) Host.raise(-r);
        return r;
    }

    private static void ok(int r) throws GameActionException {
        if (r < 0) Host.raise(-r);
    }

    private static boolean can(int r) {
        if (r == 0) return true;
        // Unchecked exceptions (null arguments, engine errors) escape can* methods as they do in the official engine.
        if (Host.isUnchecked(-r)) Host.raiseUnchecked(-r);
        return false;
    }

    private static void notNull(Object o) {
        if (o == null) throw new NullPointerException("Argument has an invalid null value");
    }

    private static int x(MapLocation l) {
        return l == null ? NULL : l.x;
    }

    private static int y(MapLocation l) {
        return l == null ? NULL : l.y;
    }

    private static int dir(Direction d) {
        return d == null ? -1 : d.ordinal();
    }

    private static MapLocation unpack(int p) {
        return new MapLocation(p >> 16, (short) p);
    }

    static RobotInfo robotInfo(int at) {
        IntBuffer b = buf;
        int id = b.get(at);
        RobotInfo carried = null;
        int carriedId = b.get(at + 9);
        if (carriedId != 0) carried = robotInfo(at + ROBOT_RECORD);
        Team t = TEAMS[b.get(at + 1)];
        UnitType ty = TYPES[b.get(at + 2)];
        int health = b.get(at + 3);
        int x = b.get(at + 4), y = b.get(at + 5);
        Direction d = DIRS[b.get(at + 6)];
        int chirality = b.get(at + 7), cheese = b.get(at + 8);
        RobotInfo cached = id < infoCache.length ? infoCache[id] : null;
        if (cached != null && cached.ID == id && cached.team == t && cached.type == ty && cached.health == health
                && cached.cheeseAmount == cheese && cached.chirality == chirality && cached.direction == d
                && cached.carryingRobot == carried && cached.location.x == x && cached.location.y == y)
            return cached;
        RobotInfo info = new RobotInfo(id, t, ty, health, new MapLocation(x, y), d, chirality, cheese, carried);
        if (id < infoCache.length) infoCache[id] = info;
        return info;
    }

    private static RobotInfo[] robotInfos(int n) {
        RobotInfo[] out = new RobotInfo[n];
        for (int i = 0; i < n; i++) out[i] = robotInfo(i * 2 * ROBOT_RECORD);
        return out;
    }

    private static MapInfo mapInfo(int at) {
        IntBuffer b = buf;
        int flags = b.get(at + 2);
        int flyingId = b.get(at + 5);
        RobotInfo flying = flyingId != 0 ? robotInfo(b.get(at + 6)) : null;
        return new MapInfo(new MapLocation(b.get(at), b.get(at + 1)), (flags & 1) != 0, flying, (flags & 2) != 0,
                (flags & 4) != 0, b.get(at + 3), TRAPS[b.get(at + 4)], (flags & 8) != 0);
    }

    private static MapLocation[] locations(int n) {
        MapLocation[] out = new MapLocation[n];
        for (int i = 0; i < n; i++) out[i] = unpack(buf.get(i));
        return out;
    }

    // ---- global ----
    @Override public int getRoundNum() { return nRound(); }
    @Override public int getMapWidth() { return mapWidth; }
    @Override public int getMapHeight() { return mapHeight; }
    @Override public boolean isCooperation() { return nCoop() != 0; }

    @Override
    public Team getBackstabbingTeam() {
        int t = nBackstabber();
        return t < 0 ? null : TEAMS[t];
    }

    @Override public int getNumberCatTraps() { return nTrapCount(h, TrapType.CAT_TRAP.ordinal()); }
    @Override public int getNumberRatTraps() { return nTrapCount(h, TrapType.RAT_TRAP.ordinal()); }

    // ---- unit ----
    @Override public int getID() { return id; }
    @Override public Team getTeam() { return team; }

    @Override
    public MapLocation getLocation() {
        int p = nLoc(h);
        if (p != lastLocPacked) {
            lastLocPacked = p;
            lastLoc = unpack(p);
        }
        return lastLoc;
    }

    @Override public MapLocation[] getAllPartLocations() { return locations(nParts(h)); }
    @Override public Direction getDirection() { return DIRS[nDir(h)]; }
    @Override public int getHealth() { return nHealth(h); }
    @Override public int getRawCheese() { return nRaw(h); }
    @Override public int getGlobalCheese() { return nGlobal(h); }
    @Override public int getAllCheese() { return nRaw(h) + nGlobal(h); }
    @Override public int getDirt() { return nDirt(h); }
    @Override public UnitType getType() { return TYPES[nType(h)]; }

    @Override
    public RobotInfo getCarrying() {
        return nCarrying(h) == 0 ? null : robotInfo(0);
    }

    @Override public boolean isBeingThrown() { return nState(h, 0) != 0; }
    @Override public boolean isBeingCarried() { return nState(h, 1) != 0; }

    // ---- sensing ----
    @Override
    public boolean onTheMap(MapLocation loc) {
        notNull(loc);
        return loc.x >= 0 && loc.y >= 0 && loc.x < mapWidth && loc.y < mapHeight;
    }

    @Override
    public boolean canSenseLocation(MapLocation loc) {
        notNull(loc);
        return nCanSense(h, loc.x, loc.y) != 0;
    }

    @Override
    public boolean isLocationOccupied(MapLocation loc) throws GameActionException {
        notNull(loc);
        return check(nOccupied(h, loc.x, loc.y)) != 0;
    }

    @Override
    public boolean canSenseRobotAtLocation(MapLocation loc) {
        notNull(loc);
        int r = nOccupied(h, loc.x, loc.y);
        return r > 0;
    }

    @Override
    public RobotInfo senseRobotAtLocation(MapLocation loc) throws GameActionException {
        notNull(loc);
        int r = check(nRobotAt(h, loc.x, loc.y));
        return r == 0 ? null : robotInfo(0);
    }

    @Override
    public boolean canSenseRobot(int id) {
        return nSenseRobot(h, id) > 0;
    }

    @Override
    public RobotInfo senseRobot(int id) throws GameActionException {
        if (nSenseRobot(h, id) <= 0) Host.raise(Host.CANT_SENSE_ROBOT);
        return robotInfo(0);
    }

    @Override
    public RobotInfo[] senseNearbyRobots() {
        MapLocation c = getLocation();
        int n = nNearby(h, c.x, c.y, -1, -1);
        return n < 0 ? new RobotInfo[0] : robotInfos(n);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(int radiusSquared) throws GameActionException {
        MapLocation c = getLocation();
        return robotInfos(check(nNearby(h, c.x, c.y, radiusSquared, -1)));
    }

    @Override
    public RobotInfo[] senseNearbyRobots(int radiusSquared, Team team) throws GameActionException {
        MapLocation c = getLocation();
        return robotInfos(check(nNearby(h, c.x, c.y, radiusSquared, team == null ? -1 : team.ordinal())));
    }

    @Override
    public RobotInfo[] senseNearbyRobots(MapLocation center, int radiusSquared, Team team)
            throws GameActionException {
        notNull(center);
        return robotInfos(check(nNearby(h, center.x, center.y, radiusSquared, team == null ? -1 : team.ordinal())));
    }

    @Override
    public boolean sensePassability(MapLocation loc) throws GameActionException {
        notNull(loc);
        return check(nPassable(h, loc.x, loc.y)) != 0;
    }

    @Override
    public MapInfo senseMapInfo(MapLocation loc) throws GameActionException {
        notNull(loc);
        check(nMapInfo(h, loc.x, loc.y));
        return mapInfo(0);
    }

    private MapInfo[] mapInfos(int n) {
        MapInfo[] out = new MapInfo[n];
        for (int i = 0; i < n; i++) out[i] = mapInfo(i * MAPINFO_RECORD);
        return out;
    }

    @Override
    public MapInfo[] senseNearbyMapInfos() {
        MapLocation c = getLocation();
        int n = nMapInfos(h, c.x, c.y, -1);
        return n < 0 ? new MapInfo[0] : mapInfos(n);
    }

    @Override
    public MapInfo[] senseNearbyMapInfos(int radiusSquared) throws GameActionException {
        MapLocation c = getLocation();
        return mapInfos(check(nMapInfos(h, c.x, c.y, radiusSquared)));
    }

    @Override
    public MapInfo[] senseNearbyMapInfos(MapLocation center) throws GameActionException {
        notNull(center);
        return mapInfos(check(nMapInfos(h, center.x, center.y, -1)));
    }

    @Override
    public MapInfo[] senseNearbyMapInfos(MapLocation center, int radiusSquared) throws GameActionException {
        notNull(center);
        return mapInfos(check(nMapInfos(h, center.x, center.y, radiusSquared)));
    }

    @Override
    public MapLocation adjacentLocation(Direction dir) {
        return getLocation().add(dir);
    }

    @Override
    public MapLocation[] getAllLocationsWithinRadiusSquared(MapLocation center, int radiusSquared)
            throws GameActionException {
        notNull(center);
        return locations(check(nLocsWithin(h, center.x, center.y, radiusSquared)));
    }

    // ---- readiness ----
    @Override public boolean isActionReady() { return nReady(h, 0) != 0; }
    @Override public int getActionCooldownTurns() { return nCooldown(h, 0); }
    @Override public boolean isMovementReady() { return nReady(h, 1) != 0; }
    @Override public boolean isTurningReady() { return nReady(h, 2) != 0; }
    @Override public int getMovementCooldownTurns() { return nCooldown(h, 1); }
    @Override public int getTurningCooldownTurns() { return nCooldown(h, 2); }

    // ---- movement ----
    @Override public boolean canMoveForward() { return can(nMove(h, nDir(h), 0)); }
    @Override public boolean canMove(Direction d) { return can(nMove(h, dir(d), 0)); }
    @Override public void moveForward() throws GameActionException { ok(nMove(h, nDir(h), 1)); }
    @Override public void move(Direction d) throws GameActionException { ok(nMove(h, dir(d), 1)); }
    @Override public boolean canTurn() { return can(nTurn(h, -2, 0)); }
    @Override public boolean canTurn(Direction d) { return can(nTurn(h, dir(d), 0)); }
    @Override public void turn(Direction d) throws GameActionException { ok(nTurn(h, dir(d), 1)); }

    // ---- building ----
    @Override public int getCurrentRatCost() { return nRatCost(h); }

    @Override
    public boolean canBuildRat(MapLocation loc) {
        notNull(loc);
        return can(nBuildRat(h, loc.x, loc.y, 0));
    }

    @Override
    public void buildRat(MapLocation loc) throws GameActionException {
        notNull(loc);
        ok(nBuildRat(h, loc.x, loc.y, 1));
    }

    @Override public boolean canBecomeRatKing() { return can(nBecomeKing(h, 0)); }
    @Override public void becomeRatKing() throws GameActionException { ok(nBecomeKing(h, 1)); }

    @Override public boolean canPlaceDirt(MapLocation loc) { return can(nDirtAction(h, x(loc), y(loc), 1, 0)); }
    @Override public void placeDirt(MapLocation loc) throws GameActionException { ok(nDirtAction(h, x(loc), y(loc), 1, 1)); }
    @Override public boolean canRemoveDirt(MapLocation loc) { return can(nDirtAction(h, x(loc), y(loc), 0, 0)); }
    @Override public void removeDirt(MapLocation loc) throws GameActionException { ok(nDirtAction(h, x(loc), y(loc), 0, 1)); }

    private static final int RAT = 0, CAT = 1;

    @Override public boolean canPlaceRatTrap(MapLocation loc) { return can(nTrapAction(h, x(loc), y(loc), RAT, 1, 0)); }
    @Override public void placeRatTrap(MapLocation loc) throws GameActionException { ok(nTrapAction(h, x(loc), y(loc), RAT, 1, 1)); }
    @Override public boolean canRemoveRatTrap(MapLocation loc) { return can(nTrapAction(h, x(loc), y(loc), RAT, 0, 0)); }
    @Override public void removeRatTrap(MapLocation loc) throws GameActionException { ok(nTrapAction(h, x(loc), y(loc), RAT, 0, 1)); }
    @Override public boolean canPlaceCatTrap(MapLocation loc) { return can(nTrapAction(h, x(loc), y(loc), CAT, 1, 0)); }
    @Override public void placeCatTrap(MapLocation loc) throws GameActionException { ok(nTrapAction(h, x(loc), y(loc), CAT, 1, 1)); }
    @Override public boolean canRemoveCatTrap(MapLocation loc) { return can(nTrapAction(h, x(loc), y(loc), CAT, 0, 0)); }
    @Override public void removeCatTrap(MapLocation loc) throws GameActionException { ok(nTrapAction(h, x(loc), y(loc), CAT, 0, 1)); }

    @Override public boolean canPickUpCheese(MapLocation loc) { return can(nPickUp(h, x(loc), y(loc), NULL, 0)); }
    @Override public void pickUpCheese(MapLocation loc) throws GameActionException { ok(nPickUp(h, x(loc), y(loc), NULL, 1)); }

    @Override
    public void pickUpCheese(MapLocation loc, int pickUpAmount) throws GameActionException {
        ok(nPickUp(h, x(loc), y(loc), pickUpAmount == NULL ? 0 : pickUpAmount, 1));
    }

    // ---- attacking ----
    @Override
    public boolean canAttack(MapLocation loc) {
        if (loc == null) return false;
        return can(nAttack(h, loc.x, loc.y, 0, 0, 0));
    }

    @Override
    public boolean canAttack(MapLocation loc, int cheeseAmount) {
        if (loc == null) return false;
        return can(nAttack(h, loc.x, loc.y, cheeseAmount, 1, 0));
    }

    @Override
    public void attack(MapLocation loc) throws GameActionException {
        if (loc == null) Host.raise(Host.ATTACK_NULL);
        ok(nAttack(h, loc.x, loc.y, 0, 0, 1));
    }

    @Override
    public void attack(MapLocation loc, int cheeseAmount) throws GameActionException {
        if (loc == null) Host.raise(Host.ATTACK_NULL);
        ok(nAttack(h, loc.x, loc.y, cheeseAmount, 1, 1));
    }

    // ---- communication ----
    @Override public boolean squeak(int messageContent) { return nSqueak(h, messageContent) != 0; }

    @Override
    public Message[] readSqueaks(int roundNum) {
        int n = nSqueaks(h, roundNum);
        Message[] out = new Message[n];
        IntBuffer b = buf;
        for (int i = 0; i < n; i++) {
            int at = 5 * i;
            out[i] = new Message(b.get(at), b.get(at + 1), b.get(at + 2), new MapLocation(b.get(at + 3), b.get(at + 4)));
        }
        return out;
    }

    @Override
    public void writeSharedArray(int index, int value) throws GameActionException {
        ok(nWriteShared(h, index, value));
    }

    @Override
    public int readSharedArray(int index) throws GameActionException {
        if (index < 0 || index >= GameConstants.SHARED_ARRAY_SIZE)
            throw new ArrayIndexOutOfBoundsException("Index " + index + " out of bounds for length "
                    + GameConstants.SHARED_ARRAY_SIZE);
        return buf.get(SHARED + team.ordinal() * GameConstants.SHARED_ARRAY_SIZE + index);
    }

    // ---- cheese, carrying ----
    @Override
    public boolean canTransferCheese(MapLocation loc, int amount) {
        notNull(loc);
        return can(nTransfer(h, loc.x, loc.y, amount, 0));
    }

    @Override
    public void transferCheese(MapLocation loc, int amount) throws GameActionException {
        notNull(loc);
        ok(nTransfer(h, loc.x, loc.y, amount, 1));
    }

    @Override public void throwRat() throws GameActionException { ok(nThrow(h, 1)); }
    @Override public boolean canThrowRat() { return can(nThrow(h, 0)); }
    @Override public void dropRat(Direction d) throws GameActionException { ok(nDrop(h, dir(d), 1)); }
    @Override public boolean canDropRat(Direction d) { return can(nDrop(h, dir(d), 0)); }

    @Override
    public boolean canCarryRat(MapLocation loc) {
        notNull(loc);
        return can(nCarry(h, loc.x, loc.y, 0));
    }

    @Override
    public void carryRat(MapLocation loc) throws GameActionException {
        notNull(loc);
        ok(nCarry(h, loc.x, loc.y, 1));
    }

    // ---- other ----
    @Override
    public void disintegrate() {
        throw new RobotDeathException();
    }

    @Override
    public void resign() {
        nResign(h);
    }

    @Override
    public void setIndicatorString(String string) {
        string.length();
    }

    @Override
    public void setIndicatorDot(MapLocation loc, int red, int green, int blue) throws GameActionException {
        notNull(loc);
        if (!onTheMap(loc)) Host.raise(Host.INDICATOR_DOT_OFF_MAP);
    }

    @Override
    public void setIndicatorLine(MapLocation startLoc, MapLocation endLoc, int red, int green, int blue)
            throws GameActionException {
        notNull(startLoc);
        notNull(endLoc);
        if (!onTheMap(startLoc) || !onTheMap(endLoc)) Host.raise(Host.INDICATOR_LINE_OFF_MAP);
    }

    @Override
    public void setTimelineMarker(String label, int red, int green, int blue) {
        label.length();
    }

    @Override
    public int hashCode() {
        return id;
    }
}
