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

/**An area of land from and to which trips occur.
 * 
 * A TravelSurveyZone (TSZ) represents a geographic area whose demographics
 * and travel behavior have been surveyed and modeled. TSZs are the fundamental
 * spatial units for trip generation, distribution, and assignment in the
 * four-step model. Each TSZ is associated with:
 * <ul>
 *   <li>A centroid {@link Node} through which all trips enter/exit the network</li>
 *   <li>An {@link AreaClass} characterizing its land use (e.g., CBD, suburban, rural)</li>
 *   <li>An optional parent {@link RegionalAreaAnalysisZone} for regional balancing</li>
 *   <li>A topological order index for efficient matrix storage and lookup</li>
 * </ul>
 * 
 * @author Will Alexander
 * @see AreaClass
 * @see RegionalAreaAnalysisZone
 * @see Demographic
 */
public class TravelSurveyZone {
	private final int nodeID;
	private final int order;
	private RegionalAreaAnalysisZone parent;
	private final AreaClass ac;
	
	/**Create a TSZ with defined ID number, vectorization index, and AreaClass
	 * 
	 * @param nodeID the ID associated with this TSZ and its corresponding Node in graphs
	 * @param order the index of this TSZ in a vectorized listing of all the network's TSZs
	 * @param ac the AreaClass associated with this TSZ
	 */
	public TravelSurveyZone(int nodeID, int order, AreaClass ac) {
		this.nodeID = nodeID;
		this.order = order;
		this.ac = ac;
	}
	
	/**
	 * @return the ID associated with this TSZ and its corresponding Node in graphs
	 */
	public int getID() {
		return nodeID;
	}
	
	public String toString() {
		return "Zone "+this.getID();
	}
	
	/**Attach an RAA to the zone for aggregation purposes
	 * 
	 * This method defines the singular RAA associated with this TSZ
	 * 
	 * @param parent the RAA which contains this TSZ
	 */
	public void setRAA(RegionalAreaAnalysisZone parent) {
		this.parent = parent;
	}
	
	/**
	 * @return the RAA which encapsulates this TSZ
	 */
	public RegionalAreaAnalysisZone getRAA() {
		return parent;
	}

	/**
	 * @return the index of this TSZ in a vectorized representation of the network's TSZs
	 */
	public int getOrder() {
		return order;
	}
	
	/**
	 * @return the AreaClass assigned to this TSZ
	 */
	public AreaClass getAreaClass() {
		return ac;
	}
	
}
