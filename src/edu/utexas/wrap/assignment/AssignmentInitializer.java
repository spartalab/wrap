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

import java.util.Collection;

import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.net.Graph;

/**
 * Initializes assignment containers for a given network. This interface
 * is responsible for creating the initial set of containers (e.g., bushes
 * rooted at each origin zone) and loading demand matrices into them.
 *
 * @param <T> the type of assignment container to initialize
 * @author Will Alexander
 * @see BushInitializer
 */
public interface AssignmentInitializer<T extends AssignmentContainer> {
	
	/**
	 * Creates and returns the full set of initialized containers for the
	 * given network. Should be called after all demand has been added via
	 * {@link #add(Graph, ODMatrix, Float)}.
	 *
	 * @param network the transportation network graph
	 * @return the collection of initialized assignment containers
	 */
	public Collection<T> initializeContainers(Graph network);
	
	/**
	 * Adds an OD matrix with an associated value of time to the initializer.
	 * Multiple matrices may be added before containers are initialized.
	 *
	 * @param network the transportation network graph
	 * @param matrix the origin-destination demand matrix
	 * @param vot the value of time for trips in this matrix ($/hr)
	 */
	public void add(Graph network, ODMatrix matrix, Float vot);
}
