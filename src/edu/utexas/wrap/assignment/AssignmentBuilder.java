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

import edu.utexas.wrap.net.Graph;

/**
 * Constructs the internal data structures for an {@link AssignmentContainer}
 * within a given transportation network. This is typically invoked during
 * initialization to set up shortest-path trees, bush topologies, or
 * other routing structures required before demand can be loaded.
 *
 * @param <T> the type of assignment container to build
 * @author Will Alexander
 * @see BushBuilder
 */
public interface AssignmentBuilder<T> {
	
	/**
	 * Builds the internal routing structure for the given container
	 * on the specified network graph.
	 *
	 * @param container the assignment container to populate
	 * @param network the transportation network graph
	 */
	public void buildStructure(T container, Graph network);

}
