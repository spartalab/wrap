package edu.utexas.wrap.marketsegmentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.demand.ODProfileProvider;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.net.BasicDemographic;
import edu.utexas.wrap.net.Demographic;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.io.FrictionFactorFactory;

public class Market implements ODProfileProvider {
	private Properties props;
	private Map<String,BasicPurpose> basicPurposes;
	private Collection<DummyPurpose> dummyPurposes;
	private final Collection<TravelSurveyZone> zones;
	private final Map<String,Demographic> basicDemos;
	private final Map<String,FrictionFactorMap> frictionFactors;
	public String name;

	public Market(Path marketFile, Map<Integer,TravelSurveyZone> zoneIDs) throws IOException {
		props = new Properties();
		props.load(Files.newInputStream(marketFile));
		name = marketFile.getFileName().toString();

		Path directory = marketFile.getParent().resolve(props.getProperty("dir"));

		this.zones = zoneIDs.values();
		this.basicDemos = getDemographics(directory,zoneIDs);
		this.frictionFactors = getFrictionFactors(directory);
		basicPurposes = getBasicPurposes(directory);
		dummyPurposes = getDummyPurposes(directory,zoneIDs);

	}

	private Collection<DummyPurpose> getDummyPurposes(Path directory,Map<Integer,TravelSurveyZone> zoneIDs) {
		// TODO Auto-generated method stub
		return Stream.of(props.getProperty("purposes.dummies.ids").split(","))
		.map(id -> {
			try {
				return new DummyPurpose(directory.resolve(props.getProperty("purposes."+id+".file")),zoneIDs);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}).collect(Collectors.toSet());
	}

	public void updateSkims(Map<String,NetworkSkim> skims) {
		basicPurposes.values().stream().forEach(purpose -> purpose.updateSkims(skims));
	}

	public Stream<ODProfile> getODProfiles() {
		return Stream.concat(
				basicPurposes.values().stream(),
				dummyPurposes.stream())
				.flatMap(Purpose::getODProfiles);
	}

	private Map<String,BasicPurpose> getBasicPurposes(
			Path directory
			) throws IOException {


		return Stream.of(props.getProperty("purposes.ids").split(","))
				.collect(
						Collectors.toMap(
								Function.identity(), 
								id -> {
									try {
										return new BasicPurpose(directory.resolve(props.getProperty("purposes."+id+".file")),this);
									} catch (IOException e1) {
										System.err.println("Error while reading purpose file: "+id);
										return null;
									}
								}

								)
						);
	}

	private Map<String,Demographic> getDemographics(Path directory, Map<Integer, TravelSurveyZone> zones) {
		//TODO improve error handling here
		return Stream.of(props.getProperty("demographics.ids").split(","))
				.collect(
						Collectors.toMap(
								Function.identity(), 
								name -> {
									Path path = directory.resolve(props.getProperty("demographics."+name+".file"));
									try {
										return new BasicDemographic(path, zones);
									} catch (IOException e) {
										System.err.println("Error while reading demographic file: "+name);
										return null;
									}
								})
						);
	}

	private Map<String,FrictionFactorMap> getFrictionFactors(Path directory) {
		return Stream.of(props.getProperty("frictFacts.ids").split(","))
				.collect(
						Collectors.toMap(
								Function.identity(), 
								id -> 
								FrictionFactorFactory.readFactorFile(
										directory.resolve(
												props.getProperty("frictFacts."+id+".file")
												)
										)
								)
						)
				;

	}


	public Demographic getBasicDemographic(String id) {
		return basicDemos.get(id);
	}

	public FrictionFactorMap getFrictionFactor(String id) {
		return frictionFactors.get(id);
	}

	public Collection<TravelSurveyZone> getZones() {
		return zones;
	}

	public BasicPurpose getBasicPurpose(String id) {
		return basicPurposes.get(id);
	}
	
	public String toString() {
		return name;
	}
}
