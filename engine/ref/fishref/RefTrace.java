package fishref;

import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.crossplay.CrossPlayLanguage;
import battlecode.server.Config;
import battlecode.server.GameInfo;
import battlecode.server.GameMaker;
import battlecode.server.GameState;
import battlecode.world.GameMapIO;
import battlecode.world.GameWorld;
import battlecode.world.InternalRobot;
import battlecode.world.LiveMap;
import battlecode.world.TeamInfo;
import battlecode.world.control.NullControlProvider;
import battlecode.world.control.PlayerControlProvider;
import battlecode.world.control.TeamControlProvider;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Plays one game on the official engine and prints the fish state hash after every round, so a fish game can be
 * compared with it round by round (tools/verify).
 *
 *   RefTrace MAP DIR:PKG DIR:PKG SEED [DUMP_ROUND]
 */
public final class RefTrace {
    private static long h;

    private static void mix(long v) {
        h ^= v;
        h *= 1099511628211L;
    }

    static long hash(GameWorld w) {
        h = 0xcbf29ce484222325L;
        List<InternalRobot> robots = new ArrayList<>();
        w.getObjectInfo().eachRobot(r -> { robots.add(r); return true; });
        robots.sort(Comparator.comparingInt(InternalRobot::getID));
        for (InternalRobot r : robots) {
            mix(r.getID()); mix(r.getTeam().ordinal()); mix(r.getType().ordinal());
            mix(r.getLocation().x); mix(r.getLocation().y); mix(r.getDirection().ordinal());
            mix(r.getHealth()); mix(r.getCheese()); mix(r.getActionCooldownTurns());
            mix(r.getMovementCooldownTurns()); mix(r.getTurningCooldownTurns());
        }
        TeamInfo t = w.getTeamInfo();
        for (Team team : new Team[] {Team.A, Team.B}) {
            mix(t.getCheese(team)); mix(t.getDirt(team)); mix(t.getCheeseTransferred(team));
            mix(t.getDamageToCats(team)); mix(t.getNumBabyRats(team)); mix(t.getNumRatKings(team));
        }
        long cheese = 0, dirt = 0;
        LiveMap m = w.getGameMap();
        for (int i = 0; i < m.getWidth() * m.getHeight(); i++) {
            MapLocation l = m.indexToLocation(i);
            cheese += (long) w.getCheese(l) * (i + 1);
            dirt += (w.getDirt(l) ? 1L : 0L) * (i + 1);
        }
        mix(cheese);
        mix(dirt);
        mix(w.isCooperation() ? 1 : 0);
        return h;
    }

    static void dump(GameWorld w) {
        List<InternalRobot> robots = new ArrayList<>();
        w.getObjectInfo().eachRobot(r -> { robots.add(r); return true; });
        robots.sort(Comparator.comparingInt(InternalRobot::getID));
        for (InternalRobot r : robots)
            System.err.println("  robot " + r.getID() + " t" + r.getTeam().ordinal() + " u" + r.getType().ordinal()
                    + " @" + r.getLocation().x + "," + r.getLocation().y + " d" + r.getDirection().ordinal()
                    + " hp" + r.getHealth() + " ch" + r.getCheese() + " cd" + r.getActionCooldownTurns() + "/"
                    + r.getMovementCooldownTurns() + "/" + r.getTurningCooldownTurns());
        TeamInfo t = w.getTeamInfo();
        for (Team team : new Team[] {Team.A, Team.B})
            System.err.println("  team " + team.ordinal() + " cheese " + t.getCheese(team) + " dirt " + t.getDirt(team)
                    + " moved " + t.getCheeseTransferred(team) + " catdmg " + t.getDamageToCats(team) + " babies "
                    + t.getNumBabyRats(team) + " kings " + t.getNumRatKings(team) + " traps "
                    + w.getTrapCount(battlecode.common.TrapType.RAT_TRAP, team) + "/"
                    + w.getTrapCount(battlecode.common.TrapType.CAT_TRAP, team));
        long cheese = 0, dirt = 0;
        LiveMap m = w.getGameMap();
        for (int i = 0; i < m.getWidth() * m.getHeight(); i++) {
            MapLocation l = m.indexToLocation(i);
            cheese += (long) w.getCheese(l) * (i + 1);
            dirt += (w.getDirt(l) ? 1L : 0L) * (i + 1);
        }
        System.err.println("  map cheese " + cheese + " dirt " + dirt + " coop " + (w.isCooperation() ? 1 : 0));
    }

    public static void main(String[] args) throws Exception {
        String mapPath = args[0];
        String[] a = args[1].split(":(?=[^:]*$)"), b = args[2].split(":(?=[^:]*$)");
        int seed = Integer.parseInt(args[3]);
        int dumpRound = args.length > 4 ? Integer.parseInt(args[4]) : -1;

        System.setProperty("bc.server.mode", "headless");
        System.setProperty("bc.server.robot-player-to-system-out", "false");
        System.setProperty("bc.engine.debug-methods", "false");
        System.setProperty("bc.engine.silence-a", "true");
        System.setProperty("bc.engine.silence-b", "true");
        Config config = new Config(new String[] {"-c=-"});
        Config.setGlobalConfig(config);

        LiveMap file = GameMapIO.loadMap(new FileInputStream(mapPath), false);
        LiveMap map = new LiveMap(file.getWidth(), file.getHeight(), file.getOrigin(), seed, file.getRounds(),
                file.getMapName(), file.getSymmetry(), file.getWallArray(), file.getDirtArray(),
                file.getCheeseMineArray(), file.getCheeseArray(), file.getCatWaypointIDs(), file.getCatWaypoints(),
                file.getInitialBodies());
        map.assertIsValid();

        String urlA = new File(a[0]).getAbsoluteFile().toURI().toURL().toString();
        String urlB = new File(b[0]).getAbsoluteFile().toURI().toURL().toString();
        File save = File.createTempFile("fishref", ".bc26");
        save.deleteOnExit();
        GameInfo info = new GameInfo("A", CrossPlayLanguage.JAVA, a[1], urlA, "B", CrossPlayLanguage.JAVA, b[1], urlB,
                new String[] {file.getMapName()}, save, false);
        GameMaker maker = new GameMaker(info, null, false);
        maker.makeGameHeader();

        TeamControlProvider prov = new TeamControlProvider();
        prov.registerControlProvider(Team.A, new PlayerControlProvider(Team.A, a[1], CrossPlayLanguage.JAVA, null, urlA,
                maker.getMatchMaker().getOut(), false));
        prov.registerControlProvider(Team.B, new PlayerControlProvider(Team.B, b[1], CrossPlayLanguage.JAVA, null, urlB,
                maker.getMatchMaker().getOut(), false));
        prov.registerControlProvider(Team.NEUTRAL, new NullControlProvider());

        long start = System.nanoTime();
        GameWorld world = new GameWorld(map, prov, maker.getMatchMaker());
        while (true) {
            if (world.runRound() == GameState.DONE) break;
            System.err.printf("R %d %016x%n", world.getCurrentRound(), hash(world));
            if (world.getCurrentRound() == dumpRound) dump(world);
        }
        Team winner = world.getWinner();
        System.out.printf("{\"map\":\"%s\",\"seed\":%d,\"winner\":%s,\"reason\":\"%s\",\"round\":%d,\"seconds\":%.3f}%n",
                file.getMapName(), seed, winner == null ? "null" : "\"" + winner + "\"",
                world.getGameStats().getDominationFactor(), world.getCurrentRound(), (System.nanoTime() - start) / 1e9);
        System.exit(0);
    }
}
