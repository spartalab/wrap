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

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.TravelSurveyZone;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rishabh
 *
 * An aggregate production/attraction map implementation
 * Created in the trip generation process, this object
 * stores the total trips produced and attracted by a specified Node
 */
public class AggregatePAHashMap implements PAMap {

    private Map<TravelSurveyZone, Float> attractors;
    private Map<TravelSurveyZone, Float> producers;

    public AggregatePAHashMap() {
        attractors = new ConcurrentHashMap<TravelSurveyZone,Float>();
        producers = new ConcurrentHashMap<TravelSurveyZone,Float>();
    }
    
    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#getProducers()
     */
    public Set<TravelSurveyZone> getProducers() {
        return attractors.keySet();
    }

    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#getAttractors()
     */
    public Set<TravelSurveyZone> getAttractors() {
        return producers.keySet();
    }

    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#getAttractions(edu.utexas.wrap.net.Node)
     */
    public float getAttractions(TravelSurveyZone z) {
        return attractors.getOrDefault(z, 0.0f);
    }

    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#getProductions(edu.utexas.wrap.net.Node)
     */
    public float getProductions(TravelSurveyZone z) {
        return producers.getOrDefault(z, 0.0f);
    }

    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#putAttractions(edu.utexas.wrap.net.Node, java.lang.Float)
     */
    public void putAttractions(TravelSurveyZone z, Float amt) {
        attractors.put(z, amt);
    }

    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#putProductions(edu.utexas.wrap.net.Node, java.lang.Float)
     */
    public void putProductions(TravelSurveyZone z, Float amt) {
        producers.put(z, amt);
    }

	public DemandMap getProductionMap() {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

	public DemandMap getAttractionMap() {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}
}
