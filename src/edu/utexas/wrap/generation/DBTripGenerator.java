package edu.utexas.wrap.generation;

import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.DBPAMap;
import edu.utexas.wrap.net.Graph;
import java.sql.*;

/**
 *
 * This is a trip generation implementation based on reading attribute weights and their values associated to each
 * zone/node. This model loads a map of weights from an attributes table and loads the respective values of each node
 * and generates an DBPAMap object.
 *
 * @author Rishabh
 */
public class DBTripGenerator extends PrimaryTripGenerator {

    private Connection db;
    private String attributesTable;
    private String productionWeightsTable;
    private String attractionWeightsTable;
    private Graph g;


    /**
     * A Trip Generator which uses the database to perform trip generation
     * @param g Graph associated with object
     * @param db Database Connection
     * @param attributesTable Name of the table where the demographic attributes are stored (node, dem, value)
     * @param prodTable table with the production rates associated with the market segment: (seg, dem, rate)
     * @param attrTable table with the attraction rates associated with the market segment: (seg, dem, rate)
     */
    public DBTripGenerator(Graph g, Connection db, String attributesTable, String prodTable, String attrTable) {
        this.g  = g;
        this.attributesTable = attributesTable;
        this.productionWeightsTable = prodTable;
        this.attractionWeightsTable = attrTable;
        this.db = db;
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
     */
    @Override
    public PAMap generate(MarketSegment marketSegment) {
        PAMap paMap = new DBPAMap(g, db, marketSegment.toString() ,marketSegment.getPAMapTable(), marketSegment.getVOT());
        String generatePA = "INSERT INTO "+marketSegment.getPAMapTable()+" (node, productions, attractions)\n" +
                "SELECT node, sum(p.rate * dp.value), sum(a.rate * dp.value)\n" +
                "  FROM ((SELECT * from "+attributesTable+") as dp INNER JOIN (SELECT * FROM "+productionWeightsTable+" WHERE seg=?) as p ON\n" +
                "  dp.dem = p.dem INNER JOIN (SELECT * FROM "+attractionWeightsTable+" WHERE seg=?) as a ON dp.dem=a.dem) group by node;\n";
        try (PreparedStatement ps = db.prepareStatement(generatePA)) {
            ps.setString(1, marketSegment.toString());
            ps.setString(2, marketSegment.toString());
            ps.executeUpdate();
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
        return paMap;
    }

    /**
     * @return instance of the Graph
     */
    public Graph getGraph() { return g; }
}
