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

/**A NetworkSkim implementation which stores the full matrix in memory vectors
 * 
 * This implementation provides a re-writable matrix which stores cost data as
 * float vectors.
 * 
 * @author William
 *
 */
public class FixedSizeNetworkSkim implements NetworkSkim {
	
	float[][] skimData;
	private String id;
	/**Construct a fixed skim from pre-existing matrix
	 * @param skim
	 */
	public FixedSizeNetworkSkim(String name) {
		id = name;
	}
	
	/**Create an empty matrix of a given n*n size
	 * @param numZones the number of zones n whose data will be stored in this skim
	 */
	public FixedSizeNetworkSkim(String id, int numZones) {
		this.id = id;
		skimData = new float[numZones][numZones];
	}

	public float getCost(TravelSurveyZone producer, TravelSurveyZone attractor) {
		return skimData[producer.getOrder()][attractor.getOrder()];
	}
	
	/**Overwrite this skim's cost for a given zone pair
	 * @param orig the origin of trips for this skim cost
	 * @param dest the destination of trips for this skim cost
	 * @param cost the cost (defined arbitrarily) of travel from the origin to the destination
	 */
	public void putCost(TravelSurveyZone orig, TravelSurveyZone dest, float cost) {
		skimData[orig.getOrder()][dest.getOrder()] = cost;
	}

	@Override
	public String toString() {
		return id;
	}


}
