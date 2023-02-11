package edu.utexas.wrap.marketsegmentation;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
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
import edu.utexas.wrap.distribution.ImpedanceMatrix;
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
 */
public class BasicPurpose implements Purpose {

	private final Properties properties;
	private final Market parent;
	private final Path source;
	
	
	private Collection<TripDistributor> distributors;
	private Demographic ubProds, ubAttrs;
	private GenerationRate[] prodRates, attrRates;
	private String name;
	private final Map<Integer,TravelSurveyZone> zones;
	private TripInterchangeSplitter modeSplitter;
	private Map<TimePeriod,Float> departureRates, arrivalRates;
	private TimeOfDaySplitter todSplitter;
	private TripBalancer balancer;

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
		loadModeSplitter();
		loadTimeOfDaySplitter();
		loadBalancer();
	}
	
	public void writeProperties() throws IOException {
		properties.store(Files.newOutputStream(source, StandardOpenOption.WRITE, StandardOpenOption.CREATE), null);
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
		.map(id -> new GravityDistributor(id,this,getDistributionScalingFactor(id),100,
				getDistributionWeights(getZoneWeightSource(id))))
		.collect(Collectors.toSet());
	}
	

	
	public NetworkSkim getNetworkSkim(TripDistributor distributor) {
		return parent.getNetworkSkim(properties.getProperty("distrib."+distributor.toString()+".skim"));
	}



	private Demographic unbalancedProductions() {
		if (ubProds == null) getPAMap();
		return ubProds;

	}

	public GenerationRate[] productionRates() {
		return prodRates;
	}
	
	private Demographic productionDemographic() {
		switch (getProducerDemographicType()) {
		case "basic":
			return parent.getBasicDemographic(getProductionDemographicSource());
		case "prodProportional":
			return parent.getBasicPurpose(getProductionDemographicSource()).unbalancedProductions();
		case "attrProportional":
			return parent.getBasicPurpose(getProductionDemographicSource()).unbalancedAttractions();
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
		switch (getAttractorDemographicType()) {
		case "basic":
			return parent.getBasicDemographic(getAttractionDemographicSource());
		case "prodProportional":
			return parent.getBasicPurpose(getAttractionDemographicSource()).unbalancedProductions();
		case "attrProportional":
			return parent.getBasicPurpose(getAttractionDemographicSource()).unbalancedAttractions();
		default:
			throw new RuntimeException("Not yet implemented");
		}
	}

	private void loadBalancer() {
		switch (getBalancingMethod()) {

		case "prodProportional":
			balancer = new Prod2AttrProportionalBalancer(null);
			break;
		case "attrProportional":
			balancer = new Attr2ProdProportionalBalancer();
			break;
		default:
			throw new RuntimeException("Not yet implemented");
		}

	}



	public FrictionFactorMap getFrictionFunction(TripDistributor distributor) {
		return parent.getFrictionFactor(properties.getProperty("distrib."+distributor+".frictFacts"));
	}


	private DistributionWeights getDistributionWeights(String skimID) {
		String source = properties.getProperty("distrib."+skimID+".weights");
		return new BasicDistributionWeights(zones,
				source == null? null : getDirectory().resolve(source));
	}



	public String getZoneWeightSource(String distributorID) {
		return properties.getProperty("distrib."+distributorID+".weights");
	}



	private Map<Mode,Float> loadModeShares(){
		return Stream.of(Mode.values())
				.filter(mode -> properties.containsKey("modeChoice.proportion."+mode.toString()))
				.collect(
						Collectors.toMap(
								Function.identity(),
								mode -> Float.parseFloat(properties.getProperty("modeChoice.proportion."+mode.toString()))
								)
						);
	}

	private void loadModeSplitter() {

		modeSplitter = new FixedProportionSplitter(loadModeShares());
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

	private void loadTimeOfDaySplitter(){
		todSplitter = new TimeOfDaySplitter(departureRates, arrivalRates, loadVOTs());
	}





	/**Instantiate a vehicle converter and convert ModalPAMatrices
	 * 
	 * This method creates a PassengerVehicleConverter according to the
	 * properties associated with this Purpose, then converts the matrices
	 * returned from the mode choice step of the model.
	 *
	 */
	@Override
	public Collection<ODMatrix> getDailyODMatrices(Collection<ModalPAMatrix> matrices) {
		return vehicleConverter().convert(matrices);
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
	public Collection<ModalPAMatrix> getModalPAMatrices(AggregatePAMatrix matrix) {
		return modeSplitter.split(matrix);
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
	public AggregatePAMatrix getAggregatePAMatrix(PAMap map) {
		
		return distributors.stream()
				.map(distributor -> {
					NetworkSkim currentObservation = getNetworkSkim(distributor);
					FrictionFactorMap frictionFunction = getFrictionFunction(distributor);
					ImpedanceMatrix impedances = new ImpedanceMatrix(getZones(),currentObservation,frictionFunction);
					
					AtomicBoolean converged = new AtomicBoolean(false);
					
					for (int iter = 0; iter < distributor.maxIterations();iter++) {
						if (converged.get()) break;
						converged.set(true);
						distributor.updateProducerWeights(map, impedances,converged);
						distributor.updateAttractorWeights(map,impedances,converged);
					}
					return distributor.constructMatrix(map,impedances);
				})
		
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

		return balancer.balance(new PAPassthroughMap(unbalancedProds,unbalancedAttrs));
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
	public Collection<ODProfile> getODProfiles(Collection<ODMatrix> matrices) {
		return todSplitter.split(matrices, this);
	}

	private Map<TimePeriod,Float> loadVOTs() {
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

	@Override
	public Float getVOT(TimePeriod tp) {
		try{
			return Float.parseFloat(properties.getProperty("vot."+tp.toString()));
		} catch (NullPointerException e) {
			return 0f;
		}
	}
	
	@Override
	public Float getVOT(Mode m, TimePeriod tp) {
		return getVOT(tp);
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



	@Override
	public Collection<TravelSurveyZone> getZones() {
		// TODO Auto-generated method stub
		return zones.values();
	}

	
}