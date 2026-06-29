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



/**
 * Optimizes route choices within a traffic assignment by iteratively
 * shifting flow from higher-cost to lower-cost routes. Implementations
 * define the specific equilibration algorithm (e.g., Algorithm B,
 * signalized optimization) and control the per-iteration convergence process.
 *
 * @param <T> the type of assignment container being optimized
 * @author Will Alexander
 * @see AtomicOptimizer
 * @see AlgorithmBOptimizer
 */
public interface AssignmentOptimizer<T extends AssignmentContainer> {
	
	/**
	 * Initializes or resets the optimizer state before a new round of optimization.
	 */
	public void initialize();

}
