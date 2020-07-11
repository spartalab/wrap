package edu.utexas.wrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.marketsegmentation.Market;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.util.io.GraphFactory;

public class Project {
	private Properties props;
	private Graph network;
	private Path projDir;
	
	public Project(Properties propertiesFile, Path directory) {
		props = propertiesFile;
		projDir = directory;
		network = getNetwork();
	}
	
	public Graph getNetwork() {
		try {
			File netFile = Paths.get(props.getProperty("network.file")).toFile();
			Integer ftn = Integer.parseInt(props.getProperty("network.firstThruNode"));
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

	public Collection<Market> getMarkets(){
		String projNames = props.getProperty("markets");
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
	
	public Collection<Assigner> getAssigners(){
		throw new RuntimeException("Not yet implemented");
	}
	
	public Collection<NetworkSkim> getInitialSkims(){
		throw new RuntimeException("Not yet implemented");
//		return Stream.of(props.getProperty("skimIDs").split(","))
//				.parallel()
//				.collect(
//						Collectors.toMap(
//								id -> TimePeriod.valueOf(id), 
//								id -> SkimFactory.readSkimFile(Paths.get(proj.getProperty("skimDir"), id+".csv"), false, network)
//								)
//						);
	}
	
	public Collection<NetworkSkim> getFeedbackSkims(Collection<Assigner> assigners){
		throw new RuntimeException("Not yet implemented");
	}
}
