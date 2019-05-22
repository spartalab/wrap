package edu.utexas.wrap.demand.containers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.assignment.Origin;
import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class DBAutoDemandMap implements AutoDemandMap {
	private final String tableName;
	private final String columnName;
	private final Mode mode;
	private final Float vot;
	private final Integer origID;
	private final Graph g;
	private Connection conn;
	
	public DBAutoDemandMap(String table, String column, Graph g, Origin orig, Float vot, Mode m, Connection db) {
		tableName = table;
		columnName = column;
		this.g = g;
		origID = orig.getNode().getID();
		mode = m;
		this.vot = vot;
		conn = db;
		String createQuery = "create table if not exists"+tableName+" (orig integer, dest integer, ? double precision)";
		try (PreparedStatement ps = db.prepareStatement(createQuery)){
			ps.setString(1, column);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public Float get(Node dest) {
		String query = "Select ? from "+tableName+" where orig=? and dest=?";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, columnName);
			ps.setInt(2, origID);
			ps.setInt(3, dest.getID());
			ResultSet rs = ps.executeQuery();
			return rs.next()? rs.getFloat(columnName) : null;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public Graph getGraph() {
		return g;
	}

	@Override
	public Collection<Node> getNodes() {
		Set<Node> ret = new HashSet<Node>();
		String query = "select dest from "+tableName+" where orig=?";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setInt(1, origID);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("dest");
				ret.add(g.getNode(id));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public Float getOrDefault(Node node, float f) {
		String query = "Select ? from "+tableName+" where orig=? and dest=?";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, columnName);
			ps.setInt(2, origID);
			ps.setInt(3, node.getID());
			ResultSet rs = ps.executeQuery();
			return rs.next()? rs.getFloat(columnName) : f;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public Float put(Node dest, Float demand) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public boolean isEmpty() {
		String query = "Select count(?) from "+tableName+" where orig=?";
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, columnName);
			ps.setInt(2, origID);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt("count") <= 0;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new RuntimeException();

	}

	@Override
	public Map<Node, Double> doubleClone() {
		Map<Node, Double> ret = new HashMap<Node,Double>();
		String query = "select dest, ? from "+tableName+" where orig=?";
		
		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, columnName);
			ps.setInt(2, origID);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Node dest = g.getNode(rs.getInt("dest"));
				Double demand = rs.getDouble(columnName);
				ret.put(dest, demand);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		}
		
		
		return ret;
	}

	@Override
	public Float getVOT() {
		return vot;
	}

	@Override
	public Mode getMode() {
		return mode;
	}

}
