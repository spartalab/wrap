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
package edu.utexas.wrap.marketsegmentation;

import java.util.Collection;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.AggregatePAMatrixProvider;
import edu.utexas.wrap.demand.DailyODMatrixProvider;
import edu.utexas.wrap.demand.ModalPAMatrixProvider;
import edu.utexas.wrap.demand.ODProfileProvider;
import edu.utexas.wrap.demand.PAMapProvider;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.TravelSurveyZone;

/**An interface for providing a complete implementation of the Urban
 * Transportation Modeling System (UTMS) demand pipeline.
 * 
 * A Purpose represents a reason for travel (e.g., home-based work,
 * home-based shopping, non-home-based) and encapsulates the full
 * four-step modeling chain for that trip type. Implementations must
 * provide:
 * <ol>
 *   <li>A balanced PAMap (trip generation + balancing)</li>
 *   <li>An AggregatePAMatrix (trip distribution)</li>
 *   <li>Modal PA matrices (mode choice)</li>
 *   <li>Daily OD matrices (PA-to-OD conversion)</li>
 *   <li>OD profiles (time-of-day factoring)</li>
 * </ol>
 * 
 * @author Will Alexander
 * @see BasicPurpose
 * @see SurrogatePurpose
 * @see Market
 */
public interface Purpose extends 
							ODProfileProvider, 
							DailyODMatrixProvider, 
							ModalPAMatrixProvider, 
							AggregatePAMatrixProvider, 
							PAMapProvider
							 {
	
	public double personTrips();

	public Market getMarket();

	public Collection<TripDistributor> getDistributors();

	public NetworkSkim getNetworkSkim(TripDistributor distributor);

	public FrictionFactorMap getFrictionFunction(TripDistributor distributor);

	public Collection<TravelSurveyZone> getZones();
	
	public Float getVOT(TimePeriod tp);
	
	public Float getVOT(Mode m, TimePeriod tp);

};

