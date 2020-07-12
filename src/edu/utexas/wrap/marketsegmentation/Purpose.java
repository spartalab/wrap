package edu.utexas.wrap.marketsegmentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.balancing.TripBalancer;
import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.AggregatePAMatrixProvider;
import edu.utexas.wrap.demand.DailyODMatrixProvider;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ModalPAMatrixProvider;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.demand.ODProfileProvider;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.PAMapProvider;
import edu.utexas.wrap.demand.containers.FixedMultiplierPassthroughPAMap;
import edu.utexas.wrap.demand.containers.FixedSizePAMap;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.generation.BasicTripGenerator;
import edu.utexas.wrap.generation.GeneralGenerationRate;
import edu.utexas.wrap.generation.GenerationRate;
import edu.utexas.wrap.generation.TripGenerator;
import edu.utexas.wrap.modechoice.FixedProportionSplitter;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.modechoice.TripInterchangeSplitter;
import edu.utexas.wrap.net.Demographic;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.util.AggregatePAMatrixCollector;
import edu.utexas.wrap.util.PassengerVehicleTripConverter;
import edu.utexas.wrap.util.TimeOfDaySplitter;

public interface Purpose extends 
							ODProfileProvider, 
							DailyODMatrixProvider, 
							ModalPAMatrixProvider, 
							AggregatePAMatrixProvider, 
							PAMapProvider {

							
};

class BasicPurpose implements Purpose {
	
	private final Properties properties;
	private final Graph network;
	private final PAMap paMap;
	
	private Map<String,NetworkSkim> skims;
	
	public BasicPurpose(Path purposeFile, Graph network, Map<String,Demographic> demographics) throws IOException {

		this.network = network;

		properties = new Properties();
		properties.load(Files.newInputStream(purposeFile));
		
		
		Demographic 
		productionDemographic = demographics.get(properties.get("prodDemographic")), 
		attractionDemographic = demographics.get(properties.get("attrDemographic"));
		
		
		TripGenerator 	producer = productionGenerator(),
				attractor = attractionGenerator();

		
		paMap = balancer().balance(
				new FixedSizePAMap(
						producer.generate(productionDemographic),
						attractor.generate(attractionDemographic)
						)
				);
	}

	private TripGenerator productionGenerator() {
		switch (properties.getProperty("prodGenerator")) {
		case "basic":
			return new BasicTripGenerator(network,productionRates());
		case "rateProportion":
			throw new RuntimeException("Not yet implemented");
		default:
			throw new RuntimeException("Unknown prodGenerator type: "+properties.getProperty("prodGenerator"));
		}
	}
	
	private GenerationRate[] productionRates() {
		throw new RuntimeException("Not yet implemented"); 
	}
	
	
	private TripGenerator attractionGenerator() {
		switch (properties.getProperty("attrGenerator")) {
		case "basic":
			return new BasicTripGenerator(network,attractionRates());
		case "rateProportion":
			throw new RuntimeException("Not yet implemented");
		default:
			throw new RuntimeException("Unknown prodGenerator type: "+properties.getProperty("attrGenerator"));
		}	}
	
	private GenerationRate[] attractionRates() {
		throw new RuntimeException("Not yet implemented");
	}
	
	
	private TripBalancer balancer() {
		throw new RuntimeException("Not yet implemented");
	}
	
	
	
	private Map<String,Float> distributionShares(){
		throw new RuntimeException("Not yet implemented");
	}
	
	private TripDistributor distributor(String skim, Float multiplier) {
		throw new RuntimeException("Not yet inmplemented");
	}
	
	private Map<Mode,Float> modeShares(){
		Stream.of(Mode.values())
		.filter(mode -> properties.containsKey("modeChoice.proportion."+mode.toString()))
		.collect(
				Collectors.toMap(
						Function.identity(),
						mode -> Float.parseFloat("modeChoice.proportion."+mode.toString())
						)
				);
		throw new RuntimeException("Not yet implemented");
	}
	
	private TripInterchangeSplitter modeSplitter() {
		
		return new FixedProportionSplitter(modeShares());
	}
	
	private PassengerVehicleTripConverter vehicleConverter() {
		return new PassengerVehicleTripConverter();
	}
	
	private Map<TimePeriod,Float> departureRates(){
		return Stream.of(TimePeriod.values())
				.filter(tp -> properties.containsKey(tp.toString()))
				.collect(
						Collectors.toMap(
								Function.identity(),
								tp -> Float.parseFloat(properties.getProperty("depRate."+tp.toString()))
								)
						);
	}

	private Map<TimePeriod,Float> arrivalRates(){
		return Stream.of(TimePeriod.values())
				.filter(tp -> properties.containsKey(tp.toString()))
				.collect(
						Collectors.toMap(
								Function.identity(), 
								tp -> Float.parseFloat(properties.getProperty("arrRate."+tp.toString()))
								)
						);
	}

	private TimeOfDaySplitter timeOfDaySplitter(){
		return new TimeOfDaySplitter(departureRates(), arrivalRates());
	}

	
	
	
	
	@Override
	public Stream<ODMatrix> getDailyODMatrices() {
		return vehicleConverter().convert(getModalPAMatrices());
	}

	@Override
	public Stream<ModalPAMatrix> getModalPAMatrices() {
		return modeSplitter().split(getAggregatePAMatrix());
	}

	@Override
	public AggregatePAMatrix getAggregatePAMatrix() {
		return distributionShares().entrySet().parallelStream()
		.map(entry -> 
			
			distributor(
					entry.getKey(), 
					entry.getValue())
			.distribute(getPAMap())
		)
		.collect(new AggregatePAMatrixCollector());
	}

	@Override
	public PAMap getPAMap() {
		return paMap;
	}

	@Override
	public Stream<ODProfile> getODProfiles() {
		return timeOfDaySplitter().split(getDailyODMatrices());
	}

	public void updateSkims(Map<String,NetworkSkim> skims) {
		this.skims = skims;
	}
}
