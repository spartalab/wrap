package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBPAMatrix implements AggregatePAMatrix {

    private Graph g;
    private Connection dbConnection;
    private float vot;
    private String tableName;

    public DBPAMatrix(Graph g, String table, Connection db, float vot) {
        this.g = g;
        tableName = table;
        dbConnection = db;
        this.vot = vot;
    }

    @Override
    public Graph getGraph() {
        return g;
    }

    @Override
    public void put(Node origin, Node destination, Float demand) {
        String weightsQuery = "INSERT INTO ? (origin, destination, demand) " +
                "VALUES (?,?,?) " +
                "ON CONFLICT (origin, destination) " +
                "DO UPDATE " +
                "SET demand = ? " +
                "WHERE " +
                "?.producer = ? " +
                "AND ?.attractor = ?";
        try(PreparedStatement ps = dbConnection.prepareStatement(weightsQuery)) {
            ps.setString(1, tableName);
            ps.setInt(2, origin.getID());
            ps.setInt(3, destination.getID());
            ps.setFloat(4, demand);
            ps.setFloat(5,demand);
            ps.setString(6, tableName);
            ps.setInt(7, origin.getID());
            ps.setString(8, tableName);
            ps.setInt(9, destination.getID());
            ps.executeUpdate();
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public Float getDemand(Node origin, Node destination) {
        Float output = 0.0f;
        String weightsQuery = "SELECT demand FROM ? WHERE origin=? AND destination=?";
        try(PreparedStatement ps = dbConnection.prepareStatement(weightsQuery)) {
            ps.setString(1, tableName);
            ps.setInt(2, origin.getID());
            ps.setInt(3, destination.getID());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                output = rs.getFloat("demand");
            }
        } catch (SQLException s) {
            s.printStackTrace();
            System.exit(1);
        }
        return output;
    }

    @Override
    public float getVOT() {
        return vot;
    }
}
