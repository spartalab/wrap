/*
    wrap - free and open-source urban transportation modeling software
    Copyright (C) 2017 the wrap project, The University of Texas at Austin

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.utexas.wrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.assignment.BasicStaticAssigner;
import edu.utexas.wrap.assignment.bush.StreamPassthroughAssigner;
import edu.utexas.wrap.marketsegmentation.SurrogatePurpose;
import edu.utexas.wrap.marketsegmentation.Market;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.FixedSizeNetworkSkim;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.io.output.FilePassthroughDummyAssigner;

/** A high-level representation of a multi-market, multi-purpose travel demand model. This class 
 * defines the set of TravelSurveyZones for which demand should be modeled, as well as the Market
 * instances which travel among these zones. Instances of this class may also define a number of
 * Assigner instances which consume ODProfiles generated by the Markets' Purposes, as well as an
 * initial set of NetworkSkims which may be relevant in ODProfile creation. These skims should be
 * updated by the Assigners after their termination.
 * 
 * The Project should define a project directory. This directory should contain all data relevant
 * to the Project, as it will serve as the point against which all relative paths will be resolved.
 * 
 * @author William
 *
 */
public class Project {
	private final Properties props;
	private Map<Integer, TravelSurveyZone> zones;
	private Map<String,NetworkSkim> currentSkims;
	private final Path projFile;
	private String name;
	private Collection<Market> markets;
	private Collection<SurrogatePurpose> surrogates;
	private Map<String,Assigner> assigners;
	
	/**Project constructor from a Properties file (*.wrp)
	 * @param projFile the location of a Project Properties (*.wrp) file
	 * @throws IOException if the file located by {@code projFile} does not exist or is corrupt
	 */
	public Project(Path projFile) throws IOException, NullPointerException {
		this.projFile = projFile;
		props = new Properties();
		name = projFile.getFileName().toString();
		try{ 
			loadPropsFromFile();
			loadZones();
			loadInitialSkims();
			loadMarkets();
			loadSurrogatePurposes();
			loadAssigners();
		} catch (NoSuchFileException e) {
			if (currentSkims == null) currentSkims = new HashMap<String,NetworkSkim>();
		}

	}

	private void loadPropsFromFile() throws IOException, NoSuchFileException {
		props.load(Files.newInputStream(getPath()));
	}

	public Path getPath() {
		return projFile;
	}

	public void loadZones() {
		try {
		BufferedReader reader = Files.newBufferedReader(getDirectory().resolve(props.getProperty("network.zones")));
		reader.readLine();
		AtomicInteger idx = new AtomicInteger(0);

		zones = reader.lines()
				.map(string -> string.split(","))
				.collect(Collectors.toMap(
						args -> Integer.parseInt(args[0]), 
						args -> new TravelSurveyZone(Integer.parseInt(args[0]),idx.getAndIncrement(),AreaClass.values()[Integer.parseInt(args[1])-1])));
		} catch (IOException | NullPointerException  e) {
			zones = Collections.emptyMap();
		}
	}

	/**Read a list of Market ids from the Project Properties, then load the
	 * corresponding Markets from the corresponding file. 
	 * 
	 * The list of Markets is
	 * defined with the Property key {@code markets.ids}; for each id {@code foo} in
	 * this list, a corresponding key {@code markets.foo.file} must be defined. The 
	 * {@code .file} should be a relative path from the Project directory to a file
	 * (*.wrm) containing the Market Properties associated with this id. 
	 * 
	 * @return a Collection of initialized Markets read from files
	 */
	private void loadMarkets(){

		String projNames = props.getProperty("markets.ids");
		
		if (projNames == null) 
			markets = Collections.emptySet();
		
		else markets = 
				Stream.of(projNames.split(","))
				.map(name -> {
					try {
						return new Market(name,getDirectory().resolve(props.getProperty("markets."+name+".file")), this);
					} catch (IOException e) {
						System.err.println("Could not load trip purposes for "+name);
						e.printStackTrace();
						return null;
					}
				})
				.collect(Collectors.toSet());
	}
	
	public Collection<NetworkSkim> getSkims(){
		return currentSkims.values();
	}
	
	public Collection<Market> getMarkets(){
		return markets;
	}
	
	public List<Assigner> getAssigners(){
		return assigners.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(Map.Entry::getValue)
				.collect(Collectors.toList());
	}
	
	public Assigner getAssigner(String id) {
		return assigners.get(id);
	}
	/**
	 * @return a Collection of DummyPurposes which are not affiliated with any particular Market
	 */
	private void loadSurrogatePurposes() {
		// TODO Auto-generated method stub
		String dummyNames = props.getProperty("dummies.ids");
		
		if (dummyNames == null) surrogates = Collections.emptySet();
		else surrogates = Stream.of(dummyNames.split(","))
				.map(name ->{
					try {
						return new SurrogatePurpose(null,getDirectory().resolve(props.getProperty("dummies."+name+".file")), zones);
					} catch (IOException e) {
						System.out.println("Could not load dummy trip purpose "+name);
						e.printStackTrace();
						return null;
					}
				})
				.collect(Collectors.toSet());
	}
	
	/**Read a list of Assigner ids from the Project Properties, then load the
	 * corresponding Assigners according to their definition. 
	 * 
	 * The list of Assigners is
	 * defined with the Property key {@code assigners.ids}; for each id {@code foo} in
	 * the list, a corresponding key {@code assigners.foo.class} must be defined. The
	 * @{code .class} should be an available Assigner implementation. Depending on the
	 * Assigner class specified, an additional key {@code assigners.foo.file} may 
	 * define a relative path from the Project directory to a file (*.wrapr) containing
	 * the Assigner Properties associated with this id.
	 * 
	 * @return a Map from an Assigner's id to newly-generated instance.
	 */
	private void loadAssigners(){

		assigners = getAssignerIDs().stream()
				.collect(Collectors.toMap(Function.identity(),id -> initializeAssigner(id)));
	}
	
	/**Read a list of NetworkSkim ids from the Project Properties, then load the
	 * corresponding NetworkSkims from the corresponding file. 
	 * 
	 * The list of NetworkSkims is defined with the Property key {@code skims.ids}; 
	 * for each id {@code foo} in the list, a corresponding key {@code skims.foo.file} 
	 * must be defined. The {@code .file} should be a relative path from the Project 
	 * directory to a file (*.csv) containing the data associated with the initial cost
	 * skims to be used in building the Project's ODProfiles
	 *  
	 * the ODProfiles of the project.
	 * 
	 * @return a Map from a NetworkSkim's id to its newly-generated instance
	 */
	private void loadInitialSkims(){
		
		currentSkims = getSkimIDs().stream()
				.parallel()
				.collect(
						Collectors.toMap(
								Function.identity(), 
								id -> new FixedSizeNetworkSkim(id, zones.size())
								)
						)
		;
	}
	
	/**Read a list of NetworkSkim ids from the Project Properties, then get the updated
	 * NetworkSkims from their associated Assigners. 
	 * 
	 * The list of NetworkSkims is defined with the Property key {@code skims.ids};
	 * for each id {@code foo} in the list, a corresponding key {@code skims.foo.assigner}
	 * must be defined. The {@code .assigner} should be an Assigner id defined in the
	 * {@code assigners.ids} property. Additionally, the key {@code skims.foo.function}
	 * specifies the cost function to be used by the Assigner in calculating the updated 
	 * cost skim.
	 * 
	 * @param assigners a Map from Assigner ids to their current instance
	 * @return a Map from NetworkSkim ids to their updated instance
	 */
//	private void updateFeedbackSkims(){
//
//		currentSkims = getSkimIDs().stream()
//		.parallel()
//		.collect(
//				Collectors.toMap(
//						Function.identity(),
//						id ->{
//							Assigner assigner = assigners.get(props.getProperty("skims."+id+".assigner"));
//							ToDoubleFunction<Link> func;
//							switch (props.getProperty("skims."+id+".function")) {
//							case "travelTimeSingleOcc":
//								func = (Link x) -> 
//									x.allowsClass(Mode.SINGLE_OCC)? x.getTravelTime() : Double.MAX_VALUE;
//									break;
//							default:
//								System.err.println("Skim funciton not yet implemented. Reverting to travel time");
//							case "travelTime":
//								func = Link::getTravelTime;
//							}
//							return assigner.getSkim(id,func);
//						}
//						)
//				);
//
//	}
	
	public void updateSkim(String id, NetworkSkim skim) {
		currentSkims.put(id, skim);
	}

	private Assigner initializeAssigner(String id) {
		try {

			switch (props.getProperty("assigners."+id+".class")) {
			case "stream":
				return new StreamPassthroughAssigner(
						getDirectory().resolve(props.getProperty("assigners."+id+".file"))
						);


			case "builtin":
				return BasicStaticAssigner.fromPropsFile(id,
						getDirectory(),
						props.getProperty("assigners."+id+".file"),
						zones
						);
			case "file":
				return new FilePassthroughDummyAssigner(getDirectory().resolve(props.getProperty("assigners."+id+".file")),zones);
			default:
				throw new RuntimeException("Not yet implemented");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	
	/**
	 * @param assigners
	 */
//	private void output(Map<String,Assigner> assigners) {
//		// TODO Auto-generated method stub
//		updateFeedbackSkims(assigners);
//		System.out.println("Printing final skims");
//		currentSkims.entrySet().stream()
//		.filter(entry -> getSkimUpdatable(entry.getKey()))
//		.forEach(
//				entry -> SkimFactory.outputCSV(
//						currentSkims.get(entry.getKey()),
//						Paths.get(getSkimFile(entry.getKey())),
//						zones.values()
//						)
//				);
//		
//		
//		System.out.println("Printing final flows");
//		assigners.forEach((id, assigner) -> {
//			assigner.outputFlows(projDir.resolve(id+"flows.csv"));
//		});
//
//	}
	
	/**
	 *
	 */
	public String toString() {
		return name;
	}

	public Path getDirectory() {
		return projFile.getParent();
	}
	
//	@Override
//	public void run() {
//		if (markets == null) markets = loadMarkets();
//		
//
//		Map<String,Assigner> assigners = null;
//		
//		for (int i = 0; i < getMaxIterations(); i++) {
//			System.out.println("Beginning feedback iteration "+i);
//			assigners = loadAssigners();
//			
//			//Update skims and redistribute
//			if (i > 0) updateFeedbackSkims(assigners);
//			
//			
//			Collection<Assigner> ac = assigners.values();
//
//			System.out.println("Calculating disaggregated ODProfiles");
//			Stream.concat(
//					markets.parallelStream()
//					.flatMap(market -> market.getODProfiles()),
//					surrogates.parallelStream()
//					.flatMap(dummy -> dummy.getODProfiles())
//					)
//			.forEach(
//					od -> 
//					ac.stream().forEach(assigner -> assigner.process(od))
//					);
//
//			
//			System.out.println("Starting assignment");
//			ac.stream().forEach(Assigner::run);
//			
//
//			
//		}
//		
//		System.out.println("Feedback loop(s) completed");
//		
//		output(assigners);
//		
//		
//		System.out.println("Done");
//	}

	public Integer getMaxIterations() {
		// TODO Auto-generated method stub
		return Integer.parseInt(props.getProperty("feedbackIters","1"));
	}
	
	public void setMaxIterations(Integer numIterations) {
		props.setProperty("feedbackIters", numIterations.toString());
	}

	public List<String> getSkimIDs() {
		// TODO Auto-generated method stub
		String prop = props.getProperty("skims.ids");
		if (prop == null || prop.replaceAll(",", "").isBlank()) return new ArrayList<String>();
		String[] ids = prop.split(",");
		return new ArrayList<String>(List.of(ids));
	}

	public List<String> getAssignerIDs() {
		// TODO Auto-generated method stub
		String params = props.getProperty("assigners.ids");
		if (params == null || params.replaceAll(",", "").isBlank()) return new ArrayList<String>();
		return new ArrayList<String>(List.of(params.split(",")));
	}
	
	public List<String> getMarketIDs() {
		// TODO Auto-generated method stub
		String params = props.getProperty("markets.ids");
		if (params == null || params.replaceAll(",", "").isBlank()) return new ArrayList<String>();
		String[] ids = params.split(",");
		return new ArrayList<String>(List.of(ids));
	}

	public String getSkimFile(String skimID) {
		return props.getProperty("skims."+skimID+".file");
	}

	public String getSkimAssigner(String skimID) {
		return props.getProperty("skims."+skimID+".assigner");
	}
	
	public String getSkimFunction(String skimID) {
		return props.getProperty("skims."+skimID+".function");
	}
	
	public String getMarketFile(String marketID) {
		return props.getProperty("markets."+marketID+".file");
	}
	
	public String getAssignerFile(Assigner assignerID) {
		return props.getProperty("assigners."+assignerID+".file");
	}
	
	public Boolean getSkimUpdatable(String skimID) {
		return Boolean.parseBoolean(props.getProperty("skims."+skimID+".overwrite"));
	}
	
	public void removeSkim(NetworkSkim skim) {
		List<String> ids = getSkimIDs();
		ids.remove(skim.toString());
		if (!ids.isEmpty()) props.setProperty("skims.ids", String.join(",", ids));
		else props.remove("skims.ids");
		
		Set<String> keys = props.stringPropertyNames();
		for (String key : keys) {
			if (key.startsWith("skims."+skim.toString())) props.remove(key);
		}
		
		currentSkims.remove(skim.toString());
	}
	
	public void removeMarket(Market market) {
		List<String> ids = getMarketIDs();
		ids.remove(market.toString());
		if (!ids.isEmpty()) props.setProperty("markets.ids", String.join(",", ids));
		else props.remove("markets.ids");
		
		Set<String> keys = props.stringPropertyNames();
		for (String key : keys) {
			if (key.startsWith("markets."+market.toString())) props.remove(key);
		}
		
		markets.remove(market);
	}

	public void addSkim(String skimID, String assignerID, String skimFunction, String skimSourceURI, Boolean updatable) {
		List<String> ids = getSkimIDs();
		ids.add(skimID);
		props.setProperty("skims.ids", String.join(",", ids));
		
		props.setProperty("skims."+skimID+".file", skimSourceURI);
		props.setProperty("skims."+skimID+".assigner", assignerID);
		props.setProperty("skims."+skimID+".function", skimFunction);
		props.setProperty("skims."+skimID+".overwrite", updatable.toString());
		
		currentSkims.put(skimID,new FixedSizeNetworkSkim(skimID, zones.size()));
		
	}

	public void addMarket(String marketID, String marketSourceURI) throws IOException {
		List<String> ids = getMarketIDs();
		ids.add(marketID);
		props.setProperty("markets.ids", String.join(",", ids));
		
		props.setProperty("markets."+marketID+".file", marketSourceURI);
		
		markets.add(new Market(marketID,Paths.get(marketSourceURI),this));
	}
	
	public void setSkimFile(String curSkimID, String text) {
		props.setProperty("skims."+curSkimID+".file", text);
	}
	
	public void setSkimAssigner(String curSkimID, String assignerID) {
		props.setProperty("skims."+curSkimID+".assigner", assignerID);
	}
	
	public void setSkimFunction(String curSkimID, String functionID) {
		props.setProperty("skims."+curSkimID+".function", functionID);
	}
	
	public void setSkimUpdatable(String curSkimID, Boolean isUpdatable) {
		props.setProperty("skims."+curSkimID+".overwrite", isUpdatable.toString());
	}

	public String getZoneFile() {
		return props.getProperty("network.zones");
	}

	public void setZoneFile(String zoneFile) {
		props.setProperty("network.zones", zoneFile);
	}

	public void setMarketFile(String curMarketID, String text) {
		props.setProperty("markets."+curMarketID+".file", text);
	}
	
	public String getAssignerClass(Assigner assigner) {
		return props.getProperty("assigners."+assigner+".class");
	}

	public void setAssignerClass(Assigner assigner, String text) {
		props.setProperty("assigners."+assigner+".class", text);
	}
	
	public void setAssignerFile(Assigner assigner, String text) {
		props.setProperty("assigners."+assigner+".file", text);
	}

	public void addAssigner(String assignerID, String assignerClass, String assignerSourceURI) {
		List<String> ids = getAssignerIDs();
		ids.add(assignerID);
		props.setProperty("assigners.ids", String.join(",",ids));
		
		props.setProperty("assigners."+assignerID+".class", assignerClass);
		props.setProperty("assigners."+assignerID+".file", assignerSourceURI);
	}

	public void removeAssigner(Assigner assigner) {
		// TODO Auto-generated method stub
		assigners.remove(assigner.toString());
		if (!assigners.isEmpty()) props.setProperty("assigners.ids", String.join(",", assigners.keySet()));
		else props.remove("assigners.ids");
		
		Set<String> keys = props.stringPropertyNames();
		for (String key : keys) {
			if (key.startsWith("assigners."+assigner)) props.remove(key);
		}
	}

	public Map<Integer,TravelSurveyZone> getZones(){
		return zones;
	}

	public NetworkSkim getNetworkSkim(String skimID) {
		// TODO Auto-generated method stub
		return currentSkims.get(skimID);
	}

	public void writeProperties() throws IOException {
		// TODO Auto-generated method stub
		props.store(Files.newOutputStream(getPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE), null);
	}

	public Path metricOutputPath() {
		// TODO Auto-generated method stub
		return getDirectory().resolve(props.getProperty("metricOutputPath"));
	}

}