package edu.utexas.wrap.tests.Unit;
import edu.utexas.wrap.balancing.DBTripBalancer;
import edu.utexas.wrap.balancing.TripBalancer;
import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.generation.DBTripGenerator;
import edu.utexas.wrap.generation.TripGenerator;
import edu.utexas.wrap.net.CentroidConnector;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TolledBPRLink;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBMarketSegmentTests {

    private Connection db;
    private Graph g;
    private MarketSegment hbw;
    private PAMap hbwPA;

    private MarketSegment hbnw;
    private PAMap hbnwPA;


    private MarketSegment nhbnw;
    private PAMap nhbnwPA;

    @BeforeClass
    public void init() {
        //Create Graph


        //Create 3 market Segment objects

        //
        TripGenerator tg = new DBTripGenerator(g, db, "demdata", "prodrates", "attrates");
        TripBalancer tb = new DBTripBalancer(db);

        hbw = new MarketSegment(g,tg,tb,null,null,null,true,true,'p','1',true,true,0.0f);
        hbnw = new MarketSegment(g,tg,tb,null,null,null,true,true,'p','1',true,true,0.0f);
        nhbnw = new MarketSegment(g,tg,tb,null,null,null,true,true,'p','1',true,true,0.0f);

    }

    public void setupDB() {
        //Connect to DB
        try {
            Class.forName("org.postgresql.Driver");
            db = DriverManager.getConnection("jdbc:postgresql://localhost:5432/testDB");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Could not find database/table to connect to");
            System.exit(3);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        //Upload Tables from CSV

    }


    @Test
    public void tripGenerationTest() {
        hbwPA = hbw.tripGeneration();
        hbnwPA = hbnw.tripGeneration();
        nhbnwPA = nhbnw.tripGeneration();
    }

    @Test
    public void tripBalanceTest() {
        hbw.tripBalance(hbwPA);
        hbnw.tripBalance(hbnwPA);
        nhbnw.tripBalance(nhbnwPA);
    }

    @Test
    public void tripDistributionTest() {}

    @AfterClass
    public void finish() {
        //Reset testDB
        tearDownDB();
        //Delete files created during tests
    }

    public void tearDownDB() {
        //Delete tables from db
        //Delete all new tables created during testing
    }

}
