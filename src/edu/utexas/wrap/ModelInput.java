package edu.utexas.wrap;

import edu.utexas.wrap.balancing.TripBalancer;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.generation.GenerationRate;
import edu.utexas.wrap.generation.TripGenerator;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
//import edu.utexas.wrap.net.TravelSurveyZone;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public interface ModelInput {

	//Overall Inputs
	/** This method provides the model's network data, including any
	 * demographic data relevant to the model
	 * @return the network model as a Graph object
	 */
	Graph getNetwork();

	/** This method returns instances of the TripPurpose objects which should be used
	 * @return a collection of this model's TripPurposes
	 */
	Collection<TripPurpose> getTripPurposes();

	//Trip Generation Inputs
	/** This method provides sets of data rates for specified trip purposes
	 * for trip production across multiple market segments. These production
	 * rates are consistent across all area classes, and define the rate at which
	 * each market segment's attribute data generates trip productions
	 * 
	 * @param purpose the trip purpose whose general production rates should be retrieved
	 * @return a Map from each applicable MarketSegment to a general production rate
	 */
	Map<MarketSegment, GenerationRate> getProdRates(TripPurpose purpose);

	/** This method returns a constructed TripGenerator for a specified TripPurpose,
	 * including any and all data used for generating this TripPurpose's productions
	 * 
	 * @param purpose the TripPurpose whose production generator should be returned
	 * @return a TripGenerator that can generate productions for this TripPurpose
	 */
	TripGenerator getProductionGenerator(TripPurpose purpose);

	/** This method returns a constructed TripGenerator for a specified TripPurpose,
	 * including any and all data used for generating this TripPurpose's attractions
	 * 
	 * @param purpose the TripPurpose whose attraction generator should be returned
	 * @return a TripGenerator that can generate attractions for this TripPurpose
	 */
	TripGenerator getAttractionGenerator(TripPurpose purpose);

	/** This method provides sets of data rates for specified trip purposes
	 * for trip attraction across multiple market segments. These attraction rates
	 * are consistent across all area classes, and define the rate at which each market
	 * segment's attribute data generates trip attractions
	 * 
	 * @param purpose the trip purpose whose general attraction rates should be retrieved
	 * @return a Map from each applicable MarketSegment to a general attraction rate
	 */
	Map<MarketSegment, GenerationRate> getAttrRates(TripPurpose purpose);

	/**This method provides the market segments that are to be maintained at the
	 * end of the trip generation step. That is to say that trip generation may use
	 * more fine-grained segments for generation, but all will be combined together
	 * at the end of trip generation into the segments provided by this method. If
	 * some segment(s) are used in generation but an enclosing trip purpose is not
	 * provided by this method, this segment's trip data is dropped.
	 * 
	 * @param purpose the trip purpose whose market segments should be returned
	 * @return a collection of market segments which, after trip generation, will contain all trips for the given purpose
	 */
	Collection<MarketSegment> getSegments(TripPurpose purpose);

	/** This method returns a Stream of MarketSegments that are used in trip
	 * production for the specified trip purpose. After generation, there is
	 * no guarantee that these segments will still be used (thus why a Stream
	 * is used rather than a Collection, as they should only be used once)
	 * 
	 * @param purpose the TripPurpose whose production market segments should be returned
	 * @return a Stream of MarketSegments used in trip production for this purpose
	 */
	Stream<MarketSegment> getProductionSegments(TripPurpose purpose);
	
	/** This method returns a Stream of MarketSegments that are used in trip
	 * attraction for the specified trip purpose. After generation, there is
	 * no guarantee that these segments will still be used (thus why a Stream
	 * is used rather than a Collection, as they should only be used once)
	 * 
	 * @param purpose the TripPurpose whose attraction market segments should be returned
	 * @return a Stream of MarketSegments used in trip attraction for this purpose
	 */
	Stream<MarketSegment> getAttractionSegments(TripPurpose purpose);

	/** This method provides the trip balancer for a specified TripPurpose
	 * 
	 * @param purpose the TripPurpose whose TripBalancer should be returned
	 * @return a TripBalancer that meets this TripPurpose's model specification
	 */
	TripBalancer getBalancer(TripPurpose purpose);

	/** This method provides sets of data rates for specified trip purposes
	 * for time period splitting across multiple market segments. These rates
	 * define the fraction of trips for each market segment whose costs are
	 * evaluated based on cost skims from each time period provided.
	 * This method is NOT defining how many trips are actually taken during the
	 * peak hour, but rather how many trips' endpoints are determined by the cost
	 * that will be encountered during the peak hour
	 * 
	 * @param purpose the trip purpose whose distribution cost share rate should be retrieved
	 * @param segment the market segment whose distribution cost shares should be retrieved
	 * @return a Map from each applicable MarketSegment to the corresponding time period's share
	 */
	Map<TimePeriod,Double> getDistributionShares(TripPurpose purpose, MarketSegment segment);

	//Trip Distribution Inputs
	/**This method provides a matrix of costs between all pairs of TravelSurveyZones
	 * in the network. These cost matrices can be used in trip distribution to define
	 * the FrictionFactorMaps that determine trip impedance
	 * 
	 * @param timePeriod the time period whose costs should be returned
	 * @return a two-dimensional structure where each TSZ's graph order is the index, with the first
	 * index being the producer and the second index being the attractor 
	 * 
	 * TODO modify this return type to a clearer structure based on TSZ maps
	 */
	float[][] getRoadwaySkim(TimePeriod timePeriod);

	/** This method returns a FrictionFactorMap for trips for a given purpose in a given
	 * time period of a specified MarketSegment. These FrictionFactorMaps define the impedance
	 * for trips between two zones
	 * 
	 * @param purpose the TripPurpose whose FrictionFactorMap should be retrieved
	 * @param timePeriod the TimePeriod whose FrictionFactorMap should be retrieved
	 * @param segment the MarketSegment whose FrictionFactorMap should be retrieved
	 * @return a FrictionFactorMap which details the trip impedance between TravelSurveyZone pairs
	 * in the network
	 */
	FrictionFactorMap getFrictionFactors(TripPurpose purpose, TimePeriod timePeriod, MarketSegment segment);

	//TODO document this and provide implementation
	/** This method returns q TripDistributor used for trips of a certain TripPurpose-MarketSegment-TimePeriod
	 * combination, including all necessary components for trip distribution
	 * 
	 * @param purpose the relevant TripPurpose which defines the TripDistributor
	 * @param timePeriod the TimePeriod whose costs should be used in the TripDistributor
	 * @param segment the MarketSegment whose FrictionFactorMap should be used in the TripDistributor
	 * @return a TripDistributor for the given trip description
	 */
	TripDistributor getDistributor(TripPurpose purpose, TimePeriod timePeriod, MarketSegment segment);

	//Market segmentation inputs
	/** For market subsegmentation, this method provides details on the fraction of each TravelSurveyZone's
	 * trips fall into the more-bespoke WorkerVehicle segments pertaining to a given segment which is more
	 * general in nature. For example, if the given MarketSegment is trips in IncomeGroup 3, the returned
	 * Map details the shares of these trips that are in IncomeGroup 3 with x workers and y vehicles for
	 * each TravelSurveyZone in the network
	 * 
	 * @param segment the given general MarketSegment which should be subdivided
	 * @param purpose the TripPurpose whose trips should be divided
	 * @return a Map from each subsegmented MarketSegment to a Map from each TravelSurveyZone to the
	 * share of that TSZ's trips that fall into the given subsegmented MarketSegment
	 */
//	Map<MarketSegment,Map<TravelSurveyZone,Double>> getWorkerVehicleSplits(MarketSegment segment, TripPurpose purpose);

	//Mode Choice Inputs
	/** This method provides details on each IncomeGroup's mode choice shares
	 * 
	 * @param purpose the TripPurpose whose mode shares should be returned
	 * @return a Map from an IncomeGroup (represented as an integer) to a Map from a Mode to the 
	 * fraction of trips taken by this IncomeGroupSegment which use this Mode
	 * 
	 * TODO alter this structure to actually use MarketSegments rather than Integers
	 */
	Map<MarketSegment,Map<Mode,Double>> getModeShares(TripPurpose purpose);

	/** This method provides each mode's fractional multiplier. That is, for each mode, the 
	 * returned object defines what value the original PA matrix should be multiplied by in
	 * PA-to-OD matrix by to account for vehicle occupancy. For example, for the SINGLE_OCCUPANCY
	 * mode, only one passenger is in each vehicle, so the multiplier is 1 (no change). However,
	 * HOV_2_PSGR trips have two passengers, so for every vehicle-trip, this accounts for two
	 * passenger-trips, so the multiplier should be 0.5 (i.e. the demand is halved by converting
	 * passenger-trips to vehicle-trips)
	 * 
	 * @return a Map from each Mode to the occupancy fractional multiplier
	 */
	Map<Mode,Double> getOccupancyRates();

	//PA to OD
	/**This method defines the fraction of trips that are departing in each time period
	 * for a given TripPurpose and IncomeGroup. These values determine the time frame
	 * in which the original trip is taken, departing from the trip producer
	 * 
	 * @param purpose the trip purpose whose time-of-day departure distribution should be returned
	 * @param ig the income group whose time-of-day departure distribution should be returned
	 * @return the distribution of shares of departure trips (trips from the producer) across time-of-day periods
	 */
	Map<TimePeriod, Double> getDepartureRates(TripPurpose purpose, MarketSegment ig);

	/**This method defines the fraction of trips that are arriving in each time period
	 * for a given TripPurpose and IncomeGroup. These values determine the time frame
	 * in which the return trip is taken, arriving back at the trip producer
	 * 
	 * @param purpose the trip purpose whose time-of-day arrival distribution should be returned
	 * @param ig the income group whose time-of-day arrival distribution should be returned
	 * @return the distribution of shares of arrival trips (trips to the producer) across time-of-day periods
	 */
	Map<TimePeriod, Double> getArrivalRates(TripPurpose purpose, MarketSegment ig);

	/**
	 * @return the directory to which output files should be written
	 */
	String getOutputDirectory();

	/**This method is used to construct market segments from labels. Given a string
	 * which represents a market segmenter, this method returns the appropriate Class
	 * object which should be instantiated
	 * 
	 * @param segmentLabel a string which clearly defines a single MarketSegment
	 * @return the Class object corresponding with the MarketSegment which can be instantiated
	 */
	Class<? extends MarketSegment> getSegmenterByString(String segmentLabel);

	/** This method provides a shorthand representation for MarketSegments
	 * @param segment a MarketSegment whose shorthand name should be returned
	 * @return a shorthand String representation of the MarketSegment
	 */
	String getLabel(MarketSegment segment);

	/**
	 * @return a collection of TimePeriods whose OD matrices should be output
	 */
	Collection<TimePeriod> getUsedTimePeriods();

	/** This method gets the value-of-time of trips for a specified purpose in a specified 
	 * segment using a specified mode
	 * 
	 * @param purpose the TripPurpose of an OD matrix whose VOT should be returned
	 * @param segment the MarketSegment of an OD matrix whose VOT should be returned
	 * @param mode the Mode of an OD matrix whose VOT should be returned
	 * @return a Float representing the cost of each time unit for the specified OD matrix attributes
	 */
	Float getVOT(TripPurpose purpose, MarketSegment segment, Mode mode);
}
