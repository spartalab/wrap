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

	private static final String dbName = "network";
	private final Float capacity;
	private final Node head;
	private final Node tail;

	private Map<Bush,BigDecimal> flow;

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
	}


	private void createTable() {
		Statement stm;
		try {
		    stm = databaseCon.createStatement();
		    stm.execute(createQuery);
        } catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
        }
	}

	private BigDecimal totalFlowFromTable() {
		Statement stm;
		try {
			stm = databaseCon.createStatement();
			BigDecimal total = null;
			ResultSet result = stm.executeQuery(sumQuery);
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
		try {
			updateFlow = updateFlow.add(delta).setScale(Optimizer.decimalPlaces, RoundingMode.HALF_EVEN);
			if(updateFlow.compareTo(BigDecimal.ZERO) < 0) throw new NegativeFlowException("invalid alter request");
			else if(updateFlow.compareTo(BigDecimal.ZERO) > 0) {
				stm = databaseCon.prepareStatement(updateQuery);
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

				stm = databaseCon.prepareStatement(deleteQuery);
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
		PreparedStatement stm;
		String query = "SELECT * FROM t" + hashCode() +
				" WHERE " +
				"bush_origin_id = ? " +
				"AND vot = ? " +
				"AND vehicle_class = ?";

		try {
			stm = databaseCon.prepareStatement(selectQuery);
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
		try {
			stm = databaseCon.prepareStatement(selectQuery);
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
		try{
			Statement stm = databaseCon.createStatement();
			stm.executeUpdate(dropQuery);
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

	public int hashCode() {

	}
}