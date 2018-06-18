package edu.utexas.wrap;

/**
 * @author rahulpatel
 *
 */
public class Link implements Priced {

	private Double capacity;
	private Node head;
	private Node tail;
	private Double length;
	private Double fftime;
	private Double b;
	private Double power;
	private Double flow;
	private Double toll;

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
	public void setBValue(Double bvalue) {
		this.b = bvalue;
	}
	public Double getPower() {
		return power;
	}
	public void setPower(Double power) {
		this.power = power;
	}
	public Double getCapacity() {
		return capacity;
	}
	public void setCapacity(Double capacity) {
		this.capacity = capacity;
	}
	public Double getFfTime() {
		return fftime;
	}
	public void setFfTime(Double fftime) {
		this.fftime = fftime;
	}
	public Node getHead() {
		return head;
	}
	public void setHead(Node head) {
		this.head = head;
	}
	public Node getTail() {
		return tail;
	}
	public void setTail(Node tail) {
		this.tail = tail;
	}
	public Double getLength() {
		return length;
	}
	public void setLength(Double length) {
		this.length = length;
	}
	public Double getFlow() throws Exception {
		if(this.flow < 0) throw new Exception();
		return this.flow;
	}
	public void setFlow(Double flow) {
		this.flow = flow;
	}
	//Used to add deltaflow to current link flow
	public void addFlow(Double deltaflow) {
		//System.out.println(this.toString()+" add: "+Double.toString(deltaflow));
		this.flow += deltaflow;
		this.flow = (Double) Math.max(flow, 0.0);
	}
	
	public void subtractFlow(Double deltaFlow) {
		//System.out.println(this.toString()+" sub: "+Double.toString(deltaFlow));
		this.flow -= deltaFlow;
		this.flow = (Double) Math.max(flow, 0.0);
	}

	/**BPR Function
	 * A link performance function using empirical constants (b and power) and 
	 * link characteristics (current flow, capacity, & free flow travel time)
	 * @return travel time for the link at current flow
	 * @throws Exception 
	 */
	public Double getTravelTime() throws Exception {
		return (Double) (getFfTime()*(1.0 + getBValue()*Math.pow(getFlow()/getCapacity(), getPower())));
	}
	
	public String toString() {
		return this.tail.toString() + " -> " + this.head.toString();
	}

	/**Derivative of {@link getTravelTime} formula
	 * Calculate the derivative of the BPR function with respect to the flow
	 * @return t': the derivative of the BPR function
	 * @throws Exception 
	 */
	public Double tPrime() throws Exception {
		// Return (a*b*t*(v/c)^a)/v
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
	
	public Double toll() {
		return toll;
	}
	
	public Double tollPrime() {
		return 0.0;
	}

	@Override
	public Double getPrice(Double vot) {
		try {
			return getTravelTime() * vot + toll();
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
