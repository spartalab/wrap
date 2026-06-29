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

import java.nio.file.Path;
import java.util.Collection;
import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.NetworkSkim;

/**
 * The primary interface for traffic assignment in the four-step travel demand model.
 * An Assigner is responsible for loading origin-destination demand onto a transportation
 * network by distributing trips across available routes. Implementations define the
 * specific equilibrium algorithm used (e.g., bush-based, path-based, or link-based
 * approaches) and manage the iterative convergence process.
 *
 * <p>The assignment process consists of:
 * <ol>
 *   <li>Initialization: loading OD profiles and constructing network structures</li>
 *   <li>Iteration: repeatedly optimizing route choices until convergence</li>
 *   <li>Evaluation: measuring the quality of the current assignment (e.g., relative gap)</li>
 *   <li>Output: producing link flows and updated network skims</li>
 * </ol>
 *
 * @param <C> the type of {@link AssignmentContainer} used by this assigner
 *            (e.g., Bush for bush-based methods, Path for path-based methods)
 * @author Will Alexander
 * @see StaticAssigner
 * @see BasicStaticAssigner
 */
public interface Assigner<C extends AssignmentContainer> {
	
	/**
	 * Computes a network skim (zone-to-zone impedance matrix) from the current
	 * assignment state using the specified cost function.
	 *
	 * @param id a unique identifier for this skim (e.g., "time", "distance")
	 * @param function the link cost function used to evaluate impedance
	 * @return a {@link NetworkSkim} containing zone-to-zone costs
	 */
	public NetworkSkim getSkim(String id, ToDoubleFunction<Link> function);

	/**
	 * Writes the current link flows and travel times to the specified output file.
	 *
	 * @param outputFile the path to which flow data should be written
	 */
	public void outputFlows(Path outputFile);
	
	/**
	 * Initializes the assigner with the given set of OD profiles. This method
	 * constructs the network, disaggregates demand matrices by mode and value
	 * of time, and prepares internal data structures for iteration.
	 *
	 * @param profiles the collection of {@link ODProfile} instances to be assigned
	 */
	public void initialize(Collection<ODProfile> profiles);
	
	/**
	 * Returns a progress estimate (between 0 and 1) based on the current
	 * evaluation metric and number of completed iterations.
	 *
	 * @param currentValue the current objective function or gap value
	 * @param numIterations the number of iterations completed so far
	 * @return a value between 0.0 (not started) and 1.0 (converged or max iterations reached)
	 */
	public double getProgress(double currentValue, int numIterations);
	
	/**
	 * Returns the set of travel modes handled by this assigner.
	 *
	 * @return the collection of {@link Mode} values assigned by this instance
	 */
	public Collection<Mode> assignedModes();
	
	/**
	 * Returns the transportation network graph used by this assigner.
	 *
	 * @return the {@link Graph} on which assignment is performed
	 */
	public Graph getNetwork();

	/**
	 * Returns the evaluator used to assess convergence of the assignment.
	 *
	 * @return the {@link AssignmentEvaluator} for this assigner
	 */
	public AssignmentEvaluator<C> getEvaluator();

	/**
	 * Returns the collection of assignment containers (e.g., bushes or paths)
	 * managed by this assigner.
	 *
	 * @return the collection of {@link AssignmentContainer} instances
	 */
	public Collection<C> getContainers();

	/**
	 * Returns the optimizer responsible for improving route choices during iteration.
	 *
	 * @return the {@link AssignmentOptimizer} for this assigner
	 */
	AssignmentOptimizer<C> getOptimizer();
	
}
