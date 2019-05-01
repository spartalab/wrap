package edu.utexas.wrap.demand;

import edu.utexas.wrap.demand.containers.AggregatePAHashMatrix;
import edu.utexas.wrap.demand.containers.DBPAMap;
import edu.utexas.wrap.demand.containers.DBPAMatrix;
import edu.utexas.wrap.demand.containers.DemandHashMap;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class MarketSegment {
    /**
     * Segment Definition
     */
    private boolean homeBased;
    private boolean workTrip;
    private char peakTrip;
    private char incomeQ;
    private boolean hasVeh;
    private boolean asManyVehAsWork;

    private static final String paMapPrefix = "PAMap_";
    private static final String paMtxPrefix = "PAMtx_";


    /**
     * Additional tools
     */
    private Connection db;
    private Graph g;
    private Float vot;
    private String table_suffix;
    private int hash;
    private Set<String> modalMatricies;

    public MarketSegment(Graph g, Connection db, boolean homeBased, boolean workTrip, char peakTrip, char incomeQ, boolean hasVeh, boolean asManyVehAsWork, float vot) {
        if(peakTrip != 'a' && peakTrip != 'p' && peakTrip != 'o')
            throw new IllegalArgumentException("peakTrip must be 'a' or 'p' or 'o' ");
        if(incomeQ != '1' && incomeQ != '2' && incomeQ != '3' && incomeQ != '4')
            throw new IllegalArgumentException("incomeQ must be '1','2','3', or '4'");
        if(!hasVeh && asManyVehAsWork)
            throw new IllegalArgumentException("Cannot have 0 vehicles and have as many vehicles as workers");
        this.homeBased = homeBased;
        this.workTrip = workTrip;
        this.peakTrip = peakTrip;
        this.incomeQ = incomeQ;
        this.hasVeh = hasVeh;
        this.asManyVehAsWork = asManyVehAsWork;

        this.g = g;
        this.db = db;
        this.vot = vot;
        hash = hashCode();
    }

    /**
     * This method generates the PA map using SQL linear algebra.
     *
     *  The generatePA SQL statment uses inner joins to compute a "dot-product" of rates and values to produce the PA Map.
     *
     *  The productions and attractions are calculated by an inner join between the attraction rates table, the production
     *  rates table, and the demographic data table on the demographics of the model. It then multiples the rates to the
     *  respective values in the demographic data table and sums them for each node. This produces the productions and
     *  attractions values that are inserted along with the the node ID into the paMap table.
     * @param demographicsTable table with the demographics values: (node, dem, value)
     * @param productionRates table with the production rates associated with the market segment: (seg, dem, rate)
     * @param attractionRates table with the attraction rates associated with the market segment: (seg, dem, rate)
     */
    public void tripGeneration(String demographicsTable, String productionRates, String attractionRates) {
        String createQuery = "CREATE TABLE  ?" +
                " (node integer, " +
                "productions real, " +
                "attractions real )";
        try(PreparedStatement ps = db.prepareStatement(createQuery)) {
            ps.setString(1, paMapPrefix+table_suffix);
            ps.executeUpdate();
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }

        /*
         *
         */
        String generatePA = "INSERT INTO ? (node, productions, attractions)\n" +
                "SELECT node, sum(p.rate * dp.value), sum(a.rate * dp.value)\n" +
                "  FROM ((SELECT * from ?) as dp INNER JOIN (SELECT * FROM ? WHERE seg=?) as p ON\n" +
                "  dp.dem = p.dem INNER JOIN (SELECT * FROM ? WHERE seg=?) as a ON dp.dem=a.dem) group by node;\n";
        try(PreparedStatement ps = db.prepareStatement(generatePA)) {
            ps.setString(1, paMapPrefix+table_suffix);
            ps.setString(2, demographicsTable);
            ps.setString(3, productionRates);
            ps.setString(4, table_suffix);
            ps.setString(5, attractionRates);
            ps.setString(6, table_suffix);
            ps.executeUpdate();
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * This function runs a basic implementation of trip balancing.
     * It essentially finds the ratio between the productions
     * and the attractions and multiplies each of the productions by this factor to match the the number of attractions
     */
    public void tripBalance() {
        String sumQuery = "SELECT sum(productions) AS prodTotal, sum(attractions) AS attrTotal FROM ?";
        try(PreparedStatement ps = db.prepareStatement(sumQuery)) {
            ps.setString(1, getPAMapTable());
            ResultSet output = ps.executeQuery();
            float prodTotal = output.getFloat("prodTotal");
            float attrTotal = output.getFloat("attrTotal");
            float factor = attrTotal/prodTotal;
            String factorQuery = "UPDATE ? SET productions = productions * ?";
            PreparedStatement fq = db.prepareStatement(factorQuery);
            fq.setString(1, getPAMapTable());
            fq.setFloat(2, factor);
            fq.executeUpdate();
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * This function performs Trip Distribution using the specified tripDistributor model and a friction factors table
     * The tripDistributor model creates an in-memory PAHasMatrix which is then written back into a paMtx table in the
     * database
     * @param dist Trip Distribution Model being used
     * @param fftable a friction factors table with peak roadway skims as the friction factor: (origin, destination, pktime)
     */
    public void tripDistribution(Class<? extends TripDistributor> dist, String fftable) {
        FrictionFactorMap ff = new FrictionFactorMap(this.g, this.db, fftable);
        try {
            TripDistributor distributor = (TripDistributor) Class.forName(dist.getName()).getConstructor(dist).newInstance(new Object[]{g, ff});
            AggregatePAHashMatrix aggPAMtx = (AggregatePAHashMatrix) distributor.distribute(new DBPAMap(g, paMapPrefix+table_suffix,db, vot));

            String createQuery = "CREATE TABLE  ?" +
                    " (origin integer, " +
                    "destination integer, " +
                    "demand real)";
            try(PreparedStatement ps = db.prepareStatement(createQuery)) {
                ps.setString(1, paMtxPrefix+table_suffix);
                ps.executeUpdate();
            } catch (SQLException s) {
                s.printStackTrace();
                System.exit(1);
            }

            DBPAMatrix dbpaMatrix = new DBPAMatrix(g, paMtxPrefix+table_suffix, db, vot);
            for(Node p:aggPAMtx.keySet()) {
                DemandHashMap dem = aggPAMtx.get(p);
                for(Node a: dem.getNodes()) {
                    dbpaMatrix.put(p, a, dem.get(p));
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void modeChoice() {

    }

    public void routeChoice() {

    }
    @Override
    public int hashCode() {
        if(hash != 0) {
            return hash;
        } else {
            return toString().hashCode();
        }
    }

    @Override
    public String toString() {
        if(table_suffix != null) {
            return table_suffix;
        } else {
            table_suffix = String.valueOf(homeBased ? 1 : 0) +
                    (workTrip ? 1 : 0) +
                    peakTrip +
                    incomeQ +
                    (hasVeh ? 1 : 0) +
                    (asManyVehAsWork ? 1 : 0);
            return table_suffix;
        }
    }

    public boolean equals(MarketSegment other) {
        return this.hash == other.hash;
    }

    public String getPAMapTable() {
        return paMapPrefix+table_suffix;
    }

    public String getPAMtxTable() {
        return paMtxPrefix + table_suffix;
    }
}
