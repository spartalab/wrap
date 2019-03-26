package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.ModalPAMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

import java.io.IOException;
import java.sql.*;
import java.util.*;

/**This is used to build the PA map by reading
 * database information.
 *
 * NOTE: Rishabh finish the documentation for this
 *
 * @author Rishabh
 *
 */
public class DBModalPAMap implements ModalPAMap {

    private final static String dbName = "sta";
    private static Properties p;
    private Connection databaseCon;
    private String tableName;
    private Map<Node, Float> attractors;
    private Map<Node, Float> producers;
    private int origin;
    private Mode mode;
    private float vot;
    private Graph g;

    public DBModalPAMap(Graph g, Node n, String table, Mode m) {
        p = new Properties();
        try {
            p.load(DBModalPAMap.class.getResourceAsStream("dbConfig.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadDatabase();
        this.g = g;
        tableName = table;
        origin = n.getID();
        mode = m;
        producers = new HashMap<>();
        attractors = new HashMap<>();
    }

    private void loadDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
            databaseCon = DriverManager.getConnection("jdbc:postgresql://db1.wrangler.tacc.utexas.edu/" + dbName, p.getProperty("user"), p.getProperty("pass"));
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Could not find database/table to connect to");
            System.exit(1);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    private void developFFMap() {
        String query = "SELECT * FROM " + tableName + " WHERE Origin = " + origin;
        try (PreparedStatement ps = databaseCon.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            databaseCon.setAutoCommit(false);
            ps.setFetchSize(100);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int dest = rs.getInt("Destination");
                float attrVal = (float) 0;
                float prodVal = (float) 0;
                //TODO: Calculate productions and attractions based on values in the table and the mode
                switch (mode) {
                    case MED_TRUCK:
                        break;
                    case HVY_TRUCK:
                        break;
                    case SINGLE_OCC:
                        break;
                    case HOV_2:
                        break;
                    case HOV_3:
                        break;
                    case WALK_TRANSIT:
                        break;
                    case DRIVE_TRANSIT:
                        break;
                }
                if(attrVal != 0)
                    attractors.put(getGraph().getNode(dest), attrVal);
                if(prodVal != 0)
                    producers.put(getGraph().getNode(dest), attrVal);

            }
            databaseCon.setAutoCommit(true);
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public Set<Node> getProducers () {
        return producers.keySet();
    }

    @Override
    public Set<Node> getAttractors () {
        return attractors.keySet();
    }

    @Override
    public Float getAttractions (Node z){
        return attractors.getOrDefault(z, 0f);
    }

    @Override
    public Float getProductions (Node z){ return producers.getOrDefault(z, 0f); }

    @Override
    public Float getVOT () { return vot; }

    @Override
    public Mode getVehicleClass () {
        return mode;
    }

    @Override
    public Graph getGraph() {
        return g;
    }

    @Override
    public Mode getMode() {
        return mode;
    }
}
