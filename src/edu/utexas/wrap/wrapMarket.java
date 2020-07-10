package edu.utexas.wrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.marketsegmentation.Market;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.util.io.GraphFactory;
import edu.utexas.wrap.util.io.SkimFactory;

public class wrapMarket {
	static Path projDir, projFile;
	static Graph network;
	static Properties proj;

	public static void main(String[] args) {
		//Get the name of the market source
		if (args.length < 2) {
			System.err.println("No model input file supplied");
			System.exit(1);
		}
		projDir = Paths.get(args[0]);
		projFile = projDir.resolve(args[1]);
		
		
		try {
			proj = getProjectProperties(projFile);
		} catch (IOException e) {
			System.err.println("Error loading project properties");
			e.printStackTrace();
			proj = null;
			System.exit(-1);
		}
		
		network = getNetwork(proj);
		
		Collection<Market> markets = getMarkets();
		
		Map<String,NetworkSkim> skims = getSeedSkims();
		
		Map<TimePeriod,Assigner> assigners = getAssigners();
		int numFeedbacks = 1;
		
		for (int i = 0; i < numFeedbacks; i++) {
			Stream<ODMatrix> stream =
			markets.parallelStream()
				.flatMap(market -> market.buildODs(skims));
			stream
				.forEach(od -> assigners.get(od.timePeriod()).attach(od));

			
			assigners.values().stream().forEach(Assigner::assign);
			
//			assigners.entrySet().parallelStream().forEach(entry -> skims.put(entry.getKey(),entry.getValue().getSkim()));
		}
		
	}

	private static Graph getNetwork(Properties proj) {
		try {
			File netFile = Paths.get(proj.getProperty("network.file")).toFile();
			Integer ftn = Integer.parseInt(proj.getProperty("network.firstThruNode"));
			return GraphFactory.readConicGraph(netFile, ftn);
		} catch (NullPointerException e) {
			System.err.println("Missing property: network.file");
			System.exit(-2);
			return null;
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			System.err.println("Invalid property value: network.firstThruNode\r\nCould not parse integer");
			System.exit(-3);
			return null;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println("Invalid property value: network.file\r\nFile not found");
			System.exit(-4);
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Error while reading network file");
			System.exit(-5);
			return null;
		}
	}

	private static Properties getProjectProperties(Path projFile) throws IOException {
		Properties proj = new Properties();
		proj.load(Files.newInputStream(projFile));
		return proj;
	}

	private static Map<TimePeriod, Assigner> getAssigners() {
		return null;
//		throw new RuntimeException("Not yet implemented");
	}

	private static Map<String, NetworkSkim> getSeedSkims() {
		return Stream.of(proj.getProperty("skimIDs").split(","))
				.parallel()
		.collect(Collectors.toMap(Function.identity(), id ->
			SkimFactory.readSkimFile(Paths.get(proj.getProperty("skimDir"), id+".csv"), false, network)
		));
	}

	private static Collection<Market> getMarkets() {
		String projNames = proj.getProperty("markets");
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
