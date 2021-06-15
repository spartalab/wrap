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
package edu.utexas.wrap.assignment.bush;

import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

/** A BackVector is the object through which all
 * Links flowing into a Node from a Bush are enumerated
 * @author William
 *
 */
public interface BackVector {

	/**
	 * @return the link along the shortest path back to the origin
	 */
	public Link getShortLink();
	
	/**
	 * @return the link along the longest path back to the origin
	 */
	public Link getLongLink();
	
	/**
	 * @return the node where this Backvector leads
	 */
	public Node getHead();
}
