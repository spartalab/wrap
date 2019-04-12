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
 * This is an implementation of a production/attraction map split up by mode
 * This is likely an incorrect implementation since we are not doing mode choice at the Trip Generation stage
 * @author Rishabh
 *
 */
public class DBModalPAMap implements ModalPAMap {

    private Connection databaseCon;
    private String tableName;
    private Map<Node, Float> attractors;
    private Map<Node, Float> producers;
    private int origin;
    private Mode mode;
    private float vot;
    private Graph g;

    public DBModalPAMap(Graph g, Node n, String table, Mode m, Float vot) {
        loadDatabase();
        this.g = g;
        tableName = table;
        origin = n.getID();
        mode = m;
        producers = new HashMap<>();
        attractors = new HashMap<>();
        this.vot = vot;
    }

    /**
     * Load the database connection from properties file
     */
    private void loadDatabase() {
        Properties p = new Properties();
        try {
            p.load(DBModalPAMap.class.getResourceAsStream("dbConfig.properties"));
        } catch (IOException e) {
            System.out.println("Couldn't open properties file");
            e.printStackTrace();
        }
        try {
            Class.forName("org.postgresql.Driver");
            databaseCon = DriverManager.getConnection(p.getProperty("databaseLink") + p.getProperty("databaseName"), p.getProperty("user"), p.getProperty("pass"));
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Could not find database/table to connect to");
            System.exit(1);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Create a modal PA map based on the specified mode and the function based on values in the table
     * It goes through all of the origins and gets the destination and computes the production and attraction
     * value for the specified mode of this object.
     */
    private void developModalPAMap() {
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
    public Graph getGraph() {
        return g;
    }

    @Override
    public void putAttractions(Node z, Float amt) {
        attractors.put(z, amt);
    }

    @Override
    public void putProductions(Node z, Float amt) {
        producers.put(z, amt);
    }

    @Override
    public Mode getMode() {
        return mode;
    }
}
