package edu.utexas.wrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.marketsegmentation.Market;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.NetworkSkim;

public class wrapMarket {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//Get the name of the market source
		if (args.length == 0) {
			System.err.println("No model input file supplied");
			System.exit(1);
		}
		Path projDir = Paths.get(args[0]);
		Path projFile = projDir.resolve(args[1]);
		Properties proj;
		try {
			proj = getProjectProperties(projFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Error loading project properties");
			e.printStackTrace();
			proj = null;
			System.exit(-1);
		}
		Graph network = getNetwork(proj);
		Collection<Market> markets = getMarkets(projDir, proj, network);
		Map<TimePeriod,NetworkSkim> skims = getSeedSkims();
		Map<TimePeriod,Assigner> assigners = getAssigners();
		int numFeedbacks = 1;
		
		for (int i = 0; i < numFeedbacks; i++) {
			Stream<ODMatrix> stream =
			markets.parallelStream()
				.flatMap(market -> market.buildODs(skims));
			stream
				.forEach(od -> assigners.get(od.timePeriod()).attach(od));

			
			assigners.values().stream().forEach(Assigner::assign);
			
			assigners.entrySet().parallelStream().forEach(entry -> skims.put(entry.getKey(),entry.getValue().getSkim()));
		}
		
	}

	private static Graph getNetwork(Properties proj) {
		throw new RuntimeException("Not yet implemented");
	}

	private static Properties getProjectProperties(Path projFile) throws IOException {
		Properties proj = new Properties();
		proj.load(Files.newInputStream(projFile));
		return proj;
	}

	private static Map<TimePeriod, Assigner> getAssigners() {
		throw new RuntimeException("Not yet implemented");
	}

	private static Map<TimePeriod, NetworkSkim> getSeedSkims() {
		throw new RuntimeException("Not yet implemented");
	}

	private static Collection<Market> getMarkets(Path projDir, Properties project, Graph network) {
		String projNames = project.getProperty("markets");
		if (projNames == null) 
			throw new RuntimeException("No markets specified in project properties. Define at least one market");
		else return 
				Stream.of(projNames.split(","))
				.map(name -> {
					try {
						return new Market(projDir.resolve(name), network);
					} catch (IOException e) {
						System.err.println("Could not load trip purposes for "+name);
						e.printStackTrace();
						return null;
					}
				})
				.collect(Collectors.toSet());
	}

}
