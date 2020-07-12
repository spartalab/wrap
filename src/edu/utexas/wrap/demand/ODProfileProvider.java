package edu.utexas.wrap.demand;

import java.util.stream.Stream;

public interface ODProfileProvider {

	public Stream<ODProfile> getODProfiles();
}
