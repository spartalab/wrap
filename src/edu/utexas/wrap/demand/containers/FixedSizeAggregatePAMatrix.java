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
package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.Map;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMatrix;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedSizeAggregatePAMatrix implements AggregatePAMatrix {

//	private Graph g;
	private final Collection<TravelSurveyZone> zones;
	private DemandMap[] matrix;

	public FixedSizeAggregatePAMatrix(Collection<TravelSurveyZone> zones) {
		this.zones = zones;
		matrix = new DemandMap[zones.size()];
		zones.forEach(zone -> matrix[zone.getOrder()] = new FixedSizeDemandMap(zones));
	}

	public FixedSizeAggregatePAMatrix(PAMatrix hbwSum, Map<TravelSurveyZone, Float> map) {
		zones = hbwSum.getZones();
		matrix = new DemandMap[zones.size()];
		hbwSum.getProducers().parallelStream().forEach(prod ->{
			matrix[prod.getOrder()] = new FixedMultiplierPassthroughDemandMap(hbwSum.getDemandMap(prod),map.getOrDefault(prod,0.0f));
		});
	}

	/**
	 * Insert the demand map for a given node
	 * @param i the Node from which there is demand
	 * @param d the map of demand from the given Node to other Nodes
	 */
	public void putDemandMap(TravelSurveyZone i, DemandMap d) {
		matrix[i.getOrder()] = d;
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMatrix#getDemand(edu.utexas.wrap.net.Node, edu.utexas.wrap.net.Node)
	 */
	public float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
		return matrix[producer.getOrder()] == null ? 0.0F : matrix[producer.getOrder()].get(attractor);
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMatrix#getGraph()
	 */
	public Collection<TravelSurveyZone> getZones(){
		return zones;
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMatrix#put(edu.utexas.wrap.net.Node, edu.utexas.wrap.net.Node, java.lang.Float)
	 */
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		matrix[producer.getOrder()].put(attractor,demand);
		
	}

	public DemandMap getDemandMap(TravelSurveyZone producer) {
		return matrix[producer.getOrder()];
	}

	public Collection<TravelSurveyZone> getProducers() {
		return zones;
	}

}
