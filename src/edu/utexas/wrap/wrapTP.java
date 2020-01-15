package edu.utexas.wrap;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.util.ODMatrixCollector;
import edu.utexas.wrap.util.io.output.ODMatrixBINWriter;

import java.util.AbstractMap.SimpleEntry;

public class wrapTP {

	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.err.println("No model input file supplied");
			System.exit(1);
		}
		
		ModelInput model = new ModelInputNCTCOG(args[0]);
		
		model.getNetwork();
		
		Collection<TripPurpose> purposes = model.getTripPurposes();
		
		purposes.parallelStream()
			.filter(purpose -> purpose instanceof Thread)
			.forEach(purpose -> {
				
				((Thread) purpose).start();
				
				try {
					((Thread) purpose).join();
				} catch (InterruptedException e) {
					System.err.println("Error in "+purpose+" thread; ignoring results");
					e.printStackTrace();
				}
			});

		Map<TimePeriod,Map<Float,Map<Mode,ODMatrix>>> ods = 
			model.getUsedTimePeriods().parallelStream().collect(
				Collectors.toMap(
					Function.identity(),
					
					tp ->
				
						purposes.parallelStream().flatMap(
								
							purpose -> 
							
								purpose.getODMap(tp)
								.entrySet().parallelStream()
								.flatMap(
										
									entry -> 
									
										entry.getValue().parallelStream()
										.map(
												
											od -> 
											
												new SimpleEntry<TripPurpose,Entry<MarketSegment,ODMatrix>>(
														
													purpose, 
													
													new SimpleEntry<MarketSegment,ODMatrix>(
														entry.getKey(),
														od)
													
													)
											)
									)
							)
						.collect(
							Collectors.groupingBy(
				
								outerEntry -> 
									model.getVOT(
											
										outerEntry.getKey(),
										outerEntry.getValue().getKey(),
										outerEntry.getValue().getValue().getMode()
										
										),
				
								Collectors.groupingBy(
										
										outerEntry -> 
											outerEntry.getValue().getValue().getMode(),
											
										Collectors.mapping(
											
											entry -> 
												entry.getValue().getValue(),
											new ODMatrixCollector()
											
											)
									)
								)
							)
						)
				);
		
		
		ods.entrySet().parallelStream().forEach(
			tpEntry -> 
		
				tpEntry.getValue().entrySet().parallelStream().forEach(
					votEntry ->
						votEntry.getValue().entrySet().parallelStream()
						.forEach(
							modeEntry ->
								ODMatrixBINWriter.write(
										model.getOutputDirectory(),
										tpEntry.getKey(),
										modeEntry.getKey(),
										votEntry.getKey(),
										modeEntry.getValue()
									)
							)
					)
			);
	}

}
