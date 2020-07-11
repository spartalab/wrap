package edu.utexas.wrap.marketsegmentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.NetworkSkim;

public class Market {
	
	Path directory;
	Collection<Purpose> purposes;
	Graph network;
	
	public Market(Path marketPath, Graph network) throws IOException {
		directory = marketPath;
		this.network = network;
		purposes = getPurposes();
	}

	public Stream<ODProfile> buildODs(Collection<NetworkSkim> skims) {
		//combine all purposes' per-mode profiles into a single stream
		return purposes.parallelStream().flatMap(purpose -> purpose.buildODs(skims));
	}
	
	private Collection<Purpose> getPurposes() throws IOException {
		Properties purposes = new Properties();
		purposes.load(Files.newInputStream(directory.resolve("purposes.properties")));
		return Stream.of(purposes.getProperty("purposes").split(","))
				.map(name -> {
					try {
						return new Purpose(directory.resolve(name+".properties"), network);
					} catch (IOException e) {
						System.err.println("Error while reading "+name+" purpose file");
						return null;
					}
				})
				.filter(x -> x != null)
				.collect(Collectors.toSet());
	}
	
}
