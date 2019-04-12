package edu.utexas.wrap.generation;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.AggregatePAMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * This is a trip generation implementation based on reading attribute weights and their values associated to each
 * zone/node. This model loads a map of weights from an attributes table and loads the respective values of each node
 * and generates an AggregatePAMap object.
 *
 * @author Rishabh
 */
public class DBTripGenerator extends TripGenerator {

    private Connection databaseCon;
    private String attributesTable;
    private String productionWeightsTable;
    private String attractionWeightsTable;
    private PAMap paMap;
    private Graph g;
    private Map<String, Float> productionWeights;
    private Map<String, Float> attractionWeights;

    /**
     * Function to load the database by reading the database properties file
     */
    private void loadDatabase() {
        Properties p = new Properties();
        try {
            p.load(DBTripGenerator.class.getResourceAsStream("dbConfig.properties"));
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
     * Function that reads from the weights table and caches a map of the attribute string to its weight to be used
     * when computing the total productions
     */
    private void developProductionWeights() {
        productionWeights = new HashMap<>();
        String weightsQuery = "SELECT * FROM " + productionWeightsTable;
        try(PreparedStatement ps = databaseCon.prepareStatement(weightsQuery)) {
            databaseCon.setAutoCommit(false);
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            while(rs.next()) {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    String attribute = md.getColumnName(i);
                    Float weight = rs.getFloat(attribute);
                    productionWeights.put(attribute, weight);
                }
            }
            databaseCon.setAutoCommit(true);
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Function that reads from the weights table and caches a map of the attribute string to its weight to be used
     * when computing the total attractions
     */
    private void developAttractionWeights() {
        attractionWeights = new HashMap<>();
        String weightsQuery = "SELECT * FROM " + attractionWeightsTable;
        try(PreparedStatement ps = databaseCon.prepareStatement(weightsQuery)) {
            databaseCon.setAutoCommit(false);
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            while(rs.next()) {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    String attribute = md.getColumnName(i);
                    Float weight = rs.getFloat(attribute);
                    attractionWeights.put(attribute, weight);
                }
            }
            databaseCon.setAutoCommit(true);
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
    }
    /**
     * Compute dot product between the actual attribute values for a specified node and the production/attraction weights
     * @param z Specified node for PA map
     * @param values map of attribute to its value
     */
    private void dotProd(Node z, Map<String,Float> values) {
        float attractions = 0.0f;
        float productions = 0.0f;
        for(String attribute: values.keySet()) {
            Float attr = values.get(attribute);
            attractions += attr * attractionWeights.get(attribute);
            productions += attr * productionWeights.get(attribute);
        }
        paMap.putAttractions(z, attractions);
        paMap.putProductions(z, productions);
    }

    /**
     * Function that reads through the attribute values for each node and creates a map of the attribute to its value
     * Then it asynchronously computes the dot product of the weights and values for the productions and attractions and
     * stores it in the PA Map object
     */
    private void developPAMap() {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        String attributesQuery = "SELECT * FROM " + attributesTable;
        try(PreparedStatement ps = databaseCon.prepareStatement(attributesQuery, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            databaseCon.setAutoCommit(false);
            ps.setFetchSize(100);
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            while(rs.next()) {
                Map<String, Float> values = new HashMap<>();
                Node or = g.getNode(rs.getInt("Origin"));
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    String attribute = md.getColumnName(i);
                    values.put(attribute, rs.getFloat(attribute));
                }
                pool.submit(() -> dotProd(or, values));
            }
            databaseCon.setAutoCommit(true);
        } catch (SQLException s) {
            pool.shutdownNow();
            s.printStackTrace();
            System.exit(1);
        }
        try {
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Constructor for Trip Generation object based on the database
     * @param g Graph that the trip generation is based on
     * @param attributesTable Name of table where each node's attribute values are stored
     * @param prodTable Name of table where the production attribute weights are stored
     * @param attrTable Name of table where the attraction attribute weights are stored
     */
    public DBTripGenerator(Graph g, String attributesTable, String prodTable, String attrTable) {
        this.g  = g;
        this.attributesTable = attributesTable;
        this.productionWeightsTable = prodTable;
        this.attractionWeightsTable = attrTable;
        loadDatabase();
        developProductionWeights();
        developAttractionWeights();
        paMap = new AggregatePAMap(g);
    }

    @Override
    /**
     * Generate the PA Map
     */
    public PAMap generate() {
        developPAMap();
        return paMap;
    }

    /**
     * @return instance of the Graph
     */
    public Graph getGraph() { return g; }
}
