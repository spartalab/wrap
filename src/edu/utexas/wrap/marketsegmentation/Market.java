package edu.utexas.wrap.marketsegmentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.net.DefaultDemographic;
import edu.utexas.wrap.net.Demographic;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.NetworkSkim;

public class Market {
	private Properties props;
	private Collection<Purpose> purposes;
	private Graph network;
	
	public Market(Path marketFile, Graph network) throws IOException {
		props = new Properties();
		props.load(Files.newInputStream(marketFile));
		
		Path directory = marketFile.getParent().resolve(props.getProperty("dir"));
		this.network = network;
		
		Collection<Demographic> demographics = getDemographics(directory);
		purposes = getPurposes(directory,demographics);
		
	}

	public Stream<ODProfile> buildODs(Collection<NetworkSkim> skims) {
		//combine all purposes' per-mode profiles into a single stream
		return purposes.parallelStream().flatMap(purpose -> purpose.buildODs(skims));
	}
	
	private Collection<Purpose> getPurposes(Path directory, Collection<Demographic> demographics) throws IOException {
		return Stream.of(props.getProperty("purposes.ids").split(","))
				.map(name -> directory.resolve(props.getProperty("purposes."+name+".file")))
				.map(path -> {
					try {
						return new Purpose(path, network);
					} catch (IOException e) {
						System.err.println("Error while reading purpose file: "+path);
						return null;
					}
				})
				.filter(x -> x != null)
				.collect(Collectors.toSet());
	}
	
	private Collection<Demographic> getDemographics(Path directory){
		return Stream.of(props.getProperty("demographics.ids").split(","))
		.map(name -> directory.resolve(props.getProperty("demographics."+name+".file")))
		.map(path -> {
			try {
				return new DefaultDemographic(path);
			} catch (IOException e) {
				System.err.println("Error while reading demographic file: "+path);
				return null;
			}
		})
		.collect(Collectors.toSet());
	}
	
}
