package edu.utexas.wrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.marketsegmentation.Market;

public class wrapMarket {
	static Path projDir, projFile;
	static Project proj;
	
	public static void main(String[] args) {
		//Get the name of the market source
		if (args.length < 2) {
			System.err.println("No model input file supplied");
			System.exit(1);
		}
		projDir = Paths.get(args[0]);
		projFile = projDir.resolve(args[1]);
		
		
		try {
			proj = new Project(getProjectProperties(projFile),projDir);
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

	private static Properties getProjectProperties(Path projFile) throws IOException {
		Properties proj = new Properties();
		proj.load(Files.newInputStream(projFile));
		return proj;
	}

}
