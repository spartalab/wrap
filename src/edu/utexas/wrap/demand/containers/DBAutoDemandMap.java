package edu.utexas.wrap.demand.containers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TravelSurveyZone;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

public class DBAutoDemandMap implements AutoDemandMap {
	private final String tableName;
	private final String columnName;
	private final Mode mode;
	private final float vot;
	private final int origID;
	private final Graph g;
	private String url;
	private Properties props;
	
	public DBAutoDemandMap(String table, String column, Graph g, Integer orig, Float vot, Mode m, String url, Properties props) {
		tableName = table;
		columnName = column;
		this.g = g;
		origID = orig;
		mode = m;
		this.vot = vot;
		this.url = url;
		this.props = props;
	}

	@Override
	public Float get(TravelSurveyZone dest) {
		PreparedStatement ps = null; ResultSet rs = null; Float r = 0.0F; Connection conn = null;
		String query = "Select "+columnName+" from "+tableName+" where orig=? and dest=?";
		try {
			conn = DriverManager.getConnection(url, props);
			ps = conn.prepareStatement(query);
			ps.setInt(1, origID);
			ps.setInt(2, dest.getNode().getID());
			rs = ps.executeQuery();
			r = rs.next()? rs.getFloat(columnName) : 0.0F;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		} finally {
			try {
				if (ps != null) ps.close();
				if (rs != null) rs.close();
				if (conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return r;
	}

	@Override
	public Graph getGraph() {
		return g;
	}

	@Override
	public Collection<TravelSurveyZone> getZones() {
		return g.getTSZs(); //FIXME this is a kludge to speed things up - assumes all nodes have demand
//		Connection conn = null;
//		PreparedStatement ps = null;
//		ResultSet rs = null;
//		Set<Node> ret = new HashSet<Node>();
//		String query = "select dest from "+tableName+" where orig=?";
//		try  {
//			conn = DriverManager.getConnection(url, props);
//			ps = conn.prepareStatement(query);
//			ps.setInt(1, origID);
//			rs = ps.executeQuery();
//			while (rs.next()) {
//				int id = rs.getInt("dest");
//				ret.add(g.getNode(id));
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if (ps != null) ps.close();
//				if (rs != null) rs.close();
//				if (conn != null) conn.close();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//		}
//		return ret;
	}

	@Override
	public Float getOrDefault(TravelSurveyZone node, float f) {
		PreparedStatement ps = null; ResultSet rs = null; Connection conn = null; Float r;
		String query = "Select "+columnName+" from "+tableName+" where orig=? and dest=?";
		try {
			conn = DriverManager.getConnection(url, props);
			ps = conn.prepareStatement(query);
			ps.setInt(1, origID);
			ps.setInt(2, node.getNode().getID());
			rs = ps.executeQuery();
			r = rs.next()? rs.getFloat(columnName) : f;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		} finally {
			try {
				if (ps != null) ps.close();
				if (rs != null) rs.close();
				if (conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return r;
	}

	@Override
	public Float put(TravelSurveyZone dest, Float demand) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public boolean isEmpty() {
		return false; //FIXME this is a kludge to speed things up
//		PreparedStatement ps = null;
//		Connection conn = null;
//		ResultSet rs = null;
//		Boolean r = true;
//		String query = "Select count("+columnName+") from "+tableName+" where orig=?";
//		try {
//			conn = DriverManager.getConnection(url, props);
//			ps = conn.prepareStatement(query);
//			ps.setInt(1, origID);
//			rs = ps.executeQuery();
//			if (rs.next()) {
//				r = rs.getInt("count") <= 0;
//				rs.close();
//				ps.close();
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally {
//			try {
//				if (ps != null) ps.close();
//				if (rs != null) rs.close();
//				if (conn != null) conn.close();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//		}
//		return r;
	}

	@Override
	public Map<TravelSurveyZone, Double> doubleClone() {
		PreparedStatement ps = null; ResultSet rs = null; Connection conn = null;
		Map<TravelSurveyZone, Double> ret = new Object2DoubleOpenHashMap<TravelSurveyZone>();
		String query = "select dest, "+columnName+" from "+tableName+" where orig=?";
		
		try {
			conn = DriverManager.getConnection(url, props);
			ps = conn.prepareStatement(query);
			ps.setInt(1, origID);
			rs = ps.executeQuery();
			while (rs.next()) {
				Node dest = g.getNode(rs.getInt("dest"));
				Double demand = rs.getDouble(columnName);
				ret.put(dest.getZone(), demand);
			}
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		} finally {
			try {
				if (ps != null) ps.close();
				if (rs != null) rs.close();
				if (conn != null) conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
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
