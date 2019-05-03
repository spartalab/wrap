package edu.utexas.wrap.tests.Unit;
import edu.utexas.wrap.balancing.DBTripBalancer;
import edu.utexas.wrap.balancing.TripBalancer;
import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.DBPAMap;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.generation.DBTripGenerator;
import edu.utexas.wrap.generation.TripGenerator;
import edu.utexas.wrap.net.CentroidConnector;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TolledBPRLink;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBMarketSegmentTests {

    private Connection db;
    private Graph g;
    private MarketSegment hbw;

    private MarketSegment hbnw;


    private MarketSegment nhbnw;

    @BeforeClass
    public void init() {
        //Create Graph


        //Create 3 market Segment objects
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
            db = DriverManager.getConnection("jdbc:postgresql://localhost:5432/wraptestdb");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Could not find database/table to connect to");
            System.exit(3);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    @Test
    public void tripGenerationhbwTest() {
        PAMap hbwPA = hbw.tripGeneration();
        PAMap hbwPAExp = new DBPAMap(g, db, "upamap_11p111",0.0f);
        float[] prodVals = new float[9];
        float[] prodValsExp = new float[9];
        float[] attrVals = new float[9];
        float[] attrValsExp = new float[9];

        for(Node p: hbwPA.getProducers()) {
            prodVals[p.getID()] = hbwPA.getProductions(p);
            prodValsExp[p.getID()] = hbwPAExp.getProductions(p);
            attrVals[p.getID()] = hbwPA.getAttractions(p);
            attrValsExp[p.getID()] = hbwPAExp.getAttractions(p);
        }
        Assert.assertArrayEquals(prodValsExp, prodVals, 0.0001f);
        Assert.assertArrayEquals(attrValsExp,attrVals, 0.0001f);
    }

    @Test
    public void tripGenerationhbnwTest() {
        PAMap hbnwPA = hbnw.tripGeneration();
        PAMap hbnwPAExp = new DBPAMap(g, db, "upamap_10p111",0.0f);
        float[] prodVals = new float[9];
        float[] prodValsExp = new float[9];
        float[] attrVals = new float[9];
        float[] attrValsExp = new float[9];

        for(Node p: hbnwPA.getProducers()) {
            prodVals[p.getID()] = hbnwPA.getProductions(p);
            prodValsExp[p.getID()] = hbnwPAExp.getProductions(p);
            attrVals[p.getID()] = hbnwPA.getAttractions(p);
            attrValsExp[p.getID()] = hbnwPAExp.getAttractions(p);
        }
        Assert.assertArrayEquals(prodValsExp, prodVals, 0.0001f);
        Assert.assertArrayEquals(attrValsExp,attrVals, 0.0001f);
    }

    @Test
    public void tripGenerationnhbnwTest() {
        PAMap nhbnwPA = nhbnw.tripGeneration();
        PAMap nhbnwPAExp = new DBPAMap(g, db, "upamap_00p111",0.0f);
        float[] prodVals = new float[9];
        float[] prodValsExp = new float[9];
        float[] attrVals = new float[9];
        float[] attrValsExp = new float[9];

        for(Node p: nhbnwPA.getProducers()) {
            prodVals[p.getID()] = nhbnwPA.getProductions(p);
            prodValsExp[p.getID()] = nhbnwPAExp.getProductions(p);
            attrVals[p.getID()] = nhbnwPA.getAttractions(p);
            attrValsExp[p.getID()] = nhbnwPAExp.getAttractions(p);
        }
        Assert.assertArrayEquals(prodValsExp, prodVals, 0.0001f);
        Assert.assertArrayEquals(attrValsExp,attrVals, 0.0001f);
    }

    @Test
    public void tripBalancehbwTest() {
        String createDup = "CREATE TABLE tpamap_11p111 AS SELECT * FROM upamap_11p111";
        try (Statement st = db.createStatement()) {
            st.execute(createDup);
        }catch (SQLException e){
            e.printStackTrace();
            System.exit(1);
        }
        PAMap balancedHBWPA = new DBPAMap(g, db, "tpamap_11p111", 0.0f);

        hbw.tripBalance(balancedHBWPA);
        PAMap hbwPAExp = new DBPAMap(g, db, "bpamap_11p111",0.0f);
        float[] prodVals = new float[9];
        float[] prodValsExp = new float[9];
        float[] attrVals = new float[9];
        float[] attrValsExp = new float[9];

        for(Node p: balancedHBWPA.getProducers()) {
            prodVals[p.getID()] = balancedHBWPA.getProductions(p);
            prodValsExp[p.getID()] = hbwPAExp.getProductions(p);
            attrVals[p.getID()] = balancedHBWPA.getAttractions(p);
            attrValsExp[p.getID()] = hbwPAExp.getAttractions(p);
        }
        Assert.assertArrayEquals(prodValsExp, prodVals, 0.0001f);
        Assert.assertArrayEquals(attrValsExp,attrVals, 0.0001f);
        String delete = "DROP TABLE tpamap_11p111";
        try (Statement st = db.createStatement()) {
            st.execute(delete);
        }catch (SQLException e){
            e.printStackTrace();
            System.exit(1);
        }

    }

    @Test
    public void tripBalancehbnwTest() {
        String createDup = "CREATE TABLE tpamap_10p111 AS SELECT * FROM upamap_10p111";
        try (Statement st = db.createStatement()) {
            st.execute(createDup);
        }catch (SQLException e){
            e.printStackTrace();
            System.exit(1);
        }
        PAMap balancedHBNWPA = new DBPAMap(g, db, "tpamap_10p111", 0.0f);

        hbnw.tripBalance(balancedHBNWPA);
        PAMap hbnwPAExp = new DBPAMap(g, db, "bpamap_10p111",0.0f);
        float[] prodVals = new float[9];
        float[] prodValsExp = new float[9];
        float[] attrVals = new float[9];
        float[] attrValsExp = new float[9];

        for(Node p: balancedHBNWPA.getProducers()) {
            prodVals[p.getID()] = balancedHBNWPA.getProductions(p);
            prodValsExp[p.getID()] = hbnwPAExp.getProductions(p);
            attrVals[p.getID()] = balancedHBNWPA.getAttractions(p);
            attrValsExp[p.getID()] = hbnwPAExp.getAttractions(p);
        }
        Assert.assertArrayEquals(prodValsExp, prodVals, 0.0001f);
        Assert.assertArrayEquals(attrValsExp,attrVals, 0.0001f);
        String delete = "DROP TABLE tpamap_10p111";
        try (Statement st = db.createStatement()) {
            st.execute(delete);
        }catch (SQLException e){
            e.printStackTrace();
            System.exit(1);
        }

    }

    @Test
    public void tripBalancenhbnwTest() {
        String createDup = "CREATE TABLE tpamap_00p111 AS SELECT * FROM upamap_00p111";
        try (Statement st = db.createStatement()) {
            st.execute(createDup);
        }catch (SQLException e){
            e.printStackTrace();
            System.exit(1);
        }
        PAMap balancedNHBNWPA = new DBPAMap(g, db, "tpamap_00p111", 0.0f);

        hbw.tripBalance(balancedNHBNWPA);
        PAMap nhbnwPAExp = new DBPAMap(g, db, "bpamap_00p111",0.0f);
        float[] prodVals = new float[9];
        float[] prodValsExp = new float[9];
        float[] attrVals = new float[9];
        float[] attrValsExp = new float[9];

        for(Node p: balancedNHBNWPA.getProducers()) {
            prodVals[p.getID()] = balancedNHBNWPA.getProductions(p);
            prodValsExp[p.getID()] = nhbnwPAExp.getProductions(p);
            attrVals[p.getID()] = balancedNHBNWPA.getAttractions(p);
            attrValsExp[p.getID()] = nhbnwPAExp.getAttractions(p);
        }
        Assert.assertArrayEquals(prodValsExp, prodVals, 0.0001f);
        Assert.assertArrayEquals(attrValsExp,attrVals, 0.0001f);
        String delete = "DROP TABLE tpamap_00p111";
        try (Statement st = db.createStatement()) {
            st.execute(delete);
        }catch (SQLException e){
            e.printStackTrace();
            System.exit(1);
        }

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
