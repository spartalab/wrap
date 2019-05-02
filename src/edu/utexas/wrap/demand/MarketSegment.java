package edu.utexas.wrap.demand;

import edu.utexas.wrap.assignment.AssignmentLoader;
import edu.utexas.wrap.balancing.TripBalancer;
import edu.utexas.wrap.demand.containers.AggregatePAHashMatrix;
import edu.utexas.wrap.demand.containers.DBPAMap;
import edu.utexas.wrap.demand.containers.DBPAMatrix;
import edu.utexas.wrap.demand.containers.DemandHashMap;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.generation.TripGenerator;
import edu.utexas.wrap.modechoice.TripInterchangeSplitter;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class MarketSegment implements Runnable{
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
    private String table_suffix;

    /**
     * Additional tools
     */
    private Graph g;
    private Float vot;
    private int hash;
    private TripGenerator tg;
    private TripBalancer tb;
    private TripDistributor td;
    private TripInterchangeSplitter mc;
    private AssignmentLoader rc;

    public MarketSegment(Graph g, TripGenerator tripGen, TripBalancer tb, TripDistributor tripDist, TripInterchangeSplitter modeChoice, AssignmentLoader routeChoice, boolean homeBased, boolean workTrip, char peakTrip, char incomeQ, boolean hasVeh, boolean asManyVehAsWork, float vot) {
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
        this.tg = tripGen;
        this.tb = tb;
        this.td = tripDist;
        this.mc = modeChoice;
        this.rc = routeChoice;
        this.vot = vot;
        hash = hashCode();
    }

    public PAMap tripGeneration() {
        return tg.generate();
    }

    /**
     * This function runs a basic implementation of trip balancing.
     * It essentially finds the ratio between the productions
     * and the attractions and multiplies each of the productions by this factor to match the the number of attractions
     */
    public void tripBalance(PAMap paMap) {
        tb.balance(paMap);
    }

    /**
     * This function performs Trip Distribution using the specified tripDistributor model and a friction factors table
     * The tripDistributor model creates an in-memory PAHasMatrix which is then written back into a paMtx table in the
     * database
     */
    public PAMatrix tripDistribution(PAMap paMap) {
        return td.distribute(paMap);
    }

    public Set<String> modeChoice(PAMatrix paMatrix) {
        //TODO: Implement DB TripInterchangeSplitter
        return new HashSet<String>();
    }

    private void routeChoice() {

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
    @Override
    public void run() {
        PAMap paMap = tripGeneration();
        tripBalance(paMap);
        PAMatrix paMatrix = tripDistribution(paMap);
        Set<String> modalPAMatrices = modeChoice(paMatrix);

    }

    public boolean equals(MarketSegment other) {
        return this.hashCode() == other.hashCode();
    }

    public String getPAMapTable() {
        return paMapPrefix + table_suffix;
    }

    public String getPAMtxTable() {
        return paMtxPrefix + table_suffix;
    }
}
