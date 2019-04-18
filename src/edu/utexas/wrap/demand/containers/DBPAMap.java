package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class DBPAMap implements PAMap {

    private Connection databaseCon;
    private String tableName;
    private Graph g;
    private float vot;

    /**
     * See PA Map
     */
    public DBPAMap(Graph g, String table, Connection db, float vot) {
        databaseCon = db;
        this.g = g;
        tableName = table;
        this.vot = vot;
    }

    @Override
    public Set<Node> getProducers () {
        Set<Node> output = new HashSet<Node>();
        String weightsQuery = "SELECT productions FROM ? WHERE productions > 0";
        try(PreparedStatement ps = databaseCon.prepareStatement(weightsQuery)) {
            ps.setString(1, tableName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                output.add(g.getNode(rs.getInt("origin")));
            }
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
        return output;
    }

    @Override
    public Set<Node> getAttractors () {
        Set<Node> output = new HashSet<Node>();
        String weightsQuery = "SELECT attractions FROM ? WHERE attractions > 0";
        try(PreparedStatement ps = databaseCon.prepareStatement(weightsQuery)) {
            ps.setString(1, tableName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                output.add(g.getNode(rs.getInt("origin")));
            }
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
        return output;
    }

    @Override
    public Float getAttractions (Node z){
        String weightsQuery = "SELECT attractions FROM ? WHERE origin=? AND attractions > 0";
        try(PreparedStatement ps = databaseCon.prepareStatement(weightsQuery)) {
            ps.setString(1, tableName);
            ps.setInt(2, z.getID());
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
    public Float getProductions (Node z){
        String weightsQuery = "SELECT productions FROM ? WHERE origin=? AND productions > 0";
        try(PreparedStatement ps = databaseCon.prepareStatement(weightsQuery)) {
            ps.setString(1, tableName);
            ps.setInt(2, z.getID());
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
    public void putAttractions(Node z, Float amt) {
        String weightsQuery = "UPDATE ? SET attractions=? WHERE origin=?";
        try(PreparedStatement ps = databaseCon.prepareStatement(weightsQuery)) {
            ps.setString(1, tableName);
            ps.setFloat(2, amt);
            ps.setInt(3, z.getID());
            ResultSet rs = ps.executeQuery();
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void putProductions(Node z, Float amt) {
        String weightsQuery = "UPDATE ? SET productions=? WHERE origin=?";
        try(PreparedStatement ps = databaseCon.prepareStatement(weightsQuery)) {
            ps.setString(1, tableName);
            ps.setFloat(2, amt);
            ps.setInt(3, z.getID());
            ResultSet rs = ps.executeQuery();
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
    }

    public String getTableName() {
        return tableName;
    }

    /**
     * @return the value of time of trips associated with this matrix
     */
    public Float getVOT() {
        return vot;
    }
}
