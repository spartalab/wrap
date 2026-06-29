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
package edu.utexas.wrap.distribution;

import java.util.concurrent.atomic.AtomicBoolean;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.PAMap;

/**The second step in the Urban Transportation Modeling System (four-step
 * model). Trip distributors convert a balanced Production-Attraction map
 * into a full origin-destination trip matrix, linking each trip to both
 * a production zone and an attraction zone. The number of trips leaving
 * each production zone and entering each attraction zone must equal the
 * values specified in the input PAMap.
 *
 * <p>Implementations typically use a gravity model or other spatial
 * interaction model to determine the proportion of trips from each
 * origin that travel to each destination.
 * 
 * @author Will Alexander
 * @see GravityDistributor
 * @see ModularGravityDistributor
 */
public interface TripDistributor {

	public int maxIterations();

	public void updateProducerWeights(PAMap map, ImpedanceMatrix impedances, AtomicBoolean converged);

	public void updateAttractorWeights(PAMap map, ImpedanceMatrix impedances, AtomicBoolean converged);

	public AggregatePAMatrix constructMatrix(PAMap map, ImpedanceMatrix impedances);
	
}
