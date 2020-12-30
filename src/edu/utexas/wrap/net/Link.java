package edu.utexas.wrap.net;


import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.assignment.bush.BackVector;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.util.NegativeFlowException;

/**A link, as in graph theory, between two nodes of a network, with arbitrary cost function
 * 
 * @author rahulpatel
 *
 */
public abstract class Link implements Priced, BackVector {

	protected final Float capacity;
	protected final Float length;
	protected final Float fftime;
	private final Node head;
	private final Node tail;
	protected ReadWriteLock ttLock;
	private int headIdx;
	
	protected Double flo;

//	private Map<AssignmentContainer,Double> flow;

//	private Double cachedFlow = null;
	protected Double cachedTT = null;
	protected Double cachedTP = null;
	private final int lid;

	public Link(Node tail, Node head, Float capacity, Float length, Float fftime, Integer linkID) {
		this.tail = tail;
		this.head = head;
		this.capacity = capacity;
		this.length = length;
		this.fftime = fftime;
		this.flo = 0.0;
		
		lid = linkID;
		
//		long d = 1999,	//  We gonna party like it's
//		c = 76537,	//	UT 76-5-37 TAMC \m/
//		b = 1831,	//	Founding of Univ. of Alabama
//		a = 2017;	//	Year of inception for this project
		
//		long a = 3, b = 5, c = 7, d = 11;
//		uid = (((head.getID()*a + tail.getID())*b + capacity.hashCode())*c + fftime.hashCode())*d+length.hashCode();
		ttLock = new ReentrantReadWriteLock();
	}

	public abstract Boolean allowsClass(Mode c);

	/** Modifies the flow on a link which comes from a specified bush. 
	 * @param delta amount by how much the flow should be altered
	 * @return whether the flow from this bush on the link is non-zero
	 */
	public Boolean changeFlow(Double delta) {
		ttLock.writeLock().lock();

		if (flo + delta <0.0) flo = 0.0;
		else flo += delta;

		if (delta != 0.0) {
			cachedTT = null;
			cachedTP = null;
		}
		ttLock.writeLock().unlock();
		return flo > 0.0;

	}

	public Float freeFlowTime() {
		return fftime;
	}


	public Float getCapacity() {
		return capacity;
	}

	public Double getFlow() {
//		if (cachedFlow != null) return cachedFlow;
//		Double f = flow.values().stream().mapToDouble(Double::doubleValue).sum();
		if (flo < 0) throw new NegativeFlowException("Negative flow on link "+this.toString());
//		cachedFlow = f;
		return flo;
	}

	public Node getHead() {
		return head;
	}

	public Float getLength() {
		return length;
	}
	
	public Node getTail() {
		return tail;
	}

	public abstract double getTravelTime();

	public int hashCode() {
//		throw new RuntimeException();
		return lid;
	}

	
	public abstract double pricePrime(Float vot);

	public abstract double tIntegral();

	public String toString() {
		return tail.toString() + "\t" + head.toString();
	}
	
	public abstract double tPrime();
	
	public Link getShortLink() {
		return this;
	}
	
	public Link getLongLink() {
		return this;
	}

	public abstract double getPrice(AssignmentContainer container);

	public void setHeadIndex(int i) {
		// TODO Auto-generated method stub
		headIdx = i;
	}
	
	public int headIndex() {
		return headIdx;
	}
	
}