package edu.utexas.wrap;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Path extends LinkedList<Link> implements Priced, AssignmentContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8522817449668927596L;
	
	private final VehicleClass c;
	private final Float vot;
	
	public Path(VehicleClass c, Float vot) {
		this.c=c;
		this.vot=vot;
	}
	
	public Path() {
		this.c = null;
		this.vot = null;
	}

	public Node node(Integer index) {
		if (index == size()) return getLast().getHead();
		return get(index).getTail();
	}

	public Path subPath(Integer start, Integer end) {
		Path sp = new Path(getVehicleClass(), getVOT());
		for (Integer i = start; i < size() && i < end; i++) {
			sp.add(get(i));
		}
		return sp;
	}

	public boolean equals(Path other) {
		if (other.size() != size()) return false;
		for (Integer i = 0; i < size(); i++) {
			if (!other.get(i).equals(get(i))) return false;
		}
		return true;
	}

	public List<Node> nodes() {
		List<Node> list = new LinkedList<Node>();
		for (Link l : this) {
			list.add(l.getTail());
		}
		if (!isEmpty()) list.add(getLast().getHead());
		return list;
	}

	public void append(Path spurPath) {
		addAll(spurPath);
	}

	@Override
	public Double getPrice(Float vot, VehicleClass c) {
		Double sum = 0.0;
		for (Link l : this) sum += l.getPrice(vot,c);
		return sum;
	}

	public String toString() {
		String ret = "[";
		for (Node n : nodes()) ret += n.toString()+",";
		return ret+"]";
	}

	public Double getLength() {
		Double sum = 0.0;
		for (Link l : this) sum += l.getLength();
		return sum;
	}

	public Double getMinFlow(Bush b, Map<Link, Double> deltaX) {
		Double maxDelta = null;
		for (Link l : this) {
			Double x = l.getBushFlow(b) + deltaX.getOrDefault(l, 0.0);
			if (maxDelta == null || x.compareTo(maxDelta) < 0) {
				maxDelta = x;
			}
		}
		return maxDelta;
	}

	@Override
	public VehicleClass getVehicleClass() {
		return c;
	}

	@Override
	public Float getVOT() {
		return vot;
	}

}
