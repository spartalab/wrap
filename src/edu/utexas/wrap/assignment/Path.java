package edu.utexas.wrap.assignment;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.VehicleClass;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.Priced;

public class Path extends LinkedList<Link> implements Priced, AssignmentContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8522817449668927596L;
	
	private final VehicleClass c;
	private final Float vot;
	
	public Path() {
		this.c = null;
		this.vot = null;
	}
	
	public Path(VehicleClass c, Float vot) {
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

	public Double getMinFlow(Bush b, Map<Link, Double> deltaX) {
		Double maxDelta = null;
		for (Link l : this) {
			Double x = l.getFlow(b) + deltaX.getOrDefault(l, 0.0);
			if (maxDelta == null || x.compareTo(maxDelta) < 0) {
				maxDelta = x;
			}
		}
		return maxDelta;
	}

	@Override
	public Double getPrice(Float vot, VehicleClass c) {
		Double sum = 0.0;
		for (Link l : this) sum += l.getPrice(vot,c);
		return sum;
	}

	@Override
	public VehicleClass getVehicleClass() {
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
		Integer idx = indexOf(n);
		Link out = this.get(idx);
		Link in  = this.get(idx-1);
		Double inf  = in == null? 0.0 : in.getFlow(this);
		Double outf = out == null? 0.0 : out.getFlow(this);
		// TODO Auto-generated method stub
		return (float) (inf - outf);
	}

	@Override
	public Set<Link> getLinks() {
		return new HashSet<Link>(this);
	}
}
