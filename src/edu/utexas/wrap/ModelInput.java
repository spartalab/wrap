package edu.utexas.wrap;

import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

import java.util.Map;

public interface ModelInput {

    //Overall Inputs
    Graph getNetwork();

    //Trip Generation Inputs
    Map<MarketSegment,Double> getGeneralProdRates(TripPurpose purpose);

    Map<MarketSegment, Map<AreaClass, Double>> getAreaClassProdRates(TripPurpose purpose);

    Map<MarketSegment, Double> getGeneralAttrRates(TripPurpose purpose);

    Map<MarketSegment, Map<AreaClass, Double>> getAreaClassAttrRates(TripPurpose purpose);
    
    //Peak/off-peak splitting inputs
    Map<MarketSegment,Double> getPeakShares(TripPurpose purpose);

    //Trip Distribution Inputs
    float[][] getRoadwaySkim(TimePeriod timePeriod);

    FrictionFactorMap getFrictionFactors(TripPurpose purpose, TimePeriod timePeriod, MarketSegment segment);

    //Market segmentation inputs
    Map<MarketSegment,Map<TravelSurveyZone,Double>> getWorkerVehicleSplits(MarketSegment segment, TripPurpose purpose);

    //Mode Choice Inputs
    Map<Integer,Map<Mode,Double>> getModeShares(TripPurpose purpose);

    Map<Mode,Double> getOccupancyRates();

    //PA to OD
    Map<TimePeriod, Double> getDepartureRates(TripPurpose purpose, MarketSegment segment);

    Map<TimePeriod, Double> getArrivalRates(TripPurpose purpose, MarketSegment segment);
    
    String getOutputDirectory();

    Class<? extends MarketSegment> getSegmenterByString(String segmentLabel);
    
    String getLabel(MarketSegment segment);
}
