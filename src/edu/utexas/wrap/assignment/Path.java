package edu.utexas.wrap.assignment;

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.Priced;

/** A sequential list of {@link edu.utexas.wrap.net.Link} objects.
 * 
 * @author William
 *
 */
public class Path extends LinkedList<Link> implements Priced, AssignmentContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8522817449668927596L;
	
	private final Mode c;
	private final Float vot;
//	private DemandHashMap demand;
	
	/**
	 * Instantiate a Path with no vehicle class or vot
	 * (general purpose but may be unstable)
	 */
	public Path() {
		this.c = null;
		this.vot = null;
	}
	
	/**
	 * @param c the Mode of travel on the path
	 * @param vot the VOT of travelers on the path
	 */
	public Path(Mode c, Float vot) {
		this.c=c;
		this.vot=vot;
	}

	/**
	 * @param spurPath the link to be appended to this
	 */
	public void append(Path spurPath) {
		addAll(spurPath);
	}

	/**
	 * @param other the Path to compare
	 * @return whether the Path consists of the same Links
	 */
	public boolean equals(Path other) {
		if (other.size() != size()) return false;
		for (Integer i = 0; i < size(); i++) {
			if (!other.get(i).equals(get(i))) return false;
		}
		return true;
	}

	/**
	 * @return the sum of the Path's constituent Links' lengths
	 */
	public Double getLength() {
		Double sum = 0.0;
		for (Link l : this) sum += l.getLength();
		return sum;
	}

	/** For a given set of container flows, determine the minimum
	 * amount of flow in the container that is present on all Links in the
	 * Path, i.e. the most flow that can be removed from the path without
	 * allowing a Link's flow to become negative.
	 * @param containerFlows the (container) flows on all Links in network
	 * @return the minimum amount of flow that is on all Links in the Path 
	 */
	public Double getMinFlow(Map<Link,Double> containerFlows) {
		Double maxDelta = null;
		for (Link l : this) {
			Double x = containerFlows.getOrDefault(l, 0.0);
			if (maxDelta == null || x.compareTo(maxDelta) < 0) {
				maxDelta = x;
			}
		}
		return maxDelta;
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.net.Priced#getPrice(java.lang.Float, edu.utexas.wrap.modechoice.Mode)
	 */
	@Override
	public double getPrice(Float vot, Mode c) {
		return this.stream().mapToDouble(x -> x.getPrice(vot, c)).sum();
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getVehicleClass()
	 */
	@Override
	public Mode getVehicleClass() {
		return c;
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getVOT()
	 */
	@Override
	public Float getVOT() {
		return vot;
	}

	/**
	 * @param n the Node whose index should be returned
	 * @return the Node's position in the List's ordered Nodes
	 */
	public Integer indexOf(Node n) {
		int index = 0;
		Iterator<Link> i = this.iterator();
		Link l = null;
		while (i.hasNext()) {
			l = i.next();
			if (l.getTail().equals(n)) return index;
			index++;
		}
		if (l != null && l.getHead().equals(n)) return index;
		return null;
	}
	
	/**
	 * @param index the position of the Node to be returned
	 * @return the Node at a given index
	 */
	public Node node(Integer index) {
		if (index == size()) return getLast().getHead();
		return get(index).getTail();
	}

	/**
	 * @return the inorder List of the Nodes in the Path 
	 */
	public List<Node> nodes() {
		List<Node> list = new LinkedList<Node>();
		for (Link l : this) {
			list.add(l.getTail());
		}
		if (!isEmpty()) list.add(getLast().getHead());
		return list;
	}

	/**
	 * @param start the position of the first Link to be included
	 * @param end the position of the last Link to be included
	 * @return the subPath between the two given points
	 */
	public Path subPath(Integer start, Integer end) {
		Path sp = new Path(getVehicleClass(), getVOT());
		for (Integer i = start; i < size() && i < end; i++) {
			sp.add(get(i));
		}
		return sp;
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		String ret = "[";
		for (Node n : nodes()) ret += n.toString()+",";
		return ret+"]";
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getDemand(edu.utexas.wrap.net.Node)
	 */
	@Override
	public Float getDemand(Node n) {
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getLinks()
	 */
	@Override
	public Set<Link> getLinks() {
		return new HashSet<Link>(this);
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getFlow(edu.utexas.wrap.net.Link)
	 */
	@Override
	public Double getFlow(Link l) {
		//TODO
		throw new RuntimeException("Not Yet Implemented");
	}

	@Override
	public Map<Link, Double> getFlows() {
		//TODO
		throw new RuntimeException("Not Yet Implemented");
	}

	@Override
	public void fromFile(BufferedInputStream in) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not Yet Implemented");
	}

	@Override
	public void toFile(OutputStream out) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not Yet Implemented");
	}
}
