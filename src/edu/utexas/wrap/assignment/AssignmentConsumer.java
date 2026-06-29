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

import java.io.IOException;

import edu.utexas.wrap.net.Graph;

/**
 * Consumes (persists or deallocates) the structure of an {@link AssignmentContainer}
 * after it has been processed. Implementations may write the container state to disk,
 * release memory, or perform other cleanup operations.
 *
 * @param <C> the type of assignment container to consume
 * @author Will Alexander
 * @see BushWriter
 * @see BushForgetter
 */
public interface AssignmentConsumer<C extends AssignmentContainer> {

	/**
	 * Consumes the given container's structure, typically by writing it
	 * to persistent storage or releasing its in-memory representation.
	 *
	 * @param container the assignment container to consume
	 * @param network the transportation network graph
	 * @throws IOException if an I/O error occurs during consumption
	 */
	public void consumeStructure(C container, Graph network) throws IOException;
}
