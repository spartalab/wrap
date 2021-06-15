/*
    wrap - free and open-source urban transportation modeling software
    Copyright (C) 2017 the wrap project, The University of Texas at Austin

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.utexas.wrap.assignment;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.Priced;

/** A sequential list of {@link edu.utexas.wrap.net.Link} objects.
 * 
 * @author William
 *
 */

public class Path extends LinkedList<Link> implements Priced {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8522817449668927596L;
	
	
	/**
	 * Instantiate a Path with no vehicle class or vot
	 * (general purpose but may be unstable)
	 * @param list 
	 */
	public Path(List<Link> list) {
		super(list);
	}
	
	public Path() {
		
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
		return new Path(this.subList(start, end));

	}

	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		String ret = "[";
		for (Node n : nodes()) ret += n.toString()+",";
		return ret+"]";
	}



}
