package edu.utexas.wrap.distribution;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * Implementation of the Friction Factor Map to store friction factors between each Origin and Destination
 *
 * @author Rishabh
 */
public class FrictionFactorMap {

	private Connection databaseCon;
	private String tableName;
	private Map<Node, Map<Node, Double>> ffmap;
	private double aConst, bConst, cConst;
	private Graph g;

	/**
	 * Function that develops a friction factor map using the specified friction factor table
	 * It reads each Origin-Destination pair and uses the pktime roadway skims
	 * in computing the friction factor.
	 */
	private void developFFMap() {
		//TODO Implement producing a Map from the data using JDBC
		String query = "SELECT * FROM " + tableName;
		try(PreparedStatement ps = databaseCon.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			databaseCon.setAutoCommit(false);
			ps.setFetchSize(100);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				int or = rs.getInt("Origin");
				int dest = rs.getInt("Destination");
				double time = rs.getDouble("pktime");
				double ff = aConst * Math.pow(time, bConst)* Math.exp(-1 * cConst * time);
				Map<Node,Double> ffDest;
				if(ffmap.containsKey(g.getNode(or))) {
					ffDest = ffmap.get(g.getNode(or));
					ffDest.put(g.getNode(dest), ff);
				} else {
					ffDest = new HashMap<Node, Double>();
					ffDest.put(g.getNode(dest), ff);

					ffmap.put(g.getNode(or), ffDest);
				}
			}
			databaseCon.setAutoCommit(true);
		} catch (SQLException s) {
			s.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Default constructor for Friction Factor map where constants a, b, and c are set to 1
	 * @param g Graph that the factors are based on
	 * @param table table where roadway skims are stored
	 */
	public FrictionFactorMap(Graph g, Connection db ,String table) {
		this(g, db ,table, 1, 1, 1);

	}

	/**
	 * Refer to NCTCOG model for definition of the constants
	 * @param g Graph that the factors are based on
	 * @param table table where roadway skims are stored
	 * @param a ffConst A
	 * @param b ffConst B
	 * @param c ff Const C
	 */
	public FrictionFactorMap(Graph g, Connection db ,String table, double a, double b, double c) {
		tableName = table;
		databaseCon = db;
		this.aConst = a;
		this.bConst = b;
		this.cConst = c;
		this.g = g;
		ffmap = new HashMap<Node, Map<Node, Double>>();
		developFFMap();
	}

	/**
	 * Return the friction factor for provided OD
	 * @param i Origin Node
	 * @param z Destination Node
	 * @return computed friction factor
	 */
	public Double get(Node i, Node z) {
		// TODO Auto-generated method stub
		return ffmap.get(i).get(z);
	}

}
