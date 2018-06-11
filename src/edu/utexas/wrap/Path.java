package edu.utexas.wrap;

import java.util.LinkedList;
import java.util.List;

public class Path extends LinkedList<Link> implements Priced {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8522817449668927596L;

	public Node node(Integer i) {
		return this.get(i).getTail();
	}

	public Path subPath(Integer start, Integer end) {
		Path sp = new Path();
		for (Integer i = start; i < this.size() && i < end; i++) {
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
		// TODO Auto-generated method stub
		List<Node> list = new LinkedList<Node>();
		for (Link l : this) {
			list.add(l.getTail());
		}
		if (this.size() > 0) list.add(this.getLast().getHead());
		return list;
	}

	public void append(Path spurPath) {
		for (Link l : spurPath) {
			add(l);
		}
	}

	@Override
	public Double getPrice(Double vot) {
		Double sum = 0.0;
		for (Link l : this) {
			try {
				sum += l.getPrice(vot);
			} catch (Exception e) {}
		}
		return sum;
	}
	
	public String toString() {
		String ret = "";
		for (Link l : this) {
			ret += l.toString() + ",";
		}
		return ret;
	}

	public Double getLength() {
		// TODO Auto-generated method stub
		Double sum = 0.0;
		for (Link l : this) sum += l.getLength();
		return sum;
	}

}
