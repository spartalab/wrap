package edu.utexas.wrap.marketsegmentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.balancing.TripBalancer;
import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.FixedSizeDemandMap;
import edu.utexas.wrap.demand.containers.FixedSizePAMap;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.generation.BasicTripGenerator;
import edu.utexas.wrap.generation.GenerationRate;
import edu.utexas.wrap.generation.TripGenerator;
import edu.utexas.wrap.modechoice.FixedProportionSplitter;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.util.AggregatePAMatrixCollector;
import edu.utexas.wrap.util.DepartureArrivalConverter;
import edu.utexas.wrap.util.PassengerVehicleTripConverter;
import edu.utexas.wrap.util.TimeOfDaySplitter;

public class Purpose {
	PurposeModel model;

	public Purpose(Path purposeFile, Graph network) {
		// TODO Auto-generated constructor stub
		model = new PurposeModel(purposeFile, network);
	}

	public Stream<ODMatrix> buildODs(Map<TimePeriod,NetworkSkim> skims) {
		
		Stream<DemandMap>
					productions	= getProductions(),
					attractions	= getAttractions();
		
		PAMap		map			= balance(productions,attractions);
		
		AggregatePAMatrix 
					aggPAMtx	= distribute(map, skims);
		
		Stream<ModalPAMatrix>
					modalPAMtxs	= chooseModes(aggPAMtx);
		
		Stream<ODMatrix>
					odMtxs		= convertToOD(modalPAMtxs);
		
		return odMtxs;
	}

	private Stream<ODMatrix> convertToOD(Stream<ModalPAMatrix> modalPAMtxs) {
		
		Stream<ODMatrix> dailyODs = new PassengerVehicleTripConverter().convert(modalPAMtxs);
		
		Stream<ODMatrix> temporalODs = new TimeOfDaySplitter(
				model.departureRates(),
				model.arrivalRates())
				.split(dailyODs);
		
		return temporalODs;

	}

	private Stream<ModalPAMatrix> chooseModes(AggregatePAMatrix aggregateMtx) {
		return new FixedProportionSplitter(model.modeShares()).split(aggregateMtx);
	}
	
	private AggregatePAMatrix distribute(PAMap map, Map<TimePeriod,NetworkSkim> skims) {
		return model.distributionShares().entrySet().parallelStream()
		.map(entry -> 
			
			model.distributor(
					
					skims.get(entry.getKey()), 
					
					entry.getValue())
			.distribute(map)
		)
		.collect(new AggregatePAMatrixCollector());
	}
	
	private PAMap balance(
			Stream<DemandMap> productions, 
			Stream<DemandMap> attractions) {
		return model.balancer().balance(new FixedSizePAMap(productions,attractions));
	}

	private Stream<DemandMap> getAttractions() {
		TripGenerator generator = model.attractionGenerator();
		return model.attractionSegments()
				.map(segment -> generator.generate(segment));
	}

	private Stream<DemandMap> getProductions() {
		TripGenerator generator = model.productionGenerator();
		return model.productionSegments()
				.map(segment -> generator.generate(segment));
	}

}

class PurposeModel {
	
	Properties properties;
	Graph network;
	
	public PurposeModel(Path purposeFile, Graph network) throws IOException {
		properties = new Properties();
		this.network = network;
		properties.load(Files.newInputStream(purposeFile));
	}

	public TripGenerator productionGenerator() {
		String genName = properties.getProperty("prodGenerator");
		
		if (genName==null) {
			System.err.println("No production generator specified");
		} else if (genName.toLowerCase().equals("basic")) {
			return new BasicTripGenerator(network,getProdRates());
		} else if (genName.toLowerCase().equals("rateproportion")) {
			throw new RuntimeException("Not yet implemented");
		}
		throw new RuntimeException("Not yet implemented");
	}
	
	private Map<MarketSegment, GenerationRate> getProdRates() {
		throw new RuntimeException("Not yet implemented");
	}

	public Stream<MarketSubsegment> productionSegments(){
		throw new RuntimeException("Not yet implemented");
	}
	
	public TripGenerator attractionGenerator() {
		throw new RuntimeException("Not yet implemented");
	}
	
	public Stream<MarketSubsegment> attractionSegments(){
		throw new RuntimeException("Not yet implemented");
	}
	
	public TripBalancer balancer() {
		throw new RuntimeException("Not yet implemented");
	}
	
	public Map<TimePeriod,Double> distributionShares(){
		throw new RuntimeException("Not yet implemented");
	}
	
	public TripDistributor distributor(NetworkSkim skim, Double multiplier) {
		throw new RuntimeException("Not yet inmplemented");
	}
	
	public Map<Mode,Double> modeShares(){
		throw new RuntimeException("Not yet implemented");
	}
	
	public Map<TimePeriod,Double> departureRates(){
		throw new RuntimeException("Not yet implemented");
	}
	
	public Map<TimePeriod,Double> arrivalRates(){
		throw new RuntimeException("Not yet implemented");
	}
}
