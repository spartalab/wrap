package edu.utexas.wrap.assignment;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.demand.containers.DemandHashMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.Priced;

/** A sequential list of {@link edu.utexas.wrap.net.Link} objects
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
	private DemandHashMap demand;
	
	public Path() {
		this.c = null;
		this.vot = null;
	}
	
	public Path(Mode c, Float vot) {
		this.c=c;
		this.vot=vot;
	}

	public void append(Path spurPath) {
		addAll(spurPath);
	}

	public boolean equals(Path other) {
		if (other.size() != size()) return false;
		for (Integer i = 0; i < size(); i++) {
			if (!other.get(i).equals(get(i))) return false;
		}
		return true;
	}

	public Double getLength() {
		Double sum = 0.0;
		for (Link l : this) sum += l.getLength();
		return sum;
	}

	public Double getMinFlow(Bush b, Map<Link, Double> deltaX, Map<Link,Double> flows) {
		Double maxDelta = null;
		for (Link l : this) {
			Double x = flows.get(l) + deltaX.getOrDefault(l, 0.0);
			if (maxDelta == null || x.compareTo(maxDelta) < 0) {
				maxDelta = x;
			}
		}
		return maxDelta;
	}

	@Override
	public Double getPrice(Float vot, Mode c) {
		Double sum = 0.0;
		for (Link l : this) sum += l.getPrice(vot,c);
		return sum;
	}

	@Override
	public Mode getVehicleClass() {
		return c;
	}

	@Override
	public Float getVOT() {
		return vot;
	}

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
	
	public Node node(Integer index) {
		if (index == size()) return getLast().getHead();
		return get(index).getTail();
	}

	public List<Node> nodes() {
		List<Node> list = new LinkedList<Node>();
		for (Link l : this) {
			list.add(l.getTail());
		}
		if (!isEmpty()) list.add(getLast().getHead());
		return list;
	}

	public Path subPath(Integer start, Integer end) {
		Path sp = new Path(getVehicleClass(), getVOT());
		for (Integer i = start; i < size() && i < end; i++) {
			sp.add(get(i));
		}
		return sp;
	}

	public String toString() {
		String ret = "[";
		for (Node n : nodes()) ret += n.toString()+",";
		return ret+"]";
	}

	@Override
	public Float getDemand(Node n) {
		return demand != null? demand.get(n) : null;
	}

	@Override
	public Set<Link> getLinks() {
		return new HashSet<Link>(this);
	}

	@Override
	public Double getFlow(Link l) {
		throw new RuntimeException("Not Yet Implemented");
	}
}
