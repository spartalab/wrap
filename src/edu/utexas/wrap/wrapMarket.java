package edu.utexas.wrap;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.marketsegmentation.Market;
import edu.utexas.wrap.marketsegmentation.DummyPurpose;
import edu.utexas.wrap.net.NetworkSkim;

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
		Collection<DummyPurpose> dummies = proj.getDummyPurposes();
		

		Map<String,Assigner> assigners = null;
		
		int numFeedbacks = 1;
		for (int i = 0; i < numFeedbacks; i++) {
			System.out.println("Beginning feedback iteration "+i);
			assigners = proj.getAssigners();
			
			//Update skims and redistribute
			Map<String,NetworkSkim> skims = i == 0? proj.getInitialSkims() : proj.getFeedbackSkims(assigners);
			//TODO project should have skims
			markets.parallelStream().forEach(market -> market.updateSkims(skims));
			
			
			Collection<Assigner> ac = assigners.values();

			System.out.println("Calculating disaggregated ODProfiles");
			Stream.concat(
					markets.parallelStream()
					.flatMap(market -> market.getODProfiles()),
					dummies.parallelStream()
					.flatMap(dummy -> dummy.getODProfiles())
					)
			.forEach(
					od -> 
					ac.stream().forEach(assigner -> assigner.process(od))
					);

			
			System.out.println("Starting assignment");
			ac.stream().forEach(Assigner::run);
			

			
		}
		
		System.out.println("Feedback loop(s) completed");
		
		proj.output(assigners);
		
		
		System.out.println("Done");
	}
}
