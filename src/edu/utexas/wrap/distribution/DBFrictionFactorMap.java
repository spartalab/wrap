package edu.utexas.wrap.distribution;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Implementation of the Friction Factor Map to store friction factors between each Origin and Destination
 *
 * @author Rishabh
 */
public class DBFrictionFactorMap implements FrictionFactorMap {

	private Connection databaseCon;
	private String tableName;
	private Map<TravelSurveyZone, Map<TravelSurveyZone, Double>> ffmap;
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
				int or = rs.getInt("origin");
				int dest = rs.getInt("destination");
				double time = rs.getDouble("pktime");
				double ff = aConst * Math.pow(time, bConst)* Math.exp(-1 * cConst * time);
				Map<TravelSurveyZone,Double> ffDest;
				if(ffmap.containsKey(g.getNode(or).getZone())) {
					ffDest = ffmap.get(g.getNode(or).getZone());
					ffDest.put(g.getNode(dest).getZone(), ff);
				} else {
					ffDest = new HashMap<TravelSurveyZone, Double>();
					ffDest.put(g.getNode(dest).getZone(), ff);

					ffmap.put(g.getNode(or).getZone(), ffDest);
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
	public DBFrictionFactorMap(Graph g, Connection db ,String table) {
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
	public DBFrictionFactorMap(Graph g, Connection db ,String table, double a, double b, double c) {
		tableName = table;
		databaseCon = db;
		this.aConst = a;
		this.bConst = b;
		this.cConst = c;
		this.g = g;
		ffmap = new HashMap<TravelSurveyZone, Map<TravelSurveyZone, Double>>();
		developFFMap();
	}

	/**
	 * Return the friction factor for provided OD
	 * @param i Origin Node
	 * @param z Destination Node
	 * @return computed friction factor
	 */
	public Float get(TravelSurveyZone i, TravelSurveyZone z) {
		// TODO Auto-generated method stub
		return ffmap.get(i).get(z).floatValue();
	}

}
