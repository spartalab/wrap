package edu.utexas.wrap.demand.containers.database;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class DBPAMap implements PAMap {

    private Connection db;
    private String tableName;
    private Graph g;
    private float vot;
    private String marketSeg;

    /**
     * See PA Map
     */
    public DBPAMap(Graph g,Connection db, String marketSeg ,String table,  float vot) {
        this.db = db;
        this.g = g;
        this.marketSeg = marketSeg;
        tableName = table;
        this.vot = vot;
        String createQuery = "CREATE TABLE IF NOT EXISTS " + tableName +
                " (node integer, " +
                "productions real, " +
                "attractions real," +
                " UNIQUE (node) )";
        try(PreparedStatement ps = db.prepareStatement(createQuery)) {
            ps.execute();
        } catch (SQLException s) {
            System.out.println(tableName);
            s.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public Set<TravelSurveyZone> getProducers () {
        Set<TravelSurveyZone> output = new HashSet<TravelSurveyZone>();
        String weightsQuery = "SELECT node FROM "+tableName+" WHERE productions > 0";
        try(PreparedStatement ps = db.prepareStatement(weightsQuery)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                output.add(g.getNode(rs.getInt("node")).getZone());
            }
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
        return output;
    }

    @Override
    public Set<TravelSurveyZone> getAttractors () {
        Set<TravelSurveyZone> output = new HashSet<TravelSurveyZone>();
        String weightsQuery = "SELECT node FROM "+tableName+" WHERE attractions > 0";
        try(PreparedStatement ps = db.prepareStatement(weightsQuery)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                output.add(g.getNode(rs.getInt("node")).getZone());
            }
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
        return output;
    }

    @Override
    public Float getAttractions (TravelSurveyZone z){
        String weightsQuery = "SELECT attractions FROM "+tableName+" WHERE node=? AND attractions > 0";
        try(PreparedStatement ps = db.prepareStatement(weightsQuery)) {
            ps.setInt(1, z.node().getID());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getFloat("attractions");
            }
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
        return 0.0f;
    }

    @Override
    public Float getProductions (TravelSurveyZone z){
        String weightsQuery = "SELECT productions FROM "+tableName+" WHERE node=? AND productions > 0";
        try(PreparedStatement ps = db.prepareStatement(weightsQuery)) {
            ps.setInt(1, z.node().getID());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getFloat("productions");
            }
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
        return 0.0f;
    }
    @Override
    public Graph getGraph() {
        return g;
    }

    @Override
    public void putAttractions(TravelSurveyZone z, Float amt) {
        String weightsQuery = "UPDATE "+tableName+" SET attractions=? WHERE node=?";
        try(PreparedStatement ps = db.prepareStatement(weightsQuery)) {
            ps.setFloat(1, amt);
            ps.setInt(2, z.node().getID());
            ps.executeUpdate();
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void putProductions(TravelSurveyZone z, Float amt) {
        String weightsQuery = "UPDATE "+tableName+" SET productions=? WHERE node=?";
        try(PreparedStatement ps = db.prepareStatement(weightsQuery)) {
            ps.setFloat(1, amt);
            ps.setInt(2, z.node().getID());
            ps.executeUpdate();
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
    }

    public String getTableName() {
        return tableName;
    }

    public String getMarketSeg() {return marketSeg;}

    /**
     * @return the value of time of trips associated with this matrix
     */
    public Float getVOT() {
        return vot;
    }

    public Connection getDB() {return db; }
}
