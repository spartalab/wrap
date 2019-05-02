package edu.utexas.wrap.generation;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.DBPAMap;
import edu.utexas.wrap.net.Graph;
import java.sql.*;

/**
 *
 * This is a trip generation implementation based on reading attribute weights and their values associated to each
 * zone/node. This model loads a map of weights from an attributes table and loads the respective values of each node
 * and generates an AggregatePAMap object.
 *
 * @author Rishabh
 */
public class DBTripGenerator extends TripGenerator {

    private Connection db;
    private String attributesTable;
    private String productionWeightsTable;
    private String attractionWeightsTable;
    private PAMap paMap;
    private Graph g;
    private String tableName;
    private String marketSegment;
    private float vot;


    /**
     * Constructor for Trip Generation object based on the database
     * @param g Graph that the trip generation is based on
     * @param attributesTable Name of table where each node's attribute values are stored
     * @param prodTable Name of table where the production attribute weights are stored
     * @param attrTable Name of table where the attraction attribute weights are stored
     */
    public DBTripGenerator(Graph g, Connection db, String paTable, String attributesTable, String prodTable, String attrTable, String marketSegment, float vot) {
        this.g  = g;
        this.attributesTable = attributesTable;
        this.productionWeightsTable = prodTable;
        this.attractionWeightsTable = attrTable;
        this.db = db;
        this.marketSegment = marketSegment;
        this.vot = vot;
        this.tableName = paTable;
    }

    /**
     * This method generates the PA map using SQL linear algebra.
     *
     *  The generatePA SQL statement uses inner joins to compute a "dot-product" of rates and values to produce the PA Map.
     *
     *  The productions and attractions are calculated by an inner join between the attraction rates table, the production
     *  rates table, and the demographic data table on the demographics of the model. It then multiplies the rates to the
     *  respective values in the demographic data table and sums them for each node. This produces the productions and
     *  attractions values that are inserted along with the the node ID into the paMap table.
     * attributesTable: table with the demographics values: (node, dem, value)
     * productionWeightsTable: table with the production rates associated with the market segment: (seg, dem, rate)
     * attractionWeightsTable: table with the attraction rates associated with the market segment: (seg, dem, rate)
     */
    @Override
    public PAMap generate() {
        if(paMap == null) {
            paMap = new DBPAMap(g, db, tableName, vot);
            String generatePA = "INSERT INTO ? (node, productions, attractions)\n" +
                    "SELECT node, sum(p.rate * dp.value), sum(a.rate * dp.value)\n" +
                    "  FROM ((SELECT * from ?) as dp INNER JOIN (SELECT * FROM ? WHERE seg=?) as p ON\n" +
                    "  dp.dem = p.dem INNER JOIN (SELECT * FROM ? WHERE seg=?) as a ON dp.dem=a.dem) group by node;\n";
            try (PreparedStatement ps = db.prepareStatement(generatePA)) {
                ps.setString(1, tableName);
                ps.setString(2, attributesTable);
                ps.setString(3, productionWeightsTable);
                ps.setString(4, marketSegment);
                ps.setString(5, attractionWeightsTable);
                ps.setString(6, marketSegment);
                ps.executeUpdate();
            } catch (SQLException s) {
                s.printStackTrace();
                System.exit(1);
            }
            return paMap;
        }
        return paMap;
    }

    /**
     * @return instance of the Graph
     */
    public Graph getGraph() { return g; }
}
