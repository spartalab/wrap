package edu.utexas.wrap;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.assignment.AssignmentBuilder;
import edu.utexas.wrap.assignment.AssignmentInitializer;
import edu.utexas.wrap.assignment.AssignmentProvider;
import edu.utexas.wrap.assignment.AssignmentConsumer;
import edu.utexas.wrap.assignment.BushInitializer;
import edu.utexas.wrap.assignment.GapEvaluator;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.BushBuilder;
import edu.utexas.wrap.assignment.bush.BushGapEvaluator;
import edu.utexas.wrap.assignment.bush.BushReader;
import edu.utexas.wrap.assignment.bush.BushWriter;
import edu.utexas.wrap.assignment.bush.algoB.AlgorithmBOptimizer;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.containers.FixedSizeODMatrix;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.util.ODMatrixCollector;

import java.util.AbstractMap.SimpleEntry;

/**This main class reads in a network's information, then
 * launches threads to create OD matrices for a specified
 * set of trip purposes, according to a given ModelInput
 * file. These purposes are then combined together according
 * to their VOT, mode, and time of day, creating matrices
 * that are then output to files.
 * 
 * @author William Alexander
 *
 */
public class wrapTP {

	public static void main(String[] args) {
		
		//Get the name of the ModelInput source
		if (args.length == 0) {
			System.err.println("No model input file supplied");
			System.exit(1);
		}
		//and instantiate it as a ModelInput object
		ModelInput model = new ModelInputNCTCOG(args[0]);
		
		//Load the model's network
		Graph network = model.getNetwork();
		
		//Retrieve the collection of trip purposes to be created
		Collection<TripPurpose> purposes = model.getTripPurposes();
		
		//Launch each trip purpose that operates as a Thread
		System.out.println("Generating OD matrices");
		purposes.parallelStream()
			//here, it's possible that not all TripPurposes are threads, so we ignore those
			.filter(purpose -> purpose instanceof Thread)
			.forEach(purpose -> {
				//Start the thread which generates TripPurposes
				((Thread) purpose).start();
				
				//then wait until it completes or is interrupted
				try {
					((Thread) purpose).join();
				} catch (InterruptedException e) {
					//if it is interrupted, for now we just ignore it 
					//(this could cause issues later. FIXME)
					System.err.println("Error in "+purpose+" thread; ignoring results");
					e.printStackTrace();
				}
			});

		
		
		Map<TimePeriod,Map<Float,Map<Mode,ODMatrix>>> ods = combineMatrices(model, purposes);
		
		//TODO redo this method to streamline
		Map<TimePeriod,Collection<ODMatrix>> flatODs = flatten(ods);
		
		for (Entry<TimePeriod, Collection<ODMatrix>> entry : flatODs.entrySet()) {
			
			AssignmentProvider<Bush> reader = new BushReader(network);
			AssignmentConsumer<Bush> writer = new BushWriter(network);
			AssignmentBuilder<Bush> builder = new BushBuilder(network);
			
			AssignmentInitializer<Bush> initializer = new BushInitializer(reader,writer,builder, network);
			
			for (ODMatrix od : entry.getValue()) initializer.add(od);
			
			Assigner<Bush> assigner = new Assigner<Bush>(
					initializer,
					new GapEvaluator<Bush>(network, reader),
					new AlgorithmBOptimizer(
							reader, 
							writer, 
							new BushGapEvaluator(),
							10E-5)
					);
			
			Thread assignmentThread = new Thread(assigner);
			assignmentThread.start();
			
			//Should this allow for multiple assignments at once? depends on success
			try {
				assignmentThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Consolidating OD Matrices");
		
//		System.out.println("Writing OD matrices");
//		ods.entrySet().parallelStream().forEach(
//			tpEntry -> 
//		
//				tpEntry.getValue().entrySet().parallelStream().forEach(
//					votEntry ->
//						votEntry.getValue().entrySet().parallelStream()
//						.forEach(
//							modeEntry ->
//								ODMatrixBINWriter.write(
//										model.getOutputDirectory(),
//										tpEntry.getKey(),
//										modeEntry.getKey(),
//										votEntry.getKey(),
//										modeEntry.getValue()
//									)
//							)
//					)
//			);
		System.out.println("Done");
	}

	private static Map<TimePeriod, Collection<ODMatrix>> flatten(Map<TimePeriod, Map<Float, Map<Mode, ODMatrix>>> ods) {
		return ods.entrySet().parallelStream()
		.collect(
				Collectors.toMap(
						Entry::getKey, 
						outerEntry -> outerEntry.getValue().entrySet().parallelStream()
						.flatMap(middleEntry -> middleEntry.getValue().entrySet().parallelStream()
								.map(
										innerEntry -> new SimpleEntry<Float,Entry<Mode,ODMatrix>>(
												middleEntry.getKey(),
												innerEntry
												)
										)
								)
						.map(
								entry -> {
									Float vot = entry.getKey();
									Mode mode = entry.getValue().getKey();
									ODMatrix od = entry.getValue().getValue();
									
									return new FixedSizeODMatrix(vot,mode,od);
								}
								)
						.collect(Collectors.toSet())
						)
				);
	}

	private static Map<TimePeriod, Map<Float, Map<Mode, ODMatrix>>> combineMatrices(ModelInput model,
			Collection<TripPurpose> purposes) {
		return expand(purposes).collect(
				Collectors.groupingBy(
						entry -> model.getAggregateTimePeriod(entry.getValue().getValue().getKey()),
						Collectors.groupingBy(
								entry -> model.getVOT(
										entry.getKey(),
										entry.getValue().getKey(),
										entry.getValue().getValue().getValue().getKey()),
								Collectors.groupingBy(
										entry -> entry.getValue().getValue().getValue().getKey(),
										Collectors.mapping(
												entry -> entry.getValue().getValue().getValue().getValue(), 
												new ODMatrixCollector())
										)
								)
						)
				);
	}

	private static Stream<SimpleEntry<TripPurpose, Entry<MarketSegment, Entry<TimePeriod, Entry<Mode, ODMatrix>>>>> expand(Collection<TripPurpose> purposes) {
		return purposes.parallelStream()
		.flatMap(
				purpose -> purpose.getODMaps()
				.map( entry -> 
				new SimpleEntry<TripPurpose, Entry<MarketSegment, Map<TimePeriod,Collection<ODMatrix>>>>(
						purpose,
						entry
						))
				)
		.flatMap(
				outerEntry -> outerEntry.getValue().getValue().entrySet().parallelStream()
				.map(innerEntry -> new SimpleEntry<TripPurpose, Entry<MarketSegment, Entry<TimePeriod,Collection<ODMatrix>>>>(
						outerEntry.getKey(),
						new SimpleEntry<MarketSegment, Entry<TimePeriod,Collection<ODMatrix>>>(
								outerEntry.getValue().getKey(),
								innerEntry
								)
						)
						)		
				)
		.flatMap(
				outerEntry-> outerEntry.getValue().getValue().getValue().parallelStream()
				.map(innerEntry -> new SimpleEntry<TripPurpose,Entry<MarketSegment,Entry<TimePeriod,Entry<Mode,ODMatrix>>>>(
						outerEntry.getKey(),
						new SimpleEntry<MarketSegment,Entry<TimePeriod,Entry<Mode,ODMatrix>>>(
								outerEntry.getValue().getKey(),
								new SimpleEntry<TimePeriod,Entry<Mode,ODMatrix>>(
										outerEntry.getValue().getValue().getKey(),
										new SimpleEntry<Mode,ODMatrix>(
												innerEntry.getMode(),
												innerEntry
												)
										)
								)
						))
				);
	}

}
