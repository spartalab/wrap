package edu.utexas.wrap;


import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.Collection;

/**
 * @author rahulpatel
 *
 */
public abstract class Link implements Priced {

	private final float capacity, length, fftime;
	private final Node head;
	private final Node tail;

	private Double cachedFlow = null;
	protected Double cachedTT = null;
	protected Double cachedPrice = null;
	private static RedissonClient databaseCon;
    private RMap<String,Double> flow;

	static {
		try {
            databaseCon = Redisson.create();
			System.out.println("Connected to database");
		} catch (Exception e) {
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
		this.flow = databaseCon.getMap("t" + hashCode());
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
        if (delta != 0) {
            cachedTT = null;
            cachedPrice = null;
            cachedFlow = null;
        }
        Double newFlow = getBushFlow(bush) + delta;
        if (newFlow < 0) throw new NegativeFlowException("invalid alter request");
        else if (newFlow > 0) flow.fastPut(bush.toString(), newFlow);
        else {
            flow.remove(bush.toString());
            return false;
        }
        return true;

    }

	public Double getBushFlow(Bush bush) {
		Double f = flow.get(bush.toString());
		if (f != null)
			return f;
		else return 0.0;
	}

    public Double getFlow() {
        if (cachedFlow != null) return cachedFlow;
        Double f = 0.0;
//		Collection<Double> output = flow.values();
		for (Double v : flow.values()) {
			f += v;
		}
        if (f < 0) throw new NegativeFlowException("Negative link flow");
        cachedFlow = f;
        return f;
    }

    public Boolean hasFlow(Bush bush) {
        return flow.get(bush.toString()) != null;
    }

	public void removeTable() {
        this.flow.delete();
	}
	
	public abstract Boolean allowsClass(VehicleClass c);
}
