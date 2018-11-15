package edu.utexas.wrap;


import java.math.BigDecimal;
import java.math.RoundingMode;

import com.datastax.driver.core.*;
import com.datastax.driver.core.Cluster.Builder;

/**
 * @author rahulpatel
 *
 */
public abstract class Link implements Priced {
	private static final String dbName = "network";

	private final float capacity, length, fftime;
	private final Node head;
	private final Node tail;

	private BigDecimal cachedFlow = null;
	protected BigDecimal cachedTT = null;
	protected BigDecimal cachedPrice = null;
	private static Session databaseCon;
 	private final String createQuery = "CREATE TABLE t" + hashCode() + " (" +
			"bush_origin_id integer, " +
			"vot real, " +
			"vehicle_class text, " +
			"flow decimal(" + Optimizer.defMC.getPrecision() + ")," +
			"PRIMARY KEY (bush_origin_id, vot, vehicle_class))";
	private final String sumQuery = "SELECT SUM (flow) AS totalFlow FROM t" + hashCode();

	private final String updateQuery = "INSERT INTO t" + hashCode() + " (bush_origin_id, vot, vehicle_class, flow) " +
			"VALUES (" +
			"?,?,?,?)";

	private final String deleteQuery = "DELETE FROM t" + hashCode() +
			" WHERE " +
			"bush_origin_id = ? " +
			"AND vot = ? " +
			"AND vehicle_class = ?";

	private final String selectQuery = "SELECT 1 FROM t" + hashCode() +
			" WHERE " +
			"bush_origin_id = ? " +
			"AND vot = ? " +
			"AND vehicle_class = ?";

	private final String dropQuery = "DROP TABLE t" + hashCode();
	static {
        Builder b = Cluster.builder().addContactPoint("127.0.0.1");
        b.withPort(9142);
        Cluster cluster = b.build();

        Session session = cluster.connect();
        session.execute("Create KEYSPACE "+ dbName +" IF NOT EXISTS WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 1}");
        session.execute("Use " + dbName);
		databaseCon = session;
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
		SimpleStatement stm = new SimpleStatement(createQuery);
		databaseCon.execute(stm);
	}

	private BigDecimal totalFlowFromTable() {
        BigDecimal total = null;
        ResultSet result = databaseCon.execute(sumQuery);
        Row out = result.one();
        if(out != null) {
            total = out.getDecimal("totalFlow");
        }
        if (total == null)
            return BigDecimal.ZERO;
        return total;
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
		//BigDecimal f = BigDecimal.ZERO;
		//for (Bush b : flow.keySet()) f = f.add(flow.get(b));
		BigDecimal f = totalFlowFromTable();
		if (f.compareTo(BigDecimal.ZERO) < 0) throw new NegativeFlowException("Negative link flow");
		cachedFlow = f;
		return f;
	}

	public String toString() {
		return tail.toString() + "\t" + head.toString();
	}

	public abstract BigDecimal getTravelTime();
	
	public abstract BigDecimal tPrime();

	public abstract BigDecimal tIntegral();

	public abstract BigDecimal getPrice(Float vot, VehicleClass c);

	public abstract BigDecimal pricePrime(Float float1);

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
        updateFlow = updateFlow.add(delta).setScale(Optimizer.decimalPlaces, RoundingMode.HALF_EVEN);
        if(updateFlow.compareTo(BigDecimal.ZERO) < 0) throw new NegativeFlowException("invalid alter request");
        else if(updateFlow.compareTo(BigDecimal.ZERO) > 0) {
            stm = databaseCon.prepare(updateQuery);
            BoundStatement bound;
            if(bush.getVehicleClass() != null)
                bound = stm.bind(bush.getOrigin().getID(), bush.getVOT(), bush.getVehicleClass().name(), updateFlow);
            else
                bound = stm.bind(bush.getOrigin().getID(), bush.getVOT(), bush.toString(), updateFlow);
            databaseCon.execute(bound);
            return true;
        } else {
            stm = databaseCon.prepare(deleteQuery);
            BoundStatement bound;
            if(bush.getVehicleClass() != null)
                bound = stm.bind(bush.getOrigin().getID(), bush.getVOT(), bush.getVehicleClass().name(), updateFlow);
            else
                bound = stm.bind(bush.getOrigin().getID(), bush.getVOT(), bush.toString(), updateFlow);
            databaseCon.execute(bound);
            return false;
        }
	}

	public BigDecimal getBushFlow(Bush bush) {
		PreparedStatement stm;
        stm = databaseCon.prepare(selectQuery);
        BoundStatement bound;
        if(bush.getVehicleClass() != null)
            bound = stm.bind(bush.getOrigin().getID(), bush.getVOT(), bush.getVehicleClass().name());
        else
            bound = stm.bind(bush.getOrigin().getID(), bush.getVOT(), bush.toString());
        ResultSet out = databaseCon.execute(bound);
        Row one = out.one();
        if(one != null) {
            return one.getDecimal("flow");
        }
		return BigDecimal.ZERO;
	}

	public Boolean hasFlow(Bush bush) {
        PreparedStatement stm;
        stm = databaseCon.prepare(selectQuery);
        BoundStatement bound;
        if(bush.getVehicleClass() != null)
            bound = stm.bind(bush.getOrigin().getID(), bush.getVOT(), bush.getVehicleClass().name());
        else
            bound = stm.bind(bush.getOrigin().getID(), bush.getVOT(), bush.toString());
        ResultSet out = databaseCon.execute(bound);
        return !out.isExhausted();
	}

	public void removeTable() {
		try{
			Statement stm = new SimpleStatement(dropQuery);
			databaseCon.execute(dropQuery);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
			//System.out.println("removeTable");
			//System.out.println("SQL Error Code: " + e.getErrorCode());
		}

	}
	
	public abstract Boolean allowsClass(VehicleClass c);
}