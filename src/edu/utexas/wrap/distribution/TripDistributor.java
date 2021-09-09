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

/**The second step in the Urban Transportation Modeling System
 * a.k.a. the four-step model, trip distributors use some means to
 * convert a Production-Attraction map to a corresponding matrix
 * where the trips are linked to both a producer and an attractor.
 * The trips leaving each producer and entering each attractor should
 * be the same as the number of trips generated and stored in the 
 * given PA Map
 * @author William
 *
 */
public interface TripDistributor {

	public int maxIterations();

	public void updateProducerWeights(PAMap map, ImpedanceMatrix impedances, AtomicBoolean converged);

	public void updateAttractorWeights(PAMap map, ImpedanceMatrix impedances, AtomicBoolean converged);

	public AggregatePAMatrix constructMatrix(PAMap map, ImpedanceMatrix impedances);
	
}
