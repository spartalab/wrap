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
package edu.utexas.wrap.net;

/**Interface for defining vectors of demographic data for TravelSurveyZones
 * 
 * For any TravelSurveyZone, multiple demographics, such as population demos,
 * employment demos, etc. may exist. For each demographic associated with a
 * TravelSurveyZone, multiple elements may be required to tell the full story
 * of the people (or other elements) located within the TSZ. Therefore, a demo
 * may provide a vector of arbitrary length for a given demographic.
 * 
 * For example, suppose a demographic is created which deals with the households
 * of each zone, based on the number of automobiles owned. Such a demographic may
 * provide a vector with each element indicating the number of households which
 * own the number of cars specified by the index. That is, a vector such as:
 * 
 * [100.0,250.0,50.0,12.0]
 * 
 * would indicate that, for the TravelSurveyZone which returned this vector, there
 * are 100 households which do not own a car, 250 which own one car, 50 which own
 * two cars, and 12 that own 3 cars.
 * 
 * @author William
 *
 */
public interface Demographic {
	
	//TODO provide a size() method for retrieval of vector length
	//TODO enforce size consistency

	/**
	 * @param zone an area in the network whose demographic vector should be returned
	 * @return the demographic vector data associated with this zone for this demographic
	 */
	public Float[] valueFor(TravelSurveyZone zone);
	
}
