package edu.utexas.wrap;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.marketsegmentation.Market;

public class wrapMarket {
	static Path projDir, projFile;
	static Project proj;
	
	public static void main(String[] args) {
		//Get a path to the project file (a Properties file of arbitrary extension)
		if (args.length < 1) {
			System.err.println("No model input file supplied");
			System.exit(1);
		}
		projFile = Paths.get(args[0]);
		
		
		//Load the project from the given path
		try {
			proj = new Project(projFile);
		} catch (IOException e) {
			System.err.println("Error loading project properties");
			e.printStackTrace();
			proj = null;
			System.exit(-1);
		}
		
		
		
		
		
		Collection<Market> markets = proj.getMarkets();
		
		Collection<Assigner> assigners = proj.getAssigners();
		
		int numFeedbacks = 1;
		for (int i = 0; i < numFeedbacks; i++) {
			boolean initialIteration = i == 0;
			
			
			Stream<ODProfile> stream =
			markets.parallelStream()
				.flatMap(market -> market.buildODs(initialIteration? proj.getInitialSkims() : proj.getFeedbackSkims(assigners)));
			
			
			stream
				.forEach(od -> 
				assigners.stream().forEach(assigner -> assigner.process(od))
				);

			
			assigners.stream().forEach(Assigner::run);
			
		}
	}
}
