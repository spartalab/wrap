package edu.utexas.wrap;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.balancing.TripBalancer;
import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.EmptyAggregatePAMatrix;
import edu.utexas.wrap.demand.containers.FixedMultiplierPassthroughPAMap;
import edu.utexas.wrap.demand.containers.FixedSizePAMap;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.generation.GenerationRate;
import edu.utexas.wrap.generation.TripGenerator;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegmenter;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.modechoice.FixedProportionSplitter;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.util.AggregatePAMatrixCollector;
import edu.utexas.wrap.util.DepartureArrivalConverter;

public class BasicTripPurpose extends Thread implements TripPurpose {
	
	ModelInput model;
	TripGenerator productionGenerator, attractionGenerator;
	TripBalancer balancer;
	TripDistributor peakDistributor, offPeakDistributor;

	@Override
	public Stream<Entry<MarketSegment, GenerationRate>> getProductionRates() {
		return model.getGeneralProdRates(this).entrySet().parallelStream();
	}

	@Override
	public Stream<Entry<MarketSegment, GenerationRate>> getAttractionRates() {
		return model.getAreaClassAttrRates(this).entrySet().parallelStream();
	}

	@Override
	public Stream<Entry<MarketSegment, DemandMap>> getProductions(Stream<Entry<MarketSegment, GenerationRate>> prodRates) {
		return prodRates.map(entry -> new SimpleEntry<MarketSegment, DemandMap>(
				entry.getKey(),
				productionGenerator.generate(entry.getValue())));
	}

	@Override
	public Stream<Entry<MarketSegment, DemandMap>> getAttractions(Stream<Entry<MarketSegment, GenerationRate>> attrRates) {
		return attrRates.map(entry -> new SimpleEntry<MarketSegment,DemandMap>(
				entry.getKey(),
				attractionGenerator.generate(entry.getValue())));
	}

	@Override
	public Stream<Entry<MarketSegment, PAMap>> buildProductionAttractionMaps(
			Stream<Entry<MarketSegment, DemandMap>> prods, Stream<Entry<MarketSegment, DemandMap>> attrs) {
		Map<MarketSegment,DemandMap> prodSegs = prods.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		Map<MarketSegment,DemandMap> attrSegs = attrs.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		
		Collection<MarketSegment> bigSegs = model.getSegments(this);
		
		return bigSegs.parallelStream().map(bigSeg -> {
			Stream<DemandMap> 
				prodMaps = prodSegs.entrySet().parallelStream()
					.filter(entry -> bigSeg.equals(entry.getKey()))
					.map(Entry::getValue),
					
				attrMaps = attrSegs.entrySet().parallelStream()
					.filter(entry -> bigSeg.equals(entry.getKey()))
					.map(Entry::getValue);
			
			return new SimpleEntry<MarketSegment,PAMap>(bigSeg, new FixedSizePAMap(prodMaps,attrMaps));
		});
		
	}

	@Override
	public Stream<Entry<MarketSegment, PAMap>> balance(
			Stream<Entry<MarketSegment, PAMap>> paMaps) {
		return paMaps.map(entry -> new SimpleEntry<MarketSegment,PAMap>(
				entry.getKey(),
				balancer.balance(entry.getValue())
				));
	}

	@Override
	public Stream<Entry<MarketSegment, AggregatePAMatrix>> distribute(
			Stream<Entry<MarketSegment, PAMap>> paMaps) {
		
		return paMaps.map( msEntry -> {
			
			Map<TimePeriod,Double> shares = model.getDistributionShares(this,msEntry.getKey());
			
			Stream<AggregatePAMatrix> mtxs = shares.entrySet().parallelStream()
				.filter(tpEntry -> tpEntry.getValue() > 0)
				.map(tpEntry -> 
					model.getDistributor(
							this,
							tpEntry.getKey(),
							msEntry.getKey())
					.distribute(msEntry.getValue()));
			
			
			return new SimpleEntry<MarketSegment,AggregatePAMatrix>(msEntry.getKey(),
					mtxs.collect(new AggregatePAMatrixCollector()));
		});
	}

	@Override
	public Stream<Entry<MarketSegment, Collection<ModalPAMatrix>>> modeChoice(
			Stream<Entry<MarketSegment, AggregatePAMatrix>> paMatrices) {
		// TODO change this from Integer to something more meaningful
		Map<Integer,Map<Mode,Double>> shares = model.getModeShares(this); 
		
		return paMatrices.map(entry ->{
		
			Map<Mode,Double> ms = shares.get(((IncomeGroupSegmenter) entry.getKey()).getIncomeGroup());
			return new SimpleEntry<MarketSegment,Collection<ModalPAMatrix>>(
					entry.getKey(),
					new FixedProportionSplitter(ms).split(entry.getValue()).collect(Collectors.toSet()));
		
		});
	}

	@Override
	public Map<TimePeriod,Stream<Entry<MarketSegment, Collection<ODMatrix>>>> convertToOD(
			Stream<Entry<MarketSegment, Collection<ModalPAMatrix>>> paMatrices) {
		
		return Stream.of(TimePeriod.values()).collect(
				Collectors.toMap(Function.identity(), 
						time -> paMatrices.map(entry ->{
							DepartureArrivalConverter converter = new DepartureArrivalConverter(
									model.getDepartureRates(this, 
											((IncomeGroupSegmenter) entry.getKey()).getIncomeGroup()).get(time),
									model.getArrivalRates(this, 
											((IncomeGroupSegmenter) entry.getKey()).getIncomeGroup()).get(time)
									);
							return new SimpleEntry<MarketSegment,Collection<ODMatrix>>(entry.getKey()
									,entry.getValue().parallelStream()
									.map(mtx -> converter.convert(mtx, 
											model.getOccupancyRates().get(mtx.getMode())))
									.collect(Collectors.toSet()));
		})));
	}

}
