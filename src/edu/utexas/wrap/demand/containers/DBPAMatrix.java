package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public class DBPAMatrix implements AggregatePAMatrix {

	private Graph g;
	private Connection db;
	private float vot;
	private String tableName;

	public DBPAMatrix(Graph g, Connection db, String table, float vot) {
		this.g = g;
		tableName = table;
		this.db = db;
		this.vot = vot;
		String createQuery = "CREATE TABLE IF NOT EXISTS  " + tableName +
				" (origin integer, " +
				"destination integer, " +
				"demand real, " +
				"UNIQUE (origin, destination))";
		try(PreparedStatement ps = db.prepareStatement(createQuery)) {
			ps.executeUpdate();
		} catch (SQLException s) {
			s.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public Graph getGraph() {
		return g;
	}

	@Override
	public void put(TravelSurveyZone prod, TravelSurveyZone attr, Float demand) {
		String weightsQuery = "INSERT INTO "+tableName+" (origin, destination, demand) " +
				"VALUES (?,?,?) " +
				"ON CONFLICT (origin, destination) " +
				"DO UPDATE " +
				"SET demand = ? " +
				"WHERE " +
				tableName+".producer = ? " +
				"AND "+tableName+".attractor = ?";
		try(PreparedStatement ps = db.prepareStatement(weightsQuery)) {
			ps.setInt(1, prod.getNode().getID());
			ps.setInt(2, attr.getNode().getID());
			ps.setFloat(3, demand);
			ps.setFloat(4,demand);
			ps.setInt(5, prod.getNode().getID());
			ps.setInt(6, attr.getNode().getID());
			ps.executeUpdate();
		} catch (SQLException s) {
			s.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public Float getDemand(TravelSurveyZone prod, TravelSurveyZone attr) {
		Float output = 0.0f;
		String weightsQuery = "SELECT demand FROM "+tableName+" WHERE origin=? AND destination=?";
		try(PreparedStatement ps = db.prepareStatement(weightsQuery)) {
			ps.setInt(1, prod.getNode().getID());
			ps.setInt(2, attr.getNode().getID());
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

	public String getTableName(){return tableName;}

	@Override
	public float getVOT() {
		return vot;
	}

	public void writeToDB(AggregatePAHashMatrix aggPAMtx) {
		for(TravelSurveyZone p : aggPAMtx.getProducers()) {
			DemandMap dem = aggPAMtx.getDemandMap(p);
			for(TravelSurveyZone a: dem.getZones()) {
				this.put(p, a, dem.get(p));
			}
		}
	}

	@Override
	public void toFile(File out) {
		// TODO Auto-generated method stub
		throw new RuntimeException("No toFile implementation");
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {
		// TODO Auto-generated method stub
		throw new RuntimeException("No getDemandMap implementation");
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		// TODO Auto-generated method stub
		throw new RuntimeException("No getProducers implementation");
	}
}
