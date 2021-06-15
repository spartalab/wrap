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

import edu.utexas.wrap.net.TravelSurveyZone;

/**A mapping from a TravelSurveyZone to the number of productions
 * and attractions that begin and end, respectively,
 * at that TravelSurveyZone. This is the result of the trip 
 * generation step of the four-step model.
 * 
 * Any PA Map should have a method of retrieving the
 * metadata that will be used in the mode-choice models.
 * 
 * @author William
 *
 */
public interface PAMap {

	/**
	 * @return the TravelSurveyZones from which trips originate
	 */
	public Collection<TravelSurveyZone> getProducers();

	/**
	 * @return the TravelSurveyZones to which trips are attracted
	 */
	public Collection<TravelSurveyZone> getAttractors();

	/**
	 * @param z the TravelSurveyZone to which trips are attracted
	 * @return the number of trips attracted to the TravelSurveyZone
	 */
	public float getAttractions(TravelSurveyZone z);

	/**
	 * @param z the Node from which trips are produced
	 * @return the number of trips produced at the TravelSurveyZone
	 */
	public float getProductions(TravelSurveyZone z);

	/**
	 * @param z the TravelSurveyZone to which trips are attracted
	 * @param amt the amount of trips attracted to the TravelSurveyZone
	 */
	public void putAttractions(TravelSurveyZone z, Float amt);

	/**
	 * @param z the TravelSurveyZone from which trips are produced
	 * @param amt the amount of trips produced at the TravelSurveyZone
	 */
	public void putProductions(TravelSurveyZone z, Float amt);

	
	/**
	 * @return a DemandMap containing all trip productions of this map
	 */
	public DemandMap getProductionMap();
	
	/**
	 * @return a DemandMap containing all trip attractions of this map
	 */
	public DemandMap getAttractionMap();
}
