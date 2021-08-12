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
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.PAPassthroughMap;
import edu.utexas.wrap.distribution.BasicDistributionWeights;
import edu.utexas.wrap.distribution.DistributionWeights;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.distribution.GravityDistributor;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.generation.AreaClassGenerationRate;
import edu.utexas.wrap.generation.ComponentTripGenerator;
import edu.utexas.wrap.generation.GeneralGenerationRate;
import edu.utexas.wrap.generation.GenerationRate;
import edu.utexas.wrap.modechoice.FixedProportionSplitter;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.modechoice.TripInterchangeSplitter;
import edu.utexas.wrap.net.Demographic;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.SecondaryDemographic;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.AggregatePAMatrixCollector;
import edu.utexas.wrap.util.PassengerVehicleTripConverter;
import edu.utexas.wrap.util.TimeOfDaySplitter;

/**A basic implementation of Purpose which completes the full UTMS
 * 
 * @author William
 *
 */public class BasicPurpose implements Purpose {

	private final Properties properties;
	private final Market parent;
	private final Path source;
	
	
	private Collection<TripDistributor> distributors;
	private Demographic ubProds, ubAttrs;
	private GenerationRate[] prodRates, attrRates;
	private String name;
	private final Map<Integer,TravelSurveyZone> zones;
	private Map<TimePeriod,Float> departureRates, arrivalRates;

	public BasicPurpose(String name,
			Path purposeFile, 
			Market parent,
			Map<Integer,TravelSurveyZone> zones
			) throws IOException {

		this.parent = parent;
		this.source = purposeFile;
		this.zones = zones;
		properties = new Properties();
		loadProperties();
		this.name = name;

	}



	public void loadProperties() throws IOException {
		properties.clear();
		properties.load(Files.newInputStream(source));
		
		loadProductionRates();
		loadAttractionRates();
		loadDistributors();
		loadDepartureRates();
		loadArrivalRates();
	}

	private void loadProductionRates() {
		switch (properties.getProperty("prodType")) {

		case "basic":
			prodRates = Stream.of(properties.getProperty("prodRate").split(","))
					.map(Double::parseDouble)
					.map(flt -> new GeneralGenerationRate(flt))
					.toArray(GenerationRate[]::new);
			break;

		case "area":
			prodRates = Stream.of(IndustryClass.values())
					.map(ic ->
					Stream.of(properties.getProperty("prodRate."+ic.toString()).split(","))
					.mapToDouble(Double::parseDouble)
					.toArray()
							)
					.map(AreaClassGenerationRate::new)
					.toArray(GenerationRate[]::new);
			break;

		default:
			throw new RuntimeException("Not yet implemented"); 
		}
	}

	private void loadAttractionRates() {
		switch (properties.getProperty("attrType")) {

		case "basic":
			attrRates = Stream.of(properties.getProperty("attrRate").split(","))
					.map(Double::parseDouble)
					.map(flt -> new GeneralGenerationRate(flt))
					.toArray(GenerationRate[]::new);
			break;

		case "area":
			attrRates = Stream.of(IndustryClass.values())
					.map(ic ->
					Stream.of(properties.getProperty("attrRate."+ic.toString()).split(","))
					.mapToDouble(Double::parseDouble)
					.toArray()
							)
					.map(AreaClassGenerationRate::new)
					.toArray(GenerationRate[]::new);
			break;

		default:
			throw new RuntimeException("Not yet implemented"); 
			}
	}
	
	private void loadDistributors() {
		distributors = Stream.of(properties.getProperty("distrib.ids").split(","))
		.map(id -> new GravityDistributor(id,this,getDistributionScalingFactor(id),
				getFrictionFunction(id),
				getDistributionWeights(getZoneWeightSource(id))))
		.collect(Collectors.toSet());
	}
	

	
	private NetworkSkim getNetworkSkim(String distributorID) {
		return parent.getNetworkSkim(properties.getProperty("distrib."+distributorID+".skim"));
	}



	private Demographic unbalancedProductions() {
		if (ubProds == null) getPAMap();
		return ubProds;

	}

	public GenerationRate[] productionRates() {
		return prodRates;
	}
	
	private Demographic productionDemographic() {
		switch (properties.getProperty("prodDemographic.type")) {
		case "basic":
			return parent.getBasicDemographic(properties.getProperty("prodDemographic.id"));
		case "prodProportional":
			return parent.getBasicPurpose(properties.getProperty("prodDemographic.id")).unbalancedProductions();
		case "attrProportional":
			return parent.getBasicPurpose(properties.getProperty("prodDemographic.id")).unbalancedAttractions();
		default:
			throw new RuntimeException("Not yet implemented");
		}
	}

	private Demographic unbalancedAttractions() {
		if (ubAttrs == null) getPAMap();
		return ubAttrs;
	}

	public GenerationRate[] attractionRates() {
		return attrRates;
	}

	private Demographic attractionDemographic() {
		switch (properties.getProperty("attrDemographic.type")) {
		case "basic":
			return parent.getBasicDemographic(properties.getProperty("attrDemographic.id"));
		case "prodProportional":
			return parent.getBasicPurpose(properties.getProperty("attrDemographic.id")).unbalancedProductions();
		case "attrProportional":
			return parent.getBasicPurpose(properties.getProperty("attrDemographic.id")).unbalancedAttractions();
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



	public FrictionFactorMap getFrictionFunction(String distributorID) {
		return parent.getFrictionFactor(properties.getProperty("distrib."+distributorID+".frictFacts"));
	}


	private DistributionWeights getDistributionWeights(String skimID) {
		String source = properties.getProperty("distrib."+skimID+".weights");
		return new BasicDistributionWeights(zones,
				source == null? null : getDirectory().resolve(source));
	}



	public String getZoneWeightSource(String distributorID) {
		return properties.getProperty("distrib."+distributorID+".weights");
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
		return new PassengerVehicleTripConverter();
	}



	private void loadDepartureRates(){
		departureRates = Stream.of(TimePeriod.values())
				.filter(tp -> properties.containsKey("depRate."+tp.toString()))
				.collect(
						Collectors.toMap(
								Function.identity(),
								tp -> Float.parseFloat(properties.getProperty("depRate."+tp.toString()))
								)
						);
		//		return ret;
	}

	private void loadArrivalRates(){
		arrivalRates = Stream.of(TimePeriod.values())
				.filter(tp -> properties.containsKey("arrRate."+tp.toString()))
				.collect(
						Collectors.toMap(
								Function.identity(), 
								tp -> Float.parseFloat(properties.getProperty("arrRate."+tp.toString()))
								)
						);
	}

	private TimeOfDaySplitter timeOfDaySplitter(){
		return new TimeOfDaySplitter(departureRates, arrivalRates, getVOTs());
	}





	/**Instantiate a vehicle converter and convert ModalPAMatrices
	 * 
	 * This method creates a PassengerVehicleConverter according to the
	 * properties associated with this Purpose, then converts the matrices
	 * returned from the mode choice step of the model.
	 *
	 */
	@Override
	public Stream<ODMatrix> getDailyODMatrices() {
		return vehicleConverter().convert(getModalPAMatrices());
	}

	/**Instantiate a mode splitter and split the AggregatePAMatrix
	 * 
	 * This method creates a FixedProportionSplitter according to the
	 * properties associated with this Purpose, then converts the
	 * AggregatePAMatrix returned from the distribution step of the model.
	 * 
	 * For each utilized mode foo, the key {@code modeChoice.proportion.foo} identifies
	 * the fraction of total trips which utilize this mode.
	 *
	 */
	@Override
	public Stream<ModalPAMatrix> getModalPAMatrices() {
		return modeSplitter().split(getAggregatePAMatrix());
	}

	/**Instantiate distribution modules and distribute the model's PAMap, then combine together
	 * 
	 * This method determines the shares of trips that are distributed using a named FrictionFactorMap,
	 * then distributes the model's PAMap accordingly via a gravity distributor and multiplies the result
	 * by the distributor's share of overall demand. This is repeated for each associated distributor,
	 * then all results are combined together by summation.
	 *
	 * The list of associated FrictionFactorMap names is held in the property {@code distrib.ids},
	 * and each id {@code foo} is associated with two other keys: {@code distrib.foo.split}
	 * and {@code distrib.foo.frictFacts}.
	 */
	@Override
	public AggregatePAMatrix getAggregatePAMatrix() {
		
		return distributors.stream()
				.map(distributor -> distributor.distribute(
						getPAMap(), 
						getNetworkSkim(distributor.toString())))
		
				.collect(new AggregatePAMatrixCollector());

	}

	/**Generate a set of productions and attractions, then balance them
	 * 
	 * First, the production and attraction source demographics' types are indicated
	 * by the properties {@code prodDemographic.type} and {@code attrDemographic.type}.
	 * These demographics are then identified using the keys {@code prodDemographic.id}
	 * and {@code attrDemographic.id}.
	 * 
	 * Next, the production and attraction rate types are indicated by the keys
	 * {@code prodType} and {@code attrType}, respectively. If these are basic rates, i.e.
	 * not dependent on the zone's area class, they will be associated with the single keys
	 * {@code prodRate} and {@code attrRate}; however, if the rates are dependent on the area
	 * class, then for each AreaClass foo, there will be an associated key {@code attrRate.foo}.
	 * Each of these keys' values will be a comma-separated list that should match the
	 * dimensionality of its associated demographic. Mixing of rate types is allowed
	 * 
	 * The unbalanced productions and attractions are calculated as a dot product of the
	 * rates and the demographics, and the results are stored as demographics for later use
	 * by trip purposes that extend from this purpose.
	 * 
	 * Finally, a TripBalancer is instantiated based on the type identified by the property
	 * {@code balancer.class}. This balancing method is then used to ensure that the number
	 * of productions and attractions is equal.
	 *
	 */
	@Override
	public PAMap getPAMap() {

		Demographic 
		productionDemographic = productionDemographic(), 
		attractionDemographic = attractionDemographic();

		ComponentTripGenerator
		producer = new ComponentTripGenerator(zones.values(),productionRates()),
		attractor = new ComponentTripGenerator(zones.values(),attractionRates());

		DemandMap 
		unbalancedProds = producer.generate(productionDemographic),
		unbalancedAttrs = attractor.generate(attractionDemographic);

		ubProds = new SecondaryDemographic(producer.getComponents());
		ubAttrs = new SecondaryDemographic(attractor.getComponents());

		return balancer().balance(new PAPassthroughMap(unbalancedProds,unbalancedAttrs));
	}

	/**Split the daily ODMatrix according to various time-of-day shares
	 *
	 * For each utilized TimePeriod foo, the keys {@code depRate.foo}, {@code arrRate.foo},
	 * and {@code vot.foo} are all required. These represent, respectively, the share of
	 * trips departing in time period {@code foo}, arriving in time period {@code foo}, and
	 * the value of time of trips occuring for this purpose during time period {@code foo}.
	 * 
	 * This data is utilized to instantiate a time-of-day splitter which multiplies the daily
	 * ODMatrix to create a series of ODMatrices combined into an ODProfile. Each ODMatrix is
	 * associated with its corresponding value of time.
	 *
	 */
	@Override
	public Stream<ODProfile> getODProfiles() {
		return timeOfDaySplitter().split(getDailyODMatrices());
	}

	private Map<TimePeriod,Float> getVOTs() {
		Map<TimePeriod,Float> ret =  Stream.of(TimePeriod.values())
				.filter(tp -> properties.getProperty("vot."+tp.toString()) != null)
				.collect(
						Collectors.toMap(
								Function.identity(), 
								tp->  Float.parseFloat(properties.getProperty("vot."+tp.toString()))
								)
						);
		return ret;
	}

	public Float getVOT(TimePeriod tp) {
		try{
			return Float.parseFloat(properties.getProperty("vot."+tp.toString()));
		} catch (NullPointerException e) {
			return 0f;
		}
	}

	public double personTrips() {
		return getPAMap().getAttractionMap().totalDemand();
	}

	public String toString() {
		return name;
	}



	public void setVOT(TimePeriod row, Float newValue) {
		properties.setProperty("vot."+row.toString(),newValue.toString());
	}



	public Path getDirectory() {
		return source.getParent();
	}



	public void setBalancingMethod(String balancerID) {
		properties.setProperty("balancer.class", balancerID);
	}




	public void setAttractorDemographicType(String demographicType) {
		properties.setProperty("attrDemographic.type", demographicType);
	}



	public void setProducerDemographicType(String demographicType) {
		properties.setProperty("prodDemographic.type", demographicType);

	}

	public Market getMarket() {
		return parent;
	}
	
	public String getBalancingMethod() {
		return properties.getProperty("balancer.class");
	}
	
	public String getProducerDemographicType(){
		return properties.getProperty("prodDemographic.type");
	}
	
	public String getAttractorDemographicType() {
		return properties.getProperty("attrDemographic.type");
	}
	
	public String getProductionDemographicSource() {
		return properties.getProperty("prodDemographic.id");
	}
	
	public String getAttractionDemographicSource() {
		return properties.getProperty("attrDemographic.id");
	}

	public Double getDistributionScalingFactor(String distributorID) {
		return Double.parseDouble(properties.getProperty("distrib."+distributorID+".split"));
	}

	public String getProducerRateType() {
		return properties.getProperty("prodType");
	}
	
	public String getAttractorRateType() {
		return properties.getProperty("attrType");
	}

	public Float getDepartureRate(TimePeriod period) {
		return departureRates.getOrDefault(period,0f);
	}
	
	public Float getArrivalRate(TimePeriod period) {
		return arrivalRates.getOrDefault(period,0f);
	}

	public void setDepartureRate(TimePeriod period, Float rate) {
		departureRates.put(period, rate);
		properties.setProperty("depRate."+period.toString(), rate.toString());
	}
	
	public void setArrivalRate(TimePeriod period, Float rate) {
		departureRates.put(period, rate);
		properties.setProperty("arrRate."+period.toString(), rate.toString());
	}
	
	public Collection<TripDistributor> getDistributors() {
		return distributors;
	}
	
}