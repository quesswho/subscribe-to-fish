

package awubot.robot.rat.goHome;

import battlecode.common.*;
import static awubot.Game.*;
import awubot.comms.GlobalArray;
import static awubot.util.Util.*;
import awubot.util.Profiler;

public class AntColony {
    public static StringBuilder[][] mark;
    private static char[] base = new char[5];
    private static char curMark;
    private static int curKing = 0;
    private static MapLocation[] lastKingLocs = new MapLocation[5];
    public static boolean shouldMark;
    private static void resetMarks(int i) {
        // Profiler.begin();
        base[i] = (char)(65536 - 3600);
        StringBuilder row = new StringBuilder();
        row.repeat('\uFFFF', mapWidth);
        var rows = mark[i];
        rows[0] = new StringBuilder(row);
        rows[1] = new StringBuilder(row);
        rows[2] = new StringBuilder(row);
        rows[3] = new StringBuilder(row);
        rows[4] = new StringBuilder(row);
        rows[5] = new StringBuilder(row);
        rows[6] = new StringBuilder(row);
        rows[7] = new StringBuilder(row);
        rows[8] = new StringBuilder(row);
        rows[9] = new StringBuilder(row);
        rows[10] = new StringBuilder(row);
        rows[11] = new StringBuilder(row);
        rows[12] = new StringBuilder(row);
        rows[13] = new StringBuilder(row);
        rows[14] = new StringBuilder(row);
        rows[15] = new StringBuilder(row);
        rows[16] = new StringBuilder(row);
        rows[17] = new StringBuilder(row);
        rows[18] = new StringBuilder(row);
        rows[19] = new StringBuilder(row);
        rows[20] = new StringBuilder(row);
        rows[21] = new StringBuilder(row);
        rows[22] = new StringBuilder(row);
        rows[23] = new StringBuilder(row);
        rows[24] = new StringBuilder(row);
        rows[25] = new StringBuilder(row);
        rows[26] = new StringBuilder(row);
        rows[27] = new StringBuilder(row);
        rows[28] = new StringBuilder(row);
        rows[29] = new StringBuilder(row);
        rows[30] = new StringBuilder(row);
        rows[31] = new StringBuilder(row);
        rows[32] = new StringBuilder(row);
        rows[33] = new StringBuilder(row);
        rows[34] = new StringBuilder(row);
        rows[35] = new StringBuilder(row);
        rows[36] = new StringBuilder(row);
        rows[37] = new StringBuilder(row);
        rows[38] = new StringBuilder(row);
        rows[39] = new StringBuilder(row);
        rows[40] = new StringBuilder(row);
        rows[41] = new StringBuilder(row);
        rows[42] = new StringBuilder(row);
        rows[43] = new StringBuilder(row);
        rows[44] = new StringBuilder(row);
        rows[45] = new StringBuilder(row);
        rows[46] = new StringBuilder(row);
        rows[47] = new StringBuilder(row);
        rows[48] = new StringBuilder(row);
        rows[49] = new StringBuilder(row);
        rows[50] = new StringBuilder(row);
        rows[51] = new StringBuilder(row);
        rows[52] = new StringBuilder(row);
        rows[53] = new StringBuilder(row);
        rows[54] = new StringBuilder(row);
        rows[55] = new StringBuilder(row);
        rows[56] = new StringBuilder(row);
        rows[57] = new StringBuilder(row);
        rows[58] = new StringBuilder(row);
        rows[59] = new StringBuilder(row);
        // Profiler.end("ant colony init");
    }
    public static void init() {
        mark = new StringBuilder[5][60];
        curKing = GlobalArray.nearestRatKingId;
        resetMarks(curKing);
        shouldMark = true;
        curMark = (char)(base[0] + 5);
    }
    public static void reachedHome() {
        curKing = GlobalArray.nearestRatKingId;
        shouldMark = true;
        curMark = (char)(base[curKing] + 5);
        // indicate("reached home " + GlobalArray.nearestRatKing);
    }
    public static void markCurrent() {
        if(!shouldMark) {
            // indicate("don't mark");
            return;
        }
        // indicate("mark " + (int)curMark);
        var row = mark[curKing][myLocY];
        row.setCharAt(myLocX, (char)Math.min(curMark, row.charAt(myLocX)));
        curMark++;
    }
    public static void preTick() {
        for(int i = 0; i < 5; i++) {
            var kingLoc = GlobalArray.getRatKing(i);
            if(kingLoc != null) {
                if(lastKingLocs[i] == null || !kingLoc.isAdjacentTo(lastKingLocs[i])) {
                    // detected teleportation or first time, reset this king's marks
                    resetMarks(i);
                } else if(!kingLoc.equals(lastKingLocs[i])) {
                    // king moved, decrease base mark (equivalent to incrementing all existing marks)
                    // indicate("king " + i + " moved");
                    mark[i][lastKingLocs[i].y + -1].setCharAt(lastKingLocs[i].x + -1, base[i]);
                    mark[i][lastKingLocs[i].y + 1].setCharAt(lastKingLocs[i].x + -1, base[i]);
                    mark[i][lastKingLocs[i].y + -1].setCharAt(lastKingLocs[i].x + 1, base[i]);
                    mark[i][lastKingLocs[i].y + 1].setCharAt(lastKingLocs[i].x + 1, base[i]);
                    base[i]--;
                }
                lastKingLocs[i] = kingLoc;
            }
        }
    }
}