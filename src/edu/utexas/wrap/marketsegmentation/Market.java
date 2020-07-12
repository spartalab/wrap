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
import edu.utexas.wrap.net.BasicDemographic;
import edu.utexas.wrap.net.Demographic;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.NetworkSkim;

public class Market implements ODProfileProvider {
	private Properties props;
	private Collection<BasicPurpose> purposes;
	private Graph network;
	
	public Market(Path marketFile, Graph network) throws IOException {
		props = new Properties();
		props.load(Files.newInputStream(marketFile));
		
		Path directory = marketFile.getParent().resolve(props.getProperty("dir"));
		this.network = network;
		
		Map<String,Demographic> demographics = getDemographics(directory);
		purposes = getPurposes(directory,demographics);
		
	}
	
	public void updateSkims(Map<String,NetworkSkim> skims) {
		purposes.stream().forEach(purpose -> purpose.updateSkims(skims));
	}
	
	public Stream<ODProfile> getODProfiles(){
		return purposes.stream().flatMap(Purpose::getODProfiles);
	}
	
	private Collection<BasicPurpose> getPurposes(Path directory, Map<String,Demographic> demographics) throws IOException {
		return Stream.of(props.getProperty("purposes.ids").split(","))
				.map(name -> directory.resolve(props.getProperty("purposes."+name+".file")))
				.map(purposeFile -> {
					try {
						return new BasicPurpose(purposeFile, network, demographics);
					} catch (IOException e) {
						System.err.println("Error while reading purpose file: "+purposeFile);
						return null;
					}
				})
				.filter(x -> x != null)
				.collect(Collectors.toSet());
	}
	
	private Map<String,Demographic> getDemographics(Path directory){
		return Stream.of(props.getProperty("demographics.ids").split(","))
				.collect(
						Collectors.toMap(
								Function.identity(), 
								name -> {
									Path path = directory.resolve(props.getProperty("demographics."+name+".file"));
									try {
										return new BasicDemographic(path, network);
									} catch (IOException e) {
										System.err.println("Error while reading demographic file: "+name);
										return null;
									}
								})
						);
	}

}
