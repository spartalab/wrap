package edu.utexas.wrap;

/**
 * @author rahulpatel
 *
 */
public class Link implements Priced {

	private final Double capacity;
	private final Node head;
	private final Node tail;
	private final Double length;
	private final Double fftime;
	private final Double b;
	private final Double power;
	private Double flow;
	private Double toll;
	private Double cachedTT = null;

	public Link(Node tail, Node head, Double capacity, Double length, Double fftime, Double b, Double power, Double toll) {
		this.tail = tail;
		this.head = head;
		this.capacity = capacity;
		this.length = length;
		this.fftime = fftime;
		this.b = b;
		this.power = power;
		this.flow = 0.0;
		this.toll = toll;
	}

	//B and power are empirical constants in the BPR function
	public Double getBValue() {
		return this.b;
	}

	public Double getPower() {
		return power;
	}

	public Double getCapacity() {
		return capacity;
	}

	public Double getFfTime() {
		return fftime;
	}
	
	public Node getHead() {
		return head;
	}

	public Node getTail() {
		return tail;
	}

	public Double getLength() {
		return length;
	}

	public Double getFlow() {
		if (flow < 0.0) throw new NegativeFlowException("Negative link flow");
		return flow;
	}
	public void setFlow(Double flow) {
		cachedTT = null;
		assert flow >= 0.0;
		this.flow = flow;
	}
	//Used to add deltaflow to current link flow
//	public void addFlow(Double deltaflow) {
//		//System.out.println(this.toString()+" add: "+Double.toString(deltaflow));
//		if (deltaflow != 0.0) cachedTT = null;
//		this.flow += deltaflow;
//		if (flow < 0.0) throw new RuntimeException("flow is "+flow.toString());
//
//		this.flow = (Double) Math.max(flow, 0.0);
//	}
	
//	public void subtractFlow(Double deltaFlow) {
//		//System.out.println(this.toString()+" sub: "+Double.toString(deltaFlow));
//		if (deltaFlow != 0.0) cachedTT = null;
//		this.flow -= deltaFlow;
//		if (flow < 0.0) throw new RuntimeException("flow is "+flow.toString());
//		this.flow = (Double) Math.max(flow, 0.0);
//	}
	
	public void changeFlow(Double delta) {
		if (flow + delta < 0.0) throw new NegativeFlowException("Removed too much link flow");
		flow = flow + delta;
		if (delta != 0.0) cachedTT = null;
	}

	/**BPR Function
	 * A link performance function using empirical constants (b and power) and 
	 * link characteristics (current flow, capacity, & free flow travel time)
	 * @return travel time for the link at current flow
	 * @throws Exception 
	 */
	public Double getTravelTime() throws Exception {
		if (cachedTT != null) return cachedTT;
		cachedTT = getFfTime()*(1.0 + getBValue()*Math.pow(getFlow()/getCapacity(), getPower()));
		return cachedTT;
	}
	
	public String toString() {
		return tail.toString() + " -> " + head.toString();
	}

	/**Derivative of {@link getTravelTime} formula
	 * Calculate the derivative of the BPR function with respect to the flow
	 * @return t': the derivative of the BPR function
	 * @throws Exception 
	 */
	public Double tPrime() throws Exception {
		// Return (a*b*t*(v/c)^a)/v
		//TODO: cache this
		Double a = getPower();
		Double b = getBValue();
		Double t = getFfTime();
		Double v = getFlow();
		Double c = getCapacity();
		Double va = Math.pow(v, a-1.0);
		Double ca = Math.pow(c, -a);
		return a*va*t*b*ca;
	}
	
	public Double tIntegral() throws Exception{
		Double a = getPower();
		Double b = getBValue();
		Double t = getFfTime();
		Double v = getFlow();
		Double c = getCapacity();
		
		return t*v + t*b*(Math.pow(v,a+1))/((a+1)*(Math.pow(c, a)));
		
	}
	
	public Double getToll() {
		return toll;
	}
	
	public Double tollPrime() {
		//TODO: Modify this if tolls change in response to flow
		return 0.0;
	}

	@Override
	public Double getPrice(Double vot) {
		try {
			return getTravelTime() * vot + getToll();
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0.0;
		}
	}
	
	public Double pricePrime(Double vot) {
		try {
			return vot * tPrime() + tollPrime();
		} catch (Exception e) {
			return tollPrime();
		}
	}
}
