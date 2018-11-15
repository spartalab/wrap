package edu.utexas.wrap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
/**
 * @author rahulpatel
 *
 */
public abstract class Link implements Priced {

	private static final String dbName = "network";

	private final Float capacity;
	private final Node head;
	private final Node tail;
	private final Float length;
	private final Float fftime;

	private Double cachedFlow = null;
	protected Double cachedTT = null;
	protected Double cachedPrice = null;
	private static Connection databaseCon;
//HEY!
	 private final String createQuery = "CREATE TABLE t" + hashCode() + " (" +
			"bush_origin_id integer, " +
			"vot real, " +
			"vehicle_class char(100), " +
			"flow double precision," +
			"UNIQUE (bush_origin_id, vot, vehicle_class))";
	private final String sumQuery = "SELECT SUM (flow) AS totalFlow FROM t" + hashCode();

	private final String insertQuery = "INSERT INTO t" + hashCode() + " (bush_origin_id, vot, vehicle_class, flow) " +
			"VALUES (" +
			"?,?,?,?)";

	private final String deleteQuery = "DELETE FROM t" + hashCode() +
			" WHERE " +
			"bush_origin_id = ? " +
			"AND vot = ? " +
			"AND vehicle_class = ?";

	private final String selectQuery = "SELECT  * FROM t" + hashCode() +
			" WHERE " +
			"bush_origin_id = ? " +
			"AND vot = ? " +
			"AND vehicle_class = ?";

	private final String dropQuery = "DROP TABLE t" + hashCode();
	static {
		try {
			databaseCon = DriverManager.getConnection("jdbc:derby:" + dbName + ";create=true");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Could not find database/table to connect to");
			System.exit(3);
		}
	}

	public Link(Node tail, Node head, Float capacity, Float length, Float fftime) {
		this.tail = tail;
		this.head = head;
		this.capacity = capacity;
		this.length = length;
		this.fftime = fftime;
		createTable();
	}


	private void createTable() {
		Statement stm;
		try {
		    stm = databaseCon.createStatement();
		    stm.execute(createQuery);
		    stm.close();
        } catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
        }
	}

	private Double totalFlowFromTable() {
		Statement stm;
		try {
			stm = databaseCon.createStatement();
			Double total = null;
			ResultSet result = stm.executeQuery(sumQuery);
			if(result.next()) {
				total = result.getDouble("totalFlow");
			}
			if (total == null)
				return 0.0;
			stm.close();
			return total;
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return 0.0;
	}
	public Float getCapacity() {
		return capacity;
	}

	public Float freeFlowTime() {
		return fftime;
	}

	public Node getHead() {
		return head;
	}

	public Node getTail() {
		return tail;
	}

	public Float getLength() {
		return length;
	}

	public Double getFlow() {
		if (cachedFlow != null) return cachedFlow;
		//BigDecimal f = BigDecimal.ZERO;
		//for (Bush b : flow.keySet()) f = f.add(flow.get(b));
		Double f = totalFlowFromTable();
		if (f < 0) throw new NegativeFlowException("Negative link flow");
		cachedFlow = f;
		return f;
	}

	public String toString() {
		return tail.toString() + "\t" + head.toString();
	}

	public abstract Double getTravelTime();
	
	public abstract Double tPrime();

	public abstract Double tIntegral();

	public abstract Double getPrice(Float vot, VehicleClass c);

	public abstract Double pricePrime(Float float1);

	/** Modifies the flow on a link which comes from a specified bush. 
	 * <b> THIS METHOD SHOULD ONLY BE CALLED BY THE {@link edu.utexas.wrap.Bush}'s {@link edu.utexas.wrap.Bush.changeFlow} METHOD </b>
	 * @param delta amount by how much the flow should be altered
	 * @param bush the origin Bush of this flow
	 * @return whether the flow from this bush on the link is non-zero
	 */
	public synchronized Boolean alterBushFlow(Double delta, Bush bush) {
		if (delta  != 0) {
			cachedTT = null;
			cachedPrice = null;
			cachedFlow = null;
		}
		//Retrieve Bush with the specified parameters
		PreparedStatement stm;
		Double updateFlow = getBushFlow(bush);
		try {
			updateFlow += delta;
			if(updateFlow < 0) throw new NegativeFlowException("invalid alter request");
			else if(updateFlow > 0) {
				stm = databaseCon.prepareStatement(deleteQuery);
				stm.setInt(1, bush.getOrigin().hashCode());
				stm.setFloat(2, bush.getVOT());
				if (bush.getVehicleClass() != null)
					stm.setString(3, bush.getVehicleClass().name());
				else
					stm.setString(3, bush.toString());
				stm.executeUpdate();
			    stm = databaseCon.prepareStatement(insertQuery);
				stm.setInt(1, bush.getOrigin().hashCode());
				stm.setFloat(2, bush.getVOT());
                if(bush.getVehicleClass() != null)
                    stm.setString(3, bush.getVehicleClass().name());
                else
                    stm.setString(3, bush.toString());
                stm.setDouble(4, updateFlow);
				stm.executeUpdate();
				stm.close();
				return true;
			} else {

				stm = databaseCon.prepareStatement(deleteQuery);
				stm.setInt(1, bush.getOrigin().hashCode());
				stm.setFloat(2, bush.getVOT());
				if(bush.getVehicleClass() != null)
				    stm.setString(3, bush.getVehicleClass().name());
				else
				    stm.setString(3, bush.toString());
				stm.executeUpdate();
				stm.close();
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
			return false;
		}
	}

	public Double getBushFlow(Bush bush) {
		PreparedStatement stm;
		try {
			stm = databaseCon.prepareStatement(selectQuery);
			stm.setInt(1, bush.getOrigin().hashCode());
			stm.setFloat(2, bush.getVOT());
			if(bush.getVehicleClass() != null)
			    stm.setString(3, bush.getVehicleClass().name());
			else
			    stm.setString(3, bush.toString());
			ResultSet result = stm.executeQuery();
			if(result.next()) {
				Double output = result.getDouble("flow");
				stm.close();
				return output;
			}
			stm.close();
		} catch (Exception e) {
			//System.out.println("getBush flow");
			//System.out.println("SQL Error Code: " + e.getErrorCode());
			e.printStackTrace();
			System.exit(1);
			return 0.0;
		}
		return 0.0;
	}

	public Boolean hasFlow(Bush bush) {
		PreparedStatement stm;
		try {
			stm = databaseCon.prepareStatement(selectQuery);
			stm.setInt(1, bush.getOrigin().hashCode());
			stm.setFloat(2, bush.getVOT());
			if(bush.getVehicleClass() != null)
			    stm.setString(3, bush.getVehicleClass().name());
			else
			    stm.setString(3, bush.toString());
			boolean output =  stm.executeQuery().next();
			stm.close();
			return output;
		} catch (SQLException e) {

			e.printStackTrace();
			System.exit(1);
			return false;
		}
	}

	public synchronized void removeTable() {
		try{
			Statement stm = databaseCon.createStatement();
			stm.executeUpdate(dropQuery);
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);

		}

	}
	
	public abstract Boolean allowsClass(VehicleClass c);
}