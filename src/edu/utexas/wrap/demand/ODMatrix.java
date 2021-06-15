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
package edu.utexas.wrap.demand;

import java.util.Collection;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

/**A map from an origin-destination pair to the number
 * of <b>vehicle</b>-trips made. This is different from the PA
 * matrix in that it may account for multiple person-trips
 * inside a single vehicle-trip, i.e. passengers in a car
 * or bus.
 * 
 * @author William
 *
 */
public interface ODMatrix {
	
	/**
	 * @return the Mode associated with this matrix 
	 */
	public Mode getMode();

	/** 
	 * @param origin the Node from which trips originate
	 * @param destination the Node to which trips travel
	 * @return the demand from the origin to the destination
	 */
	public float getDemand(TravelSurveyZone origin, TravelSurveyZone destination);
	
	/**
	 * @param origin the Node from which trips originate
	 * @param destination the Node to which trips travel
	 * @param demand the amount of trips from the origin to the destination
	 */
	public void put(TravelSurveyZone origin, TravelSurveyZone destination, Float demand);

	/**
	 * @return the TimePeriod in which trips in this matrix occur
	 */
	public TimePeriod timePeriod();
	
	/**
	 * @return the TravelSurveyZones which serve as the origin or destination of trips in this matrix
	 */
	public Collection<TravelSurveyZone> getZones();
	
	/**
	 * @param origin a TravelSurveyZone whose trips should be returned
	 * @return a DemandMap containing all trips from the given TravelSurveyZone to all zones
	 */
	public DemandMap getDemandMap(TravelSurveyZone origin);
}
