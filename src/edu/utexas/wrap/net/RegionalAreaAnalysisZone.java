package edu.utexas.wrap.net;

import java.util.Set;

/**An aggregator that combines multiple TravelSurveyZones into a region for balancing
 * 
 * Some trip purposes may require that trips be balanced not just at a network-wide
 * level but at a regional level as well. To do this, regions are defined as a set
 * of TravelSurveyZones, and the total trips produced by these zones must match the 
 * total number of trips attracted to these zones.
 * 
 * @author William
 *
 */
public class RegionalAreaAnalysisZone {
	private final Set<TravelSurveyZone> tszs;
	private AreaClass type;
	
	public RegionalAreaAnalysisZone(Set<TravelSurveyZone> zones) {
		tszs = zones;
	}
	
	public Set<TravelSurveyZone> getTSZs(){
		return tszs;
	}
	
	public AreaClass getAreaType() {
		return type;
	}
}
