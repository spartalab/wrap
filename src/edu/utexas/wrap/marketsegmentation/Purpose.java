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
import edu.utexas.wrap.balancing.Attr2ProdProportionalBalancer;
import edu.utexas.wrap.balancing.Prod2AttrProportionalBalancer;
import edu.utexas.wrap.balancing.TripBalancer;
import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.AggregatePAMatrixProvider;
import edu.utexas.wrap.demand.DailyODMatrixProvider;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ModalPAMatrixProvider;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.demand.ODProfileProvider;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.PAMapProvider;
import edu.utexas.wrap.demand.containers.AggregateFixedMultiplierPassthroughMatrix;
import edu.utexas.wrap.demand.containers.PAPassthroughMap;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.distribution.GravityDistributor;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.generation.BasicTripGenerator;
import edu.utexas.wrap.generation.AreaClassGenerationRate;
import edu.utexas.wrap.generation.GeneralGenerationRate;
import edu.utexas.wrap.generation.GenerationRate;
import edu.utexas.wrap.generation.TripGenerator;
import edu.utexas.wrap.modechoice.FixedProportionSplitter;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.modechoice.TripInterchangeSplitter;
import edu.utexas.wrap.net.Demographic;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.TravelSurveyZone;
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
	
//	private final Graph network;
	private final Collection<TravelSurveyZone> zones;
	private final PAMap paMap;
	private final Map<String,FrictionFactorMap> frictFacts;
	
	private Map<String,NetworkSkim> skims;

	public BasicPurpose(
			Path purposeFile, 
			Collection<TravelSurveyZone> zones, 
			Map<String,Demographic> demographics,
			Map<String,FrictionFactorMap> frictFacts
			) throws IOException {

//		this.network = network;
		this.frictFacts = frictFacts;
		this.zones = zones;
		properties = new Properties();
		properties.load(Files.newInputStream(purposeFile));
		
		
		Demographic 
		productionDemographic = demographics.get(properties.get("prodDemographic")), 
		attractionDemographic = demographics.get(properties.get("attrDemographic"));
		
		
		TripGenerator 	producer = productionGenerator(),
				attractor = attractionGenerator();

		
		paMap = balancer().balance(
				new PAPassthroughMap(
						producer.generate(productionDemographic),
						attractor.generate(attractionDemographic)
						)
				);
	}

	
	
	private TripGenerator productionGenerator() {
		switch (properties.getProperty("prodGenerator")) {
		case "basic":
			return new BasicTripGenerator(zones,productionRates());
		case "rateProportion":
			throw new RuntimeException("Not yet implemented");
		default:
			throw new RuntimeException("Unknown prodGenerator type: "+properties.getProperty("prodGenerator"));
		}
	}
	
	private GenerationRate[] productionRates() {
		switch (properties.getProperty("prodType")) {
		
		case "basic":
			return Stream.of(properties.getProperty("prodRate").split(","))
			.map(arg -> Float.parseFloat(arg))
			.map(flt -> new GeneralGenerationRate(flt))
			.toArray(GenerationRate[]::new);
			
		case "area":
			return Stream.of(IndustryClass.values())
			.map(ic ->
				Stream.of(properties.getProperty("prodRate."+ic.toString()).split(","))
				.mapToDouble(Double::parseDouble)
				.toArray()
			)
			.map(AreaClassGenerationRate::new)
			.toArray(GenerationRate[]::new);
			
		default:
			throw new RuntimeException("Not yet implemented"); 
		}
	}
	
	
	private TripGenerator attractionGenerator() {
		switch (properties.getProperty("attrGenerator")) {
		case "basic":
			return new BasicTripGenerator(zones,attractionRates());
		case "rateProportion":
			throw new RuntimeException("Not yet implemented");
		default:
			throw new RuntimeException("Unknown prodGenerator type: "+properties.getProperty("attrGenerator"));
		}	}
	
	private GenerationRate[] attractionRates() {
		switch (properties.getProperty("attrType")) {
		
		case "basic":
			return Stream.of(properties.getProperty("attrRate").split(","))
			.map(arg -> Float.parseFloat(arg))
			.map(flt -> new GeneralGenerationRate(flt))
			.toArray(GenerationRate[]::new);
			
		case "area":
			return Stream.of(IndustryClass.values())
			.map(ic ->
				Stream.of(properties.getProperty("attrRate."+ic.toString()).split(","))
				.mapToDouble(Double::parseDouble)
				.toArray()
			)
			.map(AreaClassGenerationRate::new)
			.toArray(GenerationRate[]::new);
			
		default:
			throw new RuntimeException("Not yet implemented"); 
		}
	}
	
	
	private TripBalancer balancer() {
		switch (properties.getProperty("balancer.class")) {
		
		case "prodProportional":
			return new Prod2AttrProportionalBalancer(null);
		
		case "attrProportional":
			return new Attr2ProdProportionalBalancer();
		
		default:
			throw new RuntimeException("Not yet implemented");
		}
		
	}
	
	
	
	private FrictionFactorMap frictionFactors(String skimID) {
		return frictFacts.get(skimID);
	}
	
	private Map<String,Float> distributionShares(){
		return Stream.of(properties.getProperty("distrib.ids").split(","))
		.collect(
				Collectors.toMap(
						Function.identity(), 
						id -> Float.parseFloat(properties.getProperty("distrib."+id+".split"))
						)
				);
	}
	
	private TripDistributor distributor(String skimID) {
		return new GravityDistributor(
				zones, 
				skims.get(skimID), 
				frictionFactors(properties.getProperty("distrib."+skimID+".frictFacts")));
	}
	
	
	
	private Map<Mode,Float> modeShares(){
		return Stream.of(Mode.values())
		.filter(mode -> properties.containsKey("modeChoice.proportion."+mode.toString()))
		.collect(
				Collectors.toMap(
						Function.identity(),
						mode -> Float.parseFloat(properties.getProperty("modeChoice.proportion."+mode.toString()))
						)
				);
	}
	
	private TripInterchangeSplitter modeSplitter() {
		
		return new FixedProportionSplitter(modeShares());
	}
	
	
	
	private PassengerVehicleTripConverter vehicleConverter() {
		return new PassengerVehicleTripConverter(getVOT());
	}
	
	
	
	private Map<TimePeriod,Float> departureRates(){
		return Stream.of(TimePeriod.values())
				.filter(tp -> properties.containsKey("depRate."+tp.toString()))
				.collect(
						Collectors.toMap(
								Function.identity(),
								tp -> Float.parseFloat(properties.getProperty("depRate."+tp.toString()))
								)
						);
//		return ret;
	}

	private Map<TimePeriod,Float> arrivalRates(){
		return Stream.of(TimePeriod.values())
				.filter(tp -> properties.containsKey("arrRate."+tp.toString()))
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
				.map(
						entry -> new AggregateFixedMultiplierPassthroughMatrix(
								distributor(entry.getKey()).distribute(getPAMap()),
								entry.getValue())
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
	
	private float getVOT() {
		return Float.parseFloat(properties.getProperty("vot"));
	}
}
