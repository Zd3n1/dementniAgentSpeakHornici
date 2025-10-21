package env;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.logging.Logger;


/**
 * Class used to model the scenario (for an global view -- used by environment simulator)
 *
 * @author Jomi
 */
public class WorldModel extends GridWorldModel {

    public static final int   GOLD  = 16;
    public static final int   DEPOT = 32;
    public static final int   ENEMY = 64;
    public static final int   TARGET = 128; // one agent target location

    public static final int   AG_CAPACITY = 3; // how many golds an agent can carry

    double                    PSim = 0.1; // probability of action/information failure
    double                    PMax = 0.5; // maximal value for action/information failure

    Location                  depot;
    int[]                     goldsWithAg;  // how many golds each agent is carrying

    int                       goldsInDepotRed  = 0; // #golds the red team puts in the depot
    int                       goldsInDepotBlue = 0; // #golds the blue team puts in the depot
    int                       initialNbGolds = 0;

    int                       maxSteps = 0; // number of steps of the simulation

    private Logger            logger   = Logger.getLogger("jasonTeamSimLocal.mas2j." + WorldModel.class.getName());

    int agsByTeam = 6;

    boolean[] lockedAgents; // true if agent is fixed (can not move or be moved)
    int[] protectReservations; // owner agent id for each orthogonal slot (-1 = free)

    public enum Move {
        UP, DOWN, RIGHT, LEFT
    };


    public static WorldModel create(int w, int h, int nbAg) {
        return new WorldModel(w,h,nbAg);
    }

    public WorldModel(int w, int h, int nbAg) {
        super(w, h, nbAg);

        agsByTeam = nbAg/2;

        goldsWithAg = new int[nbAg];
        lockedAgents = new boolean[nbAg];
        for (int i=0; i< nbAg; i++) lockedAgents[i] = false;
        protectReservations = new int[4];
        for (int i=0; i<4; i++) protectReservations[i] = -1;
        for (int i=0; i< goldsWithAg.length; i++) goldsWithAg[i] = 0;
    }

    public synchronized boolean reserveProtectSlot(int slot, int ag) {
        if (slot < 0 || slot >= protectReservations.length) return false;
        if (protectReservations[slot] == -1 || protectReservations[slot] == ag) {
            protectReservations[slot] = ag;
            return true;
        }
        return false;
    }

    public synchronized void freeProtectSlot(int slot) {
        if (slot < 0 || slot >= protectReservations.length) return;
        protectReservations[slot] = -1;
    }

    public synchronized void freeProtectSlotForAgent(int ag) {
        if (protectReservations == null) return;
        for (int i = 0; i < protectReservations.length; i++) {
            if (protectReservations[i] == ag) protectReservations[i] = -1;
        }
    }

    public synchronized int getProtectSlotOwner(int slot) {
        if (slot < 0 || slot >= protectReservations.length) return -1;
        return protectReservations[slot];
    }

    // map a location to slot index 0=up,1=right,2=down,3=left or -1 if not orth neighbor
    public int getSlotIndexForLocation(Location l) {
        if (depot == null || l == null) return -1;
        int dx = l.x - depot.x;
        int dy = l.y - depot.y;
        if (dx == 0 && dy == -1) return 0; // up
        if (dx == 1 && dy == 0) return 1;  // right
        if (dx == 0 && dy == 1) return 2;  // down
        if (dx == -1 && dy == 0) return 3; // left
        return -1;
    }

    public int getSlotIndexForXY(int x, int y) {
        if (depot == null) return -1;
        int dx = x - depot.x;
        int dy = y - depot.y;
        if (dx == 0 && dy == -1) return 0; // up
        if (dx == 1 && dy == 0) return 1;  // right
        if (dx == 0 && dy == 1) return 2;  // down
        if (dx == -1 && dy == 0) return 3; // left
        return -1;
    }

    public int getAgsByTeam() {
        return agsByTeam;
    }

    @Override
    public boolean isFree(int x, int y) {
        return super.isFree(x,y) && !hasObject(ENEMY, x, y);
    }

    public WorldView getView() {
        return (WorldView)view;
    }

    public void setDepot(int x, int y) {
        if (depot != null) {
            data[depot.x][depot.y]    = CLEAN;
        }
        depot = new Location(x, y);
        data[x][y] = DEPOT;
    }


    public Location getDepot() {
        return depot;
    }

    public int getGoldsInDepotBlue() {
        return goldsInDepotBlue;
    }

    public int getGoldsInDepotRed() {
        return goldsInDepotRed;
    }

    public boolean hasGold() {
        return countObjects(GOLD) > 0;
    }

    public boolean isAllGoldsCollected() {
        return goldsInDepotRed + goldsInDepotBlue == initialNbGolds;
    }

    public void setInitialNbGolds(int i) {
        initialNbGolds = i;
    }

    public int getInitialNbGolds() {
        return initialNbGolds;
    }

    public boolean isCarryingGold(int ag) {
        return goldsWithAg[ag] > 0;
    }

    public boolean mayCarryMoreGold(int ag) {
        return goldsWithAg[ag] < AG_CAPACITY;
    }

    public int getGoldsWithAg(int ag) {
        return goldsWithAg[ag];
    }
    public void setGoldsWithAg(int ag, int n) {
        goldsWithAg[ag] = n;
    }

    public void setPSim(double psim) {
        PSim = psim;
    }
    public void setPMax(double pmax) {
        PMax = pmax;
    }

    /** returns the probability of action/perception failure for an agent
        based on the number of golds it is carrying
    */
    public double getAgFatigue(int ag) {
        return getAgFatigue(ag, goldsWithAg[ag]);
    }

    public double getAgFatigue(int ag, int golds) {
        return PSim + ((PMax - PSim)/AG_CAPACITY) * golds;
    }

    public void setMaxSteps(int s) {
        maxSteps = s;
    }
    public int getMaxSteps() {
        return maxSteps;
    }


    /** Actions **/

    synchronized public boolean move(Move dir, int ag) throws Exception {
        // locked agents cannot move
        if (ag >= 0 && ag < lockedAgents.length && lockedAgents[ag]) {
            logger.fine("Agent " + (ag+1) + " is locked and cannot move.");
            return false;
        }
        if (ag < 0) {
            logger.warning("** Trying to move unknown agent!");
            return false;
        }
        Location l = getAgPos(ag);
        if (l == null) {
            logger.warning("** We lost the location of agent " + (ag + 1) + "!"+this);
            return false;
        }
        Location n = null;
        switch (dir) {
        case UP:
            n =  new Location(l.x, l.y - 1);
            break;
        case DOWN:
            n =  new Location(l.x, l.y + 1);
            break;
        case RIGHT:
            n =  new Location(l.x + 1, l.y);
            break;
        case LEFT:
            n =  new Location(l.x - 1, l.y);
            break;
        }
        if (n != null && canMoveTo(ag, n)) {
            // if there is an agent there, move that agent
            if (!hasObject(AGENT, n) || move(dir,getAgAtPos(n))) {
                setAgPos(ag, n);
                return true;
            }
        }
        return false;
    }

    public void setAgentLocked(int ag, boolean locked) {
        if (ag >= 0 && ag < lockedAgents.length) lockedAgents[ag] = locked;
    }

    public boolean isAgentLocked(int ag) {
        if (ag >= 0 && ag < lockedAgents.length) return lockedAgents[ag];
        return false;
    }

    private boolean canMoveTo(int ag, Location l) {
        if (isFreeOfObstacle(l)) {
            if (!l.equals(getDepot()) || isCarryingGold(ag)) { // if depot, the must be carrying gold
                return true;
            }
        }
        return false;
    }

    public boolean pick(int ag) {
        Location l = getAgPos(ag);
        if (hasObject(WorldModel.GOLD, l.x, l.y)) {
            if (getGoldsWithAg(ag) < AG_CAPACITY) {
                remove(WorldModel.GOLD, l.x, l.y);
                goldsWithAg[ag]++;
                // after picking, update lock state (protector should be unlocked when carrying gold)
                updateLockForAgent(ag);
                return true;
            } else {
                logger.warning("Agent " + (ag + 1) + " is trying the pick gold, but it is already carrying "+(AG_CAPACITY)+" golds!");
            }
        } else {
            logger.warning("Agent " + (ag + 1) + " is trying the pick gold, but there is no gold at " + l.x + "x" + l.y + "!");
        }
        return false;
    }

    public boolean drop(int ag) {
        Location l = getAgPos(ag);
        if (isCarryingGold(ag)) {
            if (l.equals(getDepot())) {
                logger.info("Agent " + (ag + 1) + " carried "+goldsWithAg[ag]+" golds to depot!");
                if (ag < agsByTeam)
                    goldsInDepotRed += goldsWithAg[ag];
                else
                    goldsInDepotBlue += goldsWithAg[ag];
                goldsWithAg[ag] = 0;
                // after dropping, maybe re-lock if in protect position
                updateLockForAgent(ag);
            } else {
                add(WorldModel.GOLD, l.x, l.y);
                goldsWithAg[ag]--;
                updateLockForAgent(ag);
            }
            return true;
        }
        return false;
    }

    @Override
    public void setAgPos(int ag, int x, int y) {
        super.setAgPos(ag, x, y);
        updateLockForAgent(ag);
    }

    @Override
    public void setAgPos(int ag, Location l) {
        super.setAgPos(ag, l);
        updateLockForAgent(ag);
    }

    private void updateLockForAgent(int ag) {
        // only blue team protectors are considered for locking
        if (ag < 0 || ag >= lockedAgents.length) return;
        if (depot == null) { // no depot yet
            lockedAgents[ag] = false;
            return;
        }
        Location loc = getAgPos(ag);
        if (loc == null) {
            lockedAgents[ag] = false;
            return;
        }
        // protectors are the blue team (ids >= agsByTeam)
        if (ag >= agsByTeam) {
            // lock if near depot and not carrying gold and the agent owns/can reserve the specific orth slot
            int slot = getSlotIndexForLocation(loc);
            if (slot != -1 && isNearDepot(loc) && !isCarryingGold(ag)) {
                int owner = getProtectSlotOwner(slot);
                boolean reserved = false;
                if (owner == ag) {
                    reserved = true;
                } else if (owner == -1) {
                    reserved = reserveProtectSlot(slot, ag);
                }
                lockedAgents[ag] = reserved;
            } else {
                // not in a protect slot or carrying gold -> unlock and free any reservation
                lockedAgents[ag] = false;
                freeProtectSlotForAgent(ag);
            }
        } else {
            lockedAgents[ag] = false;
        }
        // debug
        //logger.fine("Agent " + (ag+1) + " locked=" + lockedAgents[ag]);
    }

    private boolean isNearDepot(Location l) {
        if (depot == null || l == null) return false;
        int dx = Math.abs(l.x - depot.x);
        int dy = Math.abs(l.y - depot.y);
        // only up, down, left, right
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }


    public void wall(int x1, int y1, int x2, int y2) {
        for (int i=x1; i<=x2; i++) {
            for (int j=y1; j<=y2; j++) {
                data[i][j] = OBSTACLE;
            }
        }
    }

    public String toString() {
        StringBuilder s = new StringBuilder();

        s.append("---------------------------------------------\n|");
        for (int j = 0; j < getHeight(); j++) {
            for (int i = 0; i < getWidth(); i++) {
                if (hasObject(OBSTACLE, i, j)) {
                    s.append('X');
                } else if (hasObject(DEPOT, i, j)) {
                    s.append('O');
                } else if (hasObject(AGENT, i, j)) {
                    s.append((getAgAtPos(i, j)+1)+"");
                } else if (hasObject(GOLD, i, j)) {
                    s.append('G');
                } else if (hasObject(ENEMY, i, j)) {
                    s.append('E');
                } else {
                    s.append(' ');
                }
            }
            s.append("|\n|");
        }
        s.append("---------------------------------------------\n");

        return s.toString();
    }
}
