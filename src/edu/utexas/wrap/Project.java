package edu.utexas.wrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.io.SkimFactory;

public class Project {
	private final Properties props;
//	private Graph network;
	private Map<Integer, TravelSurveyZone> zones;
	private Path projDir;
	
	public Project(Path projFile) throws IOException {
		props = new Properties();
		props.load(Files.newInputStream(projFile));
		
		projDir = projFile.getParent();
		zones = getZones();
//		network = readNetwork();
//		throw new RuntimeException("Zones not loaded");
	}
	
	
	private Map<Integer, TravelSurveyZone> getZones() throws IOException {
		BufferedReader reader = Files.newBufferedReader(projDir.resolve(props.getProperty("network.zones")));
		reader.readLine();
		AtomicInteger idx = new AtomicInteger(0);

		Map<Integer, TravelSurveyZone> zones = reader.lines()
				.map(string -> string.split(","))
				.collect(Collectors.toMap(
						args -> Integer.parseInt(args[0]), 
						args -> new TravelSurveyZone(Integer.parseInt(args[0]),idx.getAndIncrement(),AreaClass.values()[Integer.parseInt(args[1])-1])));
		return zones;
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
						return new Market(projDir.resolve(props.getProperty("markets."+name+".file")), zones);
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
										zones
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
						projDir.resolve(props.getProperty("assigners."+id+".file")),
						zones
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

	
	public void output(Map<String,Assigner> assigners) {
		// TODO Auto-generated method stub
		Map<String,NetworkSkim> finalSkims = getFeedbackSkims(assigners);
		
		System.out.println("Printing final skims");
		finalSkims.entrySet()
		.forEach(
				entry -> SkimFactory.outputCSV(
						finalSkims.get(entry.getKey()),
						projDir.resolve(props.getProperty("skims."+entry.getKey()+".file")),
						zones.values()
						)
				);
		
		
		System.out.println("Printing final flows");
		assigners.forEach((id, assigner) -> {
			assigner.outputFlows(projDir.resolve(id+"flows.csv"));
		});

	}
}