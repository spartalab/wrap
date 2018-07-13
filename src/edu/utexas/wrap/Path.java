package edu.utexas.wrap;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class Path extends LinkedList<Link> implements Priced {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8522817449668927596L;

	public Node node(Integer index) {
		if (index == size()) return getLast().getHead();
		return get(index).getTail();
	}

	public Path subPath(Integer start, Integer end) {
		Path sp = new Path();
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
	public BigDecimal getPrice(Double vot) {
		BigDecimal sum = BigDecimal.ZERO;
		for (Link l : this) sum =sum.add(l.getPrice(vot));
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

}
