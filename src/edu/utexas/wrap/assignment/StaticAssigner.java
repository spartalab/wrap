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

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.net.Link;

/**
 * A specialization of {@link Assigner} for static (time-independent) traffic
 * assignment. Static assigners operate within a single {@link TimePeriod} and
 * seek a User Equilibrium solution where no traveler can unilaterally reduce
 * their travel cost by switching routes.
 *
 * <p>In addition to the base Assigner capabilities, a StaticAssigner exposes
 * configuration details such as the link type used for delay functions, the
 * maximum number of iterations, and tolling policies.
 *
 * @param <C> the type of {@link AssignmentContainer} used by this assigner
 * @author Will Alexander
 * @see BasicStaticAssigner
 */
public interface StaticAssigner<C extends AssignmentContainer> extends Assigner<C> {

	/**
	 * Returns the time period for which this assigner performs assignment.
	 *
	 * @return the {@link TimePeriod} of this assignment
	 */
	public TimePeriod getTimePeriod();
	
	/**
	 * Returns the concrete link class used to model delay functions
	 * (e.g., BPR links or conic delay links).
	 *
	 * @return the {@link Link} subclass used by this assigner's network
	 */
	public Class<? extends Link> getLinkType();
	
	/**
	 * Returns the maximum number of iterations before termination,
	 * regardless of convergence.
	 *
	 * @return the iteration limit
	 */
	public Integer maxIterations();
	
	/**
	 * Sets the tolling policy function, which maps each link to a
	 * monetary toll value. This toll is incorporated into route cost
	 * calculations during assignment.
	 *
	 * @param policy a function returning the toll for a given link
	 */
	public void setTollingPolicy(ToDoubleFunction<Link> policy);
	
}
