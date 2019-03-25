package edu.utexas.wrap.distribution;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FrictionFactorMap {

	private final static String dbName = "sta";
	private static Properties p;
	private Connection databaseCon;
	private String tableName;
	private Map<Node, Map<Node, Double>> ffmap;
	private double aConst, bConst, cConst;
	private Graph g;

	private void loadDatabase() {
		try {
			Class.forName("org.postgresql.Driver");
			databaseCon = DriverManager.getConnection("jdbc:postgresql://db1.wrangler.tacc.utexas.edu/" + dbName, p.getProperty("user"), p.getProperty("pass"));
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Could not find database/table to connect to");
			System.exit(1);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}


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

	public FrictionFactorMap(Graph g, String table) {
		this(g, table, 1, 1, 1);

	}
	public FrictionFactorMap(Graph g, String table, double a, double b, double c) {
		tableName = table;
		p = new Properties();
		try {
			p.load(FrictionFactorMap.class.getResourceAsStream("dbConfig.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		loadDatabase();
		this.aConst = a;
		this.bConst = b;
		this.cConst = c;
		this.g = g;
		ffmap = new HashMap<Node, Map<Node, Double>>();
		developFFMap();

	}

	public Double get(Node i, Node z) {
		// TODO Auto-generated method stub
		return ffmap.get(i).get(z);
	}

}
