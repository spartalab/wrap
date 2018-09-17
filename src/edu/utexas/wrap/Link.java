package edu.utexas.wrap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.sql.*;
//import java.sql.DriverManager;
/**
 * @author rahulpatel
 *
 */
public abstract class Link implements Priced {

	private final Float capacity;
	private final Node head;
	private final Node tail;

	private Map<AssignmentContainer,Double> flow;

	private BigDecimal cachedFlow = null;
	protected BigDecimal cachedTT = null;
	protected BigDecimal cachedPrice = null;

	public Link(Node tail, Node head, Float capacity, Float length, Float fftime) {
		this.tail = tail;
		this.head = head;
		this.capacity = capacity;
		this.length = length;
		this.fftime = fftime;

		this.flow = new HashMap<Bush,BigDecimal>();
		try {
			Class.forName("org.postgresql.Driver");
			databaseCon = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + dbName);
			System.out.println("Able to connect to database");
            createTable(databaseCon);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Could not find database/table to connect to");
			System.exit(3);
		}
	}


	private void createTable() {
		Statement stm;
		String query = "CREATE TABLE t" + hashCode() + " (" +
				"bush_origin_id integer, " +
				"vot real, " +
				"vehicle_class text, " +
                "flow decimal(" + Optimizer.defMC.getPrecision() + ")," +
				"UNIQUE (bush_origin_id, vot, vehicle_class))";
		try {
		    stm = databaseCon.createStatement();
		    stm.execute(query);
        } catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
        }
	}

	private BigDecimal totalFlowFromTable() {
		Statement stm;
		String query = "SELECT SUM (flow) AS totalFlow FROM t" + hashCode();
		try {
			stm = databaseCon.createStatement();
			BigDecimal total = null;
			ResultSet result = stm.executeQuery(query);
			if(result.next()) {
				total = result.getBigDecimal("totalFlow");
			}
			if (total == null)
				return BigDecimal.ZERO;
			return total;
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return BigDecimal.ZERO;
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

	public BigDecimal getFlow() {
		if (cachedFlow != null) return cachedFlow;
		BigDecimal f = BigDecimal.ZERO;
		for (Bush b : flow.keySet()) f = f.add(flow.get(b));
		if (f.compareTo(BigDecimal.ZERO) < 0) throw new NegativeFlowException("Negative link flow");
		cachedFlow = f;
		return f;
	}

	public String toString() {
		return tail.toString() + "\t" + head.toString();
	}

	public abstract BigDecimal getTravelTime();
	
	public abstract BigDecimal tPrime();

	public abstract Boolean allowsClass(VehicleClass c);

	/** Modifies the flow on a link which comes from a specified bush. 
	 * <b> THIS METHOD SHOULD ONLY BE CALLED BY THE {@link edu.utexas.wrap.Bush}'s {@link edu.utexas.wrap.Bush.changeFlow} METHOD </b>
	 * @param delta amount by how much the flow should be altered
	 * @param bush the origin Bush of this flow
	 * @return whether the flow from this bush on the link is non-zero
	 */
	public synchronized Boolean alterBushFlow(Double delta, Bush bush) {
		if (delta != 0) {
			cachedTT = null;
			cachedPrice = null;
			cachedFlow = null;
		}
		//Retrieve Bush with the specified parameters
		PreparedStatement stm;
		BigDecimal updateFlow = getBushFlow(bush);
		String query = "INSERT INTO t" + hashCode() + " (bush_origin_id, vot, vehicle_class, flow) " +
				"VALUES (" +
				"?,?,?,?)" +
				"ON CONFLICT (bush_origin_id, vot, vehicle_class)" +
				"DO UPDATE " +
				"SET flow = ? " +
				"WHERE " +
				"t" +hashCode()+".bush_origin_id = ? " +
				"AND t"+hashCode()+".vot = ? " +
				"AND t"+hashCode()+".vehicle_class = ?";
		try {
			updateFlow = updateFlow.add(delta).setScale(Optimizer.decimalPlaces, RoundingMode.HALF_EVEN);
			if(updateFlow.compareTo(BigDecimal.ZERO) < 0) throw new NegativeFlowException("invalid alter request");
			else if(updateFlow.compareTo(BigDecimal.ZERO) > 0) {
				stm = databaseCon.prepareStatement(query);
				stm.setInt(1, bush.getOrigin().getID());
				stm.setFloat(2, bush.getVOT());
				stm.setString(3, bush.toString());
				stm.setBigDecimal(4, updateFlow);
				stm.setBigDecimal(5, updateFlow);
				stm.setInt(6, bush.getOrigin().getID());
				stm.setFloat(7, bush.getVOT());
				stm.setString(8, bush.toString());
				stm.executeUpdate();
				return true;
			} else {
				query = "DELETE FROM t" + hashCode() +
						" WHERE " +
						"bush_origin_id = ? " +
						"AND vot = ? " +
						"AND vehicle_class = ?";
				stm = databaseCon.prepareStatement(query);
				stm.setInt(1, bush.getOrigin().getID());
				stm.setFloat(2, bush.getVOT());
				stm.setString(3, bush.toString());
				stm.executeUpdate();
				return false;
			}
		} catch (SQLException e) {
			//System.out.println("alter bush flow");
			//System.out.println("SQL Error Code: " + e.getErrorCode());
			e.printStackTrace();
			System.exit(1);
			return false;
		}
		//If Bush exists, get the flow and add delta to flow
			//If new flow is < 0 throw error
			//if new flow == 0 remove the row and return false
			//If new flow is > 0 update the row in the table
		//Otherwise add Bush with delta flow
//		BigDecimal newFlow = flow.getOrDefault(bush,BigDecimal.ZERO).add(delta).setScale(Optimizer.decimalPlaces, RoundingMode.HALF_EVEN);
//		if (newFlow.compareTo(BigDecimal.ZERO) < 0) throw new NegativeFlowException("invalid alter request");
//		else if (newFlow.compareTo(BigDecimal.ZERO) > 0) flow.put(bush, newFlow);
//		else {
//			flow.remove(bush);
//			return false;
//		}
//		return true;
	}

	public BigDecimal getBushFlow(Bush bush) {
		return flow.getOrDefault(bush, BigDecimal.ZERO);
	}

		try {
			stm = databaseCon.prepareStatement(query);
			stm.setInt(1, bush.getOrigin().getID());
			stm.setFloat(2, bush.getVOT());
			//System.out.println(bush.toString());
			stm.setString(3, bush.toString());
			ResultSet result = stm.executeQuery();
			if(result.next()) {
				return result.getBigDecimal("flow");
			}
		} catch (Exception e) {
			//System.out.println("getBush flow");
			//System.out.println("SQL Error Code: " + e.getErrorCode());
			e.printStackTrace();
			System.exit(1);
			return BigDecimal.ZERO;
		}
		return BigDecimal.ZERO;
	}

	public abstract Double getTravelTime();

	public Boolean hasFlow(Bush bush) {
		PreparedStatement stm;
		String query = "SELECT * FROM t" + hashCode() +
				" WHERE " +
				"bush_origin_id = ? " +
				"AND vot = ? " +
				"AND vehicle_class = ?";

		try {
			stm = databaseCon.prepareStatement(query);
			stm.setInt(1, bush.getOrigin().getID());
			stm.setFloat(2, bush.getVOT());
			if(bush.getVehicleClass() != null)
			    stm.setString(3, bush.getVehicleClass().name());
			else
			    stm.setString(3, bush.toString());
			return stm.executeQuery().next();
		} catch (SQLException e) {
			//System.out.println("has flow");
			//System.out.println("SQL Error Code: " + e.getErrorCode());
			e.printStackTrace();
			System.exit(1);
			return false;
		}
		//return flow.get(bush) != null;
	}

	public void removeTable() {
		String query = "DROP TABLE t" + hashCode();
		try{
			Statement stm = databaseCon.createStatement();
			stm.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
			//System.out.println("removeTable");
			//System.out.println("SQL Error Code: " + e.getErrorCode());
		}

	}

	public abstract Double pricePrime(Float float1);

	public abstract Double tIntegral();

	public String toString() {
		return tail.toString() + "\t" + head.toString();
	}

	public abstract Boolean allowsClass(VehicleClass c);
}