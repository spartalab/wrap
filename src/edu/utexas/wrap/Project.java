package edu.utexas.wrap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.assignment.BasicStaticAssigner;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.StreamPassthroughAssigner;
import edu.utexas.wrap.marketsegmentation.Market;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.io.GraphFactory;
import edu.utexas.wrap.util.io.SkimFactory;

public class Project {
	private Properties props;
	private Graph network;
	private Path projDir;
	
	public Project(Path projFile) throws IOException {
		props = new Properties();
		props.load(Files.newInputStream(projFile));
		
		projDir = projFile.getParent();
		
		network = readNetwork();
	}
	
	private Graph readNetwork() {
		System.out.println("Reading network");
		
		try {
			File linkFile = projDir.resolve(props.getProperty("network.links")).toFile();
			
			Map<Integer, AreaClass> zoneClasses = getAreaClasses();
			
			switch(props.getProperty("network.linkType")) {

			case "conic":
				
				Integer ftn = Integer.parseInt(props.getProperty("network.firstThruNode"));
				return GraphFactory.readConicGraph(linkFile, ftn, zoneClasses);
				
			case "bpr":
				//TODO
				Graph g = GraphFactory.readTNTPGraph(linkFile);
				
				AtomicInteger idx = new AtomicInteger(0);
				
				zoneClasses.entrySet().parallelStream()
				.forEach(entry -> {
					Node n = g.getNode(entry.getKey());
					TravelSurveyZone z = new TravelSurveyZone(n,idx.getAndIncrement(),entry.getValue());
					n.setTravelSurveyZone(z);
					g.addZone(z);
				});
				g.setNumZones(idx.get());
				
				return g;
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

	public Graph getNetwork() {
		return network;
	}
	
	private Map<Integer, AreaClass> getAreaClasses() throws IOException {
		BufferedReader reader = Files.newBufferedReader(projDir.resolve(props.getProperty("network.zones")));
		reader.readLine();

		Map<Integer, AreaClass> zoneClasses = reader.lines()
				.map(string -> string.split(","))
				.collect(Collectors.toMap(
						args -> Integer.parseInt(args[0]), 
						args -> AreaClass.values()[Integer.parseInt(args[1])-1]));
		return zoneClasses;
	}

	public Collection<Market> getMarkets(){
		System.out.println("Reading Market configurations");

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
	
	public Map<String,Assigner> getAssigners(){
		System.out.println("Reading Assigner configurations");

		return Stream.of(props.getProperty("assigners.ids").split(","))
		.collect(Collectors.toMap(Function.identity(), id -> initializeAssigner(id)));
	}
	
	public Map<String,NetworkSkim> getInitialSkims(){
		System.out.println("Reading initial NetworkSkims");
		
		return Stream.of(props.getProperty("skims.ids").split(","))
				.parallel()
				.collect(
						Collectors.toMap(
								Function.identity(), 
								id -> SkimFactory.readSkimFile(
										projDir.resolve(props.getProperty("skims."+id+".file")), 
										false, 
										network
										)
								)
						)
		;
	}
	
	public Map<String,NetworkSkim> getFeedbackSkims(Map<String,Assigner> assigners){
		System.out.println("Updating NetworkSkims");

		return Stream.of(props.getProperty("skims.ids").split(","))
		.parallel()
		.collect(
				Collectors.toMap(
						Function.identity(),
						id ->{
							Assigner assigner = assigners.get(props.getProperty("skims."+id+".assigner"));
							ToDoubleFunction<Link> func;
							switch (props.getProperty("skims."+id+".function")) {
							default:
								System.err.println("Skim funciton not yet implemented. Reverting to travel time");
							case "travelTime":
								func = Link::getTravelTime;
							}
							return assigner.getSkim(func);
						}
						)
				);

//		return assigners.values().parallelStream().collect(Collectors.toMap(Assigner::toString,assigner -> assigner.getSkim(func)));
	}

	private Assigner initializeAssigner(String id) {
		try {

			switch (props.getProperty("assigners."+id+".class")) {
			case "stream":
				return new StreamPassthroughAssigner(
						projDir.resolve(props.getProperty("assigners."+id+".file"))
						);


			case "bush":
				return new BasicStaticAssigner<Bush>(
						network, //TODO this should provide a copy of the network, rather than the initial one to allow for multiple non-interfering Assigners
						projDir.resolve(props.getProperty("assigners."+id+".file"))
						);
			default:
				throw new RuntimeException("Not yet implemented");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private  void outputFlows() {
		try {
			BufferedWriter writer = Files.newBufferedWriter(projDir.resolve("flows.csv"),StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			network.getLinks().parallelStream()
			.map(link -> link.toString()+","+link.getFlow()+","+link.getTravelTime()+"\r\n")
			.forEach(line -> {
				try {
					writer.write(line);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void output(Map<String, NetworkSkim> finalSkims) {
		// TODO Auto-generated method stub
		
		System.out.println("Printing final skims");
		finalSkims.entrySet()
		.forEach(
				entry -> SkimFactory.outputCSV(
						finalSkims.get(entry.getKey()),
						projDir.resolve(props.getProperty("skims."+entry.getKey()+".file")),
						getNetwork().getTSZs()
						)
				);
		
		
		System.out.println("Printing final flows");
		outputFlows();

	}
}