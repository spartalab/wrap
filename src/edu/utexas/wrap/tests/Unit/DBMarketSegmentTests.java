package edu.utexas.wrap.tests.Unit;
import edu.utexas.wrap.balancing.DBTripBalancer;
import edu.utexas.wrap.balancing.TripBalancer;
import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.DBPAMap;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.generation.DBTripGenerator;
import edu.utexas.wrap.generation.TripGenerator;
import edu.utexas.wrap.net.*;
import org.junit.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runners.MethodSorters;

import java.sql.*;
import java.util.*;

public class DBMarketSegmentTests {

    private static Connection db;
    private static Graph g;
    private static MarketSegment hbw;
    private static MarketSegment hbnw;
    private static MarketSegment nhbnw;

    @BeforeClass
    public static void init() {
        //Connect to DB
        setupDB();

        //Create Graph
        createGraph();


        //Create 3 market Segment objects
        TripGenerator tg = new DBTripGenerator(g, db, "demdata", "prodrates", "attrrates");
        TripBalancer tb = new DBTripBalancer(db);

        hbw = new MarketSegment(g,tg,tb,null,null,null,true,true,'p','1',true,true,0.0f);
        hbnw = new MarketSegment(g,tg,tb,null,null,null,true,false,'p','1',true,true,0.0f);
        nhbnw = new MarketSegment(g,tg,tb,null,null,null,false,false,'p','1',true,true,0.0f);

    }

    private static void setupDB() {
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
        tearDownDB();
    }

    @BeforeEach
    private static void removeTables() {
        deleteAllTables();
    }

    private static void createGraph() {
        g = new Graph();

        Node a = new Node(1,false);
        Node b = new Node(2,false);
        Node c = new Node(3,false);
        Node d = new Node(4,false);
        Node e = new Node(5,false);
        Node f = new Node(6,false);
        Node gl = new Node(7,false);
        Node h = new Node(8,false);
        Node i = new Node(9,false);
        Node j = new Node(10,false);
        Node k = new Node(11,false);
        Node l = new Node(12,false);
        Node m = new Node(13,false);
        Node n = new Node(14,false);
        Node o = new Node(15,false);
        Node p = new Node(16,false);

        List<Link> allLinks = new ArrayList<>();
        allLinks.add(new TolledBPRLink(a,i, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,0.0f) {
            @Override
            public Double getTravelTime() { return 15.0;}
        });
        allLinks.add(new TolledBPRLink(i,a, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,0.0f) {
            @Override
            public Double getTravelTime() { return 18.0;}
        });
        allLinks.add(new TolledBPRLink(b,j, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,0.0f) {
            @Override
            public Double getTravelTime() { return 22.0;}
        });
        allLinks.add(new TolledBPRLink(j,b, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,0.0f) {
            @Override
            public Double getTravelTime() { return 10.0;}
        });
        allLinks.add(new TolledBPRLink(c,k, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,0.0f) {
            @Override
            public Double getTravelTime() { return 11.0;}
        });
        allLinks.add(new TolledBPRLink(k,c, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,0.0f) {
            @Override
            public Double getTravelTime() { return 19.0;}
        });
        allLinks.add(new TolledBPRLink(d,l, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,0.0f) {
            @Override
            public Double getTravelTime() { return 21.0;}
        });
        allLinks.add(new TolledBPRLink(l,d, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,0.0f) {
            @Override
            public Double getTravelTime() { return 21.0;}
        });
        allLinks.add(new TolledBPRLink(e,m, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,0.0f) {
            @Override
            public Double getTravelTime() { return 19.0;}
        });
        allLinks.add(new TolledBPRLink(m,e, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,0.0f) {
            @Override
            public Double getTravelTime() { return 7.0;}
        });
        allLinks.add(new TolledBPRLink(f,n, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,0.0f) {
            @Override
            public Double getTravelTime() { return 29.0;}
        });
        allLinks.add(new TolledBPRLink(n,f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,0.0f) {
            @Override
            public Double getTravelTime() { return 15.0;}
        });
        allLinks.add(new TolledBPRLink(o,gl, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,0.0f) {
            @Override
            public Double getTravelTime() { return 18.0;}
        });
        allLinks.add(new TolledBPRLink(gl,o, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,0.0f) {
            @Override
            public Double getTravelTime() { return 5.0;}
        });
        allLinks.add(new TolledBPRLink(h,p, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,0.0f) {
            @Override
            public Double getTravelTime() { return 6.0;}
        });
        allLinks.add(new TolledBPRLink(p,h, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,0.0f) {
            @Override
            public Double getTravelTime() { return 9.0;}
        });
        g.addAll(allLinks);

    }

    @Test
    public void toStringhbwTest(){
        Assert.assertEquals("11p111", hbw.toString());
    }

    @Test
    public void toStringhbnwTest(){
        Assert.assertEquals("10p111", hbnw.toString());
    }

    @Test
    public void toStringnhbnwTest(){
        Assert.assertEquals("00p111", nhbnw.toString());
    }

    @Test
    public void getPAMapTablehbwTest(){
        Assert.assertEquals("pamap_11p111", hbw.getPAMapTable());
    }

    @Test
    public void getPAMapTablehbnwTest(){
        Assert.assertEquals("pamap_10p111", hbnw.getPAMapTable());
    }

    @Test
    public void getPAMapTablenhbnwTest(){
        Assert.assertEquals("pamap_00p111", nhbnw.getPAMapTable());
    }

    @Test
    public void getPAMtxTablehbwTest(){
        Assert.assertEquals("pamtx_11p111", hbw.getPAMtxTable());
    }

    @Test
    public void getPAMtxTablehbnwTest(){
        Assert.assertEquals("pamtx_10p111", hbnw.getPAMtxTable());
    }

    @Test
    public void getPAMtxTablenhbnwTest(){
        Assert.assertEquals("pamtx_00p111", nhbnw.getPAMtxTable());
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
        Assert.assertArrayEquals(prodValsExp, prodVals, 0.01f);
        Assert.assertArrayEquals(attrValsExp, attrVals, 0.01f);
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
        Assert.assertArrayEquals(prodValsExp, prodVals, 0.01f);
        Assert.assertArrayEquals(attrValsExp,attrVals, 0.01f);
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
        Assert.assertArrayEquals(prodValsExp, prodVals, 0.01f);
        Assert.assertArrayEquals(attrValsExp,attrVals, 0.01f);
    }

    @Test
    public void tripBalancehbwTest() {
        String createDup = "CREATE TABLE IF NOT EXISTS tpamap_11p111 AS SELECT * FROM upamap_11p111";
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
        Assert.assertArrayEquals(prodValsExp, prodVals, 0.01f);
        Assert.assertArrayEquals(attrValsExp,attrVals, 0.01f);
    }

    @Test
    public void tripBalancehbnwTest() {
        String createDup = "CREATE TABLE IF NOT EXISTS tpamap_10p111 AS SELECT * FROM upamap_10p111";
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
        Assert.assertArrayEquals(prodValsExp, prodVals, 0.01f);
        Assert.assertArrayEquals(attrValsExp,attrVals, 0.01f);
    }

    @Test
    public void tripBalancenhbnwTest() {
        String createDup = "CREATE TABLE IF NOT EXISTS tpamap_00p111 AS SELECT * FROM upamap_00p111";
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
        Assert.assertArrayEquals(prodValsExp, prodVals, 0.01f);
        Assert.assertArrayEquals(attrValsExp,attrVals, 0.01f);

    }
//    @Test
//    public void tripDistributionTest() {}

    @AfterClass
    public static void finish() {
        //Reset testDB
        tearDownDB();
    }

    public static void tearDownDB() {
        deleteAllTables();
    }

    @AfterEach
    public static void deleteAllTables() {
        //Delete tables from db
        //Delete all new tables created during testing
        ArrayList<String> tables = new ArrayList<String>();
        tables.add("pamap_11p111");
        tables.add("pamap_10p111");
        tables.add("pamap_00p111");
        tables.add("tpamap_11p111");
        tables.add("tpamap_10p111");
        tables.add("tpamap_00p111");
        for(String s: tables) {
            String delete = "DROP TABLE IF EXISTS " + s;
            try (PreparedStatement ps = db.prepareStatement(delete)) {
                ps.executeUpdate();
            } catch (SQLException ignored) { }
        }
    }

}
