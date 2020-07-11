package edu.utexas.wrap.marketsegmentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.balancing.TripBalancer;
import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.FixedSizePAMap;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.generation.BasicTripGenerator;
import edu.utexas.wrap.generation.GeneralGenerationRate;
import edu.utexas.wrap.generation.GenerationRate;
import edu.utexas.wrap.generation.TripGenerator;
import edu.utexas.wrap.modechoice.FixedProportionSplitter;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.util.AggregatePAMatrixCollector;
import edu.utexas.wrap.util.PassengerVehicleTripConverter;
import edu.utexas.wrap.util.TimeOfDaySplitter;

public class Purpose {
	PurposeModel model;

	public Purpose(Path purposeFile, Graph network) throws IOException {
		model = new PurposeModel(purposeFile, network);
	}

	public Stream<ODProfile> buildODs(Collection<NetworkSkim> skims) {
		
		Stream<DemandMap>
					productions	= getProductions(),
					attractions	= getAttractions();
		
		PAMap		map			= balance(productions,attractions);
		
		AggregatePAMatrix 
					aggPAMtx	= distribute(map);
		
		Stream<ModalPAMatrix>
					modalPAMtxs	= chooseModes(aggPAMtx);
		
		Stream<ODProfile>
					odProfiles	= convertToOD(modalPAMtxs);
		
		return odProfiles;
	}

	private Stream<ODProfile> convertToOD(Stream<ModalPAMatrix> modalPAMtxs) {
		
		//One daily ODMatrix per mode
		Stream<ODMatrix> dailyODs = new PassengerVehicleTripConverter().convert(modalPAMtxs);
		
		//One ODProfile (consisting of multiple TimePeriods' ODMatrices) per mode
		Stream<ODProfile> temporalODs = new TimeOfDaySplitter(
				model.departureRates(),
				model.arrivalRates())
				.split(dailyODs);
		
		return temporalODs;

	}

	private Stream<ModalPAMatrix> chooseModes(AggregatePAMatrix aggregateMtx) {
		return new FixedProportionSplitter(model.modeShares()).split(aggregateMtx);
	}
	
	private AggregatePAMatrix distribute(PAMap map) {
		return model.distributionShares().entrySet().parallelStream()
		.map(entry -> 
			
			model.distributor(
					entry.getKey(), 
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
	
	private Map<MarketSubsegment, GenerationRate> getProdRates() {
		productionSegments().collect(Collectors.toMap(Function.identity(), 
				segment -> {
					String key = "prodRate";
					for (int i = 0; i<segment.depth()-1;i++) {
						key += "."+segment.index(i);
					}
					return new GeneralGenerationRate(Double.parseDouble(properties.getProperty(key)));

				}
				));
		

		throw new RuntimeException("Not yet implemented");
	}

	public Stream<MarketSubsegment> productionSegments(){
		long keyDepth = properties.keySet().stream()
				.filter(key -> ((String) key).startsWith("prodRate")).count();

		List<String> keys = LongStream.range(0,keyDepth)
				.mapToObj(idx -> properties.getProperty("prodKey."+idx))
				.collect(Collectors.toList());
		
		
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
	
	public Map<NetworkSkim,Float> distributionShares(){
		throw new RuntimeException("Not yet implemented");
	}
	
	public TripDistributor distributor(NetworkSkim skim, Float multiplier) {
		throw new RuntimeException("Not yet inmplemented");
	}
	
	public Map<Mode,Float> modeShares(){
		throw new RuntimeException("Not yet implemented");
	}
	
	public Map<TimePeriod,Float> departureRates(){
		throw new RuntimeException("Not yet implemented");
	}
	
	public Map<TimePeriod,Float> arrivalRates(){
		throw new RuntimeException("Not yet implemented");
	}
}
