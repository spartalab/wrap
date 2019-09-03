package edu.utexas.wrap.demand.containers.database;

import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;

import java.sql.Connection;

public class DBModalPAMatrix extends DBPAMatrix implements ModalPAMatrix {

    Mode m;
    public DBModalPAMatrix(Graph g, String table, Connection db, float vot, Mode m) {
        super(g, db, table, vot);
        this.m  = m;
    }

    @Override
    public Mode getMode() {
        return m;
    }
}
