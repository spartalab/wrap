package edu.utexas.wrap.demand;


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class ODPassthroughMatrix implements ODMatrix {
	
	private ModalPAMatrix base;
	
	public ODPassthroughMatrix(ModalPAMatrix baseMatrix) {
		base = baseMatrix;
	}

	@Override
	public Mode getMode() {
		return base.getMode();
	}

	@Override
	public Float getDemand(TravelSurveyZone origin, TravelSurveyZone destination) {
		return base.getDemand(origin, destination);
	}

	@Override
	public void put(TravelSurveyZone origin, TravelSurveyZone destination, Float demand) {
		base.put(origin, destination, demand);
	}

	@Override
	public Graph getGraph() {
		return base.getGraph();
	}

	@Override
	public void write(Path outputOD) {
		
		try (BufferedWriter out = Files.newBufferedWriter(outputOD, StandardOpenOption.CREATE)){
			base.getProducers().parallelStream().forEach( orig -> {
						DemandMap map = base.getDemandMap(orig);
						map.getZones().parallelStream().forEach(dest ->{
							try {
								out.write(orig.getNode().getID()+","+dest.getNode().getID()+","+map.get(dest));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						});
					});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
