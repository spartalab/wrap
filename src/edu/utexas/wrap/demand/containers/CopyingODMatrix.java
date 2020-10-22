package edu.utexas.wrap.demand.containers;

import java.util.Collection;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

public class CopyingODMatrix implements ODMatrix {
	private final Mode mode;
	private final DemandMap[] demandMaps;
	private final Float vot;
	private final Collection<TravelSurveyZone> zones;
	private final TimePeriod tp;

	public CopyingODMatrix(ODMatrix parent, Float vot, Mode mode, TimePeriod tp) {
		this.mode = mode;
		this.tp = tp;
		this.vot = vot;
		this.zones = parent.getZones();
		demandMaps = new DemandMap[zones.size()];
		
		zones.stream().forEach(orig -> {
			DemandMap dm = new FixedSizeDemandMap(zones);
			zones.stream().forEach(dest -> dm.put(dest, parent.getDemand(orig, dest)));
			demandMaps[orig.getOrder()] = dm;
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
		// TODO Auto-generated method stub
		throw new RuntimeException("Read-only matrix");
	}

	@Override
	public Float getVOT() {
		return vot;
	}

	@Override
	public void setVOT(float VOT) {
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
		return demandMaps[origin.getOrder()];
	}

}
