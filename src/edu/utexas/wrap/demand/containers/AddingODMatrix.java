package edu.utexas.wrap.demand.containers;

import java.util.Collection;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

public class AddingODMatrix implements ODMatrix {

	private final DemandMap[] demandMaps;
	private final TimePeriod tp;
	private final Mode mode;
	private final Collection<TravelSurveyZone> zones;
	
	public AddingODMatrix(Collection<ODMatrix> ods, Mode mode, TimePeriod tp, Collection<TravelSurveyZone> zones) {
		this.mode = mode;
		this.tp = tp;
		this.zones = zones;
		
		demandMaps = new DemandMap[zones.size()];
		zones.parallelStream().forEach(origin ->{
			DemandMap dm = new FixedSizeDemandMap(zones);
			zones.stream().forEach(destination -> dm.put(destination, (float) ods.stream().mapToDouble(od -> od.getDemand(origin, destination)).sum()));
			demandMaps[origin.getOrder()] = dm;
		});
	}
	
	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public float getDemand(TravelSurveyZone origin, TravelSurveyZone destination) {
		return demandMaps[origin.getOrder()].get(destination);
	}

	@Override
	public void put(TravelSurveyZone origin, TravelSurveyZone destination, Float demand) {
		throw new RuntimeException("Read-only matrix");
	}


	@Override
	public TimePeriod timePeriod() {
		return tp;
	}

	@Override
	public Collection<TravelSurveyZone> getZones() {
		return zones;
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone origin) {
		// TODO Auto-generated method stub
		return demandMaps[origin.getOrder()];
	}

}
