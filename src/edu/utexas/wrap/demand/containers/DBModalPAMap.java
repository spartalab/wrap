package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.ModalPAMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;

import java.sql.*;

/**This is used to read the PA map stored in the database
 *
 * This is an implementation of a production/attraction map split up by mode
 * This is likely an incorrect implementation since we are not doing mode choice
 * at the Trip Generation stage
 *
 * Meh, it could be useful later. -Wm
 *
 * This extends the DBPAMap implementation by taking into account the mode and VOT
 * The rest of the implementation is the same as a normal PAMap stored in the database
 * @author Rishabh
 *
 */
public class DBModalPAMap extends DBPAMap implements ModalPAMap {

    private Mode mode;

    public DBModalPAMap(Graph g, String table,  Connection db, Mode m, Float vot) {
        super(g, table, db, vot);
        mode = m;
    }

    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.ModalPAMap#getMode()
     */
    @Override
    public Mode getMode() {
        return mode;
    }
}
