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
    private String fftable;
    private Class<? extends TripDistributor> distributorClass;
    private Set<String> modalMatricies;

    public MarketSegment(Graph g, Connection db, boolean homeBased, boolean workTrip, char peakTrip, char incomeQ, boolean hasVeh, boolean asManyVehAsWork, float vot, Class<? extends TripDistributor> dist, String fftable) {
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
        this.fftable = fftable;
        distributorClass = dist;


        hash = hashCode();
    }

    public void tripGeneration(String demographicsTable, String productionRates, String attractionRates) {
        String createQuery = "CREATE TABLE  ?" +
                " (origin integer, " +
                "productions real, " +
                "attractions real )";
        try(PreparedStatement ps = db.prepareStatement(createQuery)) {
            ps.setString(1, paMapPrefix+table_suffix);
            ps.executeUpdate();
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }

        String generatePA = "INSERT INTO ? (origin, productions, attractions)\n" +
                "SELECT origin, sum(p.rate * dp.value), sum(a.rate * dp.value)\n" +
                "  FROM ((SELECT * from ?) as dp INNER JOIN (SELECT * FROM ? WHERE seg=?) as p ON\n" +
                "  dp.dem = p.dem INNER JOIN (SELECT * FROM ? WHERE seg=?) as a ON dp.dem=a.dem) group by origin;\n";
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

    public void tripDistribution() {
        FrictionFactorMap ff = new FrictionFactorMap(this.g, this.db, fftable);
        try {
            TripDistributor distributor = (TripDistributor) Class.forName(distributorClass.getName()).getConstructor(distributorClass).newInstance(new Object[]{g, ff});
            AggregatePAHashMatrix aggPAMtx = (AggregatePAHashMatrix) distributor.distribute(new DBPAMap(g, paMapPrefix+table_suffix,db, vot));

            String createQuery = "CREATE TABLE  ?" +
                    " (producer integer, " +
                    "attractor integer, " +
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
