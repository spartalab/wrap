package edu.utexas.wrap;

import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;

import java.io.IOException;
import java.util.Map;

public interface ModelInput {

    //Overall Inputs
    Graph getNetwork();

    //Trip Generation Inputs

    Map<MarketSegment,Double> getGeneralProdRates(TripPurpose purpose);

    Map<MarketSegment, Map<AreaClass, Double>> getAreaClassProdRates(TripPurpose purpose);

    Map<MarketSegment, Double> getGeneralAttrRates(TripPurpose purpose);

    Map<MarketSegment, Map<AreaClass, Double>> getAreaClassAttrRates(TripPurpose purpose);


    //Trip Distribution Inputs
    float[][] getSkimFactors();

    Map<MarketSegment, FrictionFactorMap> getFrictionFactors(TripPurpose purpose);


    //Mode Choice Inputs
    Map<MarketSegment,Map<Mode,Double>> getModeShares(TripPurpose purpose);

    Map<Mode,Double> getOccupancyRates();


    //PA to OD
    Map<TimePeriod, Double> getDepartureRates(TripPurpose purpose, MarketSegment segment);

    Map<TimePeriod, Double> getArrivalRates(TripPurpose purpose, MarketSegment segment);

}
