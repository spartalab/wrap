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
	private Collection<BasicPurpose> purposes;
	private final Collection<TravelSurveyZone> zones;
	
	public Market(Path marketFile, Map<Integer,TravelSurveyZone> zoneIDs) throws IOException {
		props = new Properties();
		props.load(Files.newInputStream(marketFile));
		
		Path directory = marketFile.getParent().resolve(props.getProperty("dir"));

		this.zones = zoneIDs.values();
		purposes = getPurposes(directory, getDemographics(directory, zoneIDs), getFrictionFactors(directory));
		
	}
	
	public void updateSkims(Map<String,NetworkSkim> skims) {
		purposes.stream().forEach(purpose -> purpose.updateSkims(skims));
	}
	
	public Stream<ODProfile> getODProfiles() {
		return purposes.stream().flatMap(Purpose::getODProfiles);
	}
	
	private Collection<BasicPurpose> getPurposes(
			Path directory,
			Map<String,Demographic> demographics, 
			Map<String,FrictionFactorMap> frictFacts
			) throws IOException {
		
		return Stream.of(props.getProperty("purposes.ids").split(","))
				.map(name -> directory.resolve(props.getProperty("purposes."+name+".file")))
				.map(purposeFile -> {
					try {
						return new BasicPurpose(purposeFile, zones, demographics, frictFacts);
					} catch (IOException e) {
						System.err.println("Error while reading purpose file: "+purposeFile);
						return null;
					}
				})
				.filter(x -> x != null)
				.collect(Collectors.toSet());
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

}
