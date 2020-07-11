package edu.utexas.wrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.marketsegmentation.Market;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.util.io.GraphFactory;

public class Project {
	private Properties props;
	private Graph network;
	private Path projDir;
	
	public Project(Path projFile) throws IOException {
		props = new Properties();
		props.load(Files.newInputStream(projFile));
		
		projDir = projFile.getParent();
		
		network = getNetwork();
	}
	
	public Graph getNetwork() {
		try {
			File linkFile = projDir.resolve(props.getProperty("network.links")).toFile();

			switch(props.getProperty("network.linkType")) {

			case "conic":
				
				BufferedReader reader = Files.newBufferedReader(projDir.resolve(props.getProperty("network.zones")));
				
				reader.readLine();
				
				Map<Integer, AreaClass> zoneClasses = reader.lines()
				.map(string -> string.split(","))
				.collect(Collectors.toMap(
						args -> Integer.parseInt(args[0]), 
						args -> AreaClass.values()[Integer.parseInt(args[1])-1]));
				
				Integer ftn = Integer.parseInt(props.getProperty("network.firstThruNode"));
				return GraphFactory.readConicGraph(linkFile, ftn, zoneClasses);
				
			case "bpr":
				//TODO
				throw new RuntimeException("Not yet implemented");
				
			default:
				throw new IllegalArgumentException("network.type");
			}
			
		} catch (NullPointerException e) {
			System.err.println("Missing property: network.linkFile");
			System.exit(-2);
			return null;
			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			System.err.println("Invalid property value: network.firstThruNode\r\nCould not parse integer");
			System.exit(-3);
			return null;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println("Invalid property value: network.linkFile\r\nFile not found");
			e.printStackTrace();
			System.exit(-4);
			return null;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Error while reading network files");
			System.exit(-5);
			return null;
			
		} catch (IllegalArgumentException e) {
			System.err.println("Illegal argument: "+e.getMessage());
			System.exit(-6);
			return null;
		}
		
	}

	public Collection<Market> getMarkets(){
		String projNames = props.getProperty("markets.ids");
		if (projNames == null) 
			throw new RuntimeException("No markets specified in project properties. Define at least one market");
		else return 
				Stream.of(projNames.split(","))
				.map(name -> {
					try {
						return new Market(projDir.resolve(props.getProperty("markets."+name+".file")), network);
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
