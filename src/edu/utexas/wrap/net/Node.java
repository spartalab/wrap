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
package edu.utexas.wrap.net;

/**A node, as in graph theory, which connects to others via several Links
 * 
 * @author William
 *
 */
public class Node {

	private final int ID;
	private final TravelSurveyZone zone;
	private final int graphOrder;
	private Link[] outLinks, inLinks;
	
	public Node(Integer ID, Integer order, TravelSurveyZone zone) {
		this.ID = ID;
		graphOrder = order;
		this.zone = zone;
	}
	
	public Node(Node n) {
		this.ID = n.ID;
		this.graphOrder = n.graphOrder;
		this.zone = n.zone;
	}
	
	public boolean equals(Node n) {
		return n.getID() == this.ID;
	}

	public Integer getID() {
		return ID;
	}

	public int hashCode() {
		return ID;
	}


	public boolean isCentroid() {
		return zone != null;
	}


	public String toString() {
		return "Node " + this.ID;
	}
	
	public int getOrder() {
		return graphOrder;
	}

	public void setForwardStar(Link[] fs) {
		// TODO Auto-generated method stub
		outLinks = fs;
	}

	public void setReverseStar(Link[] rs) {
		// TODO Auto-generated method stub
		inLinks = rs;
	}
	
	public Link[] forwardStar() {
		return outLinks == null? new Link[0] : outLinks;
	}
	
	public Link[] reverseStar() {
		return inLinks;
	}

	public int orderOf(Link l) {
		// TODO Auto-generated method stub
		return l.headIndex();
	}

	public TravelSurveyZone getZone() {
		return zone;
	}

}
