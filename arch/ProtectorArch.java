package arch;

import jason.environment.grid.Location;

public class ProtectorArch extends LocalMinerArch { // většina kódu je stejná jako v MinerArch
    /**
     * Protector nepotřebuje posílat informace o své poloze a zlatě,
     * protože se po zakotvení nehýbe. Odstraníme broadcast.
     */
    @Override
    void locationPerceived(int x, int y) {
        Location oldLoc = model.getAgPos(getMyId());
        if (oldLoc != null) {
            model.clearAgView(oldLoc);
        }
        if (oldLoc == null || !oldLoc.equals(new Location(x,y))) {
            try {
                model.setAgPos(getMyId(), x, y);
                model.incVisited(x, y);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Vrací vždy false, aby zabránil Architektuře vnutit agentovi restart,
     * když "zamrzne" na depotu.
     */
    @Override
    public boolean isRobotFrozen() {
        return false;
    }
}
